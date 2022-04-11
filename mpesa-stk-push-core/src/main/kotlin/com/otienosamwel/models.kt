package com.otienosamwel

@kotlinx.serialization.Serializable
data class StkDetails(
    val AccountReference: String,
    val Amount: Int,
    val BusinessShortCode: Int,
    val CallBackURL: String,
    val PartyA: Long,
    val PartyB: Int,
    val Password: String,
    val PhoneNumber: Long,
    val Timestamp: String,
    val TransactionDesc: String,
    val TransactionType: String
)

@kotlinx.serialization.Serializable
internal data class TokenInfo(val access_token: String, val expires_in: String)


/**
 * This is the model class wth details regarding the mpesa application.
 * @param clientId the client id of the application in the daraja portal.
 * @param clientSecret the client secret of the application in the daraja portal.
 * @param passKey the pass key of the application in the daraja portal.
 */
data class MpesaAppDetails(val clientId: String, val clientSecret: String, val passKey: String)



