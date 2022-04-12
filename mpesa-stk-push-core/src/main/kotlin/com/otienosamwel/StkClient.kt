package com.otienosamwel

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat

/**
 * The main stk client class. This class takes care of authentication and authorization of requests.
 *
 * @param stkDetails receives a call from the [buildStkDetails] function.
 * @param mpesaAppDetails an instance of [MpesaAppDetails] class.
 */
class StkClient(stkDetails: () -> StkDetails, private val mpesaAppDetails: MpesaAppDetails) {
    private val stkDetails: StkDetails = stkDetails()

    companion object {
        private const val TOKEN_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"
        private const val STK_URI = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"
    }

    private val tokenClient = HttpClient(CIO) {
        install(Auth) {
            basic {

                sendWithoutRequest { true }

                credentials {
                    BasicAuthCredentials(mpesaAppDetails.clientId, mpesaAppDetails.clientSecret)
                }
            }
        }

        install(Logging) {
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    private val client = HttpClient(CIO) {

        expectSuccess = false

        engine {
            requestTimeout = 50000
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {
                sendWithoutRequest { true }
                loadTokens {
                    val tokenInfo: TokenInfo = tokenClient.get(TOKEN_URL).body()
                    BearerTokens(tokenInfo.access_token, "")
                }
            }
        }
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        return sdf.format(System.currentTimeMillis())
    }

    private fun getPassword(timeStamp: String): String {
        val stringToEncode = "${stkDetails.PartyB}${mpesaAppDetails.passKey}$timeStamp"

        return stringToEncode.toByteArray(Charsets.UTF_8).encodeBase64()
    }

    /**
     * Makes the mpesa stk push request. This is a suspending function and can only be called within the context of a coroutine.
     *
     * @return  The http status code. This is only used to judge if the client received the request successfully. Details
     * on whether the client accepted the request are sent to the callback url by safaricom.
     */
    suspend fun makeStkPush(): HttpStatusCode {
        val timeStamp = getFormattedTime()
        val password = getPassword(timeStamp)

        val response: HttpResponse = client.post(STK_URI) {
            setBody(
                StkDetails(
                    AccountReference = stkDetails.AccountReference,
                    Amount = stkDetails.Amount,
                    BusinessShortCode = stkDetails.BusinessShortCode,
                    CallBackURL = stkDetails.CallBackURL,
                    PartyA = stkDetails.PartyA,
                    PartyB = stkDetails.PartyB,
                    Password = password,
                    PhoneNumber = stkDetails.PhoneNumber,
                    Timestamp = timeStamp,
                    TransactionDesc = stkDetails.TransactionDesc,
                    TransactionType = stkDetails.TransactionType
                )
            )
        }
        return response.status
    }
}

/**
 * A builder function for the [StkDetails] class.
 *
 * @param accountReference A brief account description, used to identify the business.
 * @param amount The amount to be paid from the clients account.
 * @param businessShortCode The short code of the business, also called the till number.
 * @param callbackUri The url to which safaricom will send a request confirming whether the client accepted the payment request.
 * @param partyA The mpesa registered phone number of the client sending money.
 * @param partyB Usually same as the short code of the business, also called the till number.
 * @param phoneNumber The phone number of the client that will receive the stk prompt
 * @param transactionDescription any additional information to be sent with the request.
 * @return  An [StkDetails] object.
 */
fun buildStkDetails(
    accountReference: String,
    amount: Int,
    businessShortCode: Int,
    callbackUri: String = "https://example.com",
    partyA: Long,
    partyB: Int = businessShortCode,
    phoneNumber: Long,
    transactionDescription: String
): StkDetails {

    return StkDetails(
        AccountReference = accountReference,
        Amount = amount,
        BusinessShortCode = businessShortCode,
        CallBackURL = callbackUri,
        PartyA = partyA,
        PartyB = partyB,
        Password = "",
        PhoneNumber = phoneNumber,
        Timestamp = "",
        TransactionDesc = transactionDescription,
        TransactionType = "CustomerPayBillOnline"
    )
}