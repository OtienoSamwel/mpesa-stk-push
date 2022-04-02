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

data class MpesaAppDetails(val clientId: String, val clientSecret: String, val passKey: String)



