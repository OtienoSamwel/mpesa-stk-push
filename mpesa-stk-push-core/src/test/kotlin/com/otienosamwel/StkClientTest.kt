package com.otienosamwel

import io.ktor.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Base64

internal class StkClientTest {

    @Test
    fun ensure_the_base64_encoding_is_uniform() {

        val withKotlin = "sam".toByteArray(Charsets.UTF_8).encodeBase64()
        val withJava = String(Base64.getEncoder().encode("sam".toByteArray()))
        println(" comparing $withKotlin to $withJava")
        assertTrue(withKotlin == withJava)
    }

}