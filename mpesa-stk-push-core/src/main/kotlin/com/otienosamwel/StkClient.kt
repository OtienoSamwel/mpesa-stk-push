package com.otienosamwel

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.text.SimpleDateFormat
import java.util.*

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
        install(Logging) { level = LogLevel.ALL }
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { isLenient = true; prettyPrint = true })
        }
    }

    private val client = HttpClient(CIO) {
        expectSuccess = false
        engine { requestTimeout = 50000 }
        defaultRequest { contentType(ContentType.Application.Json) }
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { isLenient = true; prettyPrint = true })
        }
        install(Logging) { level = LogLevel.ALL }
        install(Auth) {
            bearer {
                sendWithoutRequest { true }
                loadTokens {
                    val tokenInfo: TokenInfo = tokenClient.get(TOKEN_URL)
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
        val encoded: ByteArray = Base64.getEncoder().encode(stringToEncode.toByteArray()); return String(encoded)
    }

    suspend fun makeStkPush(): HttpStatusCode {
        val timeStamp = getFormattedTime()
        val password = getPassword(timeStamp)
        val response: HttpResponse = client.post(STK_URI) {
            body = StkDetails(
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
        }
        return response.status
    }
}

fun buildStkDetails(
    accountReference: String,
    amount: Int,
    businessShortCode: Int,
    callbackUri: String = "https://example.com",
    partyA: Long, partyB: Int, phoneNumber: Long, transactionDescription: String
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