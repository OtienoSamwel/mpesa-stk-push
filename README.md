[![](https://jitpack.io/v/otienosamwel/mpesa-stk-push.svg)](https://jitpack.io/#otienosamwel/mpesa-stk-push)

# Mpesa-stk-push

An mpesa library to perform stk push requests with minimal configuration. Thia library is a wrapper around the mpesa
express daraja api.

## Installation

In your root build.gradle file add the following dependencies:

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

After this you can add the following dependency to your build.gradle file for a specific module:

```groovy  
dependencies {
    implementation 'com.github.otienosamwel:mpesa-stk-push:Tag'
}
```

## Usage
This library will work on any platform that supports Kotlin/JVM. Below is a sample of the library in action inside 
a ktor-server project. More example projects will be added soon.

```kotlin   
import com.otienosamwel.MpesaAppDetails
import com.otienosamwel.StkClient
import com.otienosamwel.buildStkDetails
import io.ktor.application.*
import io.ktor.routing.*


fun Application.configureRouting() {
    routing {
        get("/stk") {
            val stkLClient = StkClient(
                mpesaAppDetails = MpesaAppDetails(
                    clientId = "IpOAkYhhizzfwkAHP1XLNz1SNeJRvAPR",
                    clientSecret = "vicGTsdYHArr0AkK",
                    passKey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
                ),
                stkDetails = {
                    buildStkDetails(
                        accountReference = "Company Limited",
                        amount = 1,
                        businessShortCode = 174379,
                        partyA = 254708374149,
                        partyB = 174379,
                        transactionDescription = "Payment of rent",
                        phoneNumber = 254746707600
                    )
                })

            stkLClient.makeStkPush()
        }
    }
}
```
