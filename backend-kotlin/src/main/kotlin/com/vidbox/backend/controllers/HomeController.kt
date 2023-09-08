package com.vidbox.backend.controllers

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.secretmanager.v1.*
import com.google.cloud.storage.*
import com.vidbox.backend.services.GmailService
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.io.*
import java.util.*
import java.util.zip.CRC32C
import java.util.zip.Checksum

@RestController
class HomeController(private val gmailService: GmailService,
                     @Value("\${gmail-oauth2-secret}") private val gmailOauth2Secret: String) {

    fun toReadableFormat(dateTime: String): String {
        val year = dateTime.substring(0, 4).toInt()
        val month = dateTime.substring(5, 7).toInt()
        val day = dateTime.substring(8, 10).toInt()
        val hour = dateTime.substring(11, 13).toInt()
        val minute = dateTime.substring(14, 16).toInt()

        val months = arrayOf(
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
        )

        val amPm = if (hour < 12) "AM" else "PM"
        val readableHour = if (hour % 12 == 0) 12 else hour % 12

        return "${months[month - 1]} $day, $year at $readableHour:${minute.toString().padStart(2, '0')} $amPm"
    }

    fun extractMovieName(htmlContent: String): String {
        val document = Jsoup.parse(htmlContent)
        val tdElement = document.select("td[style*=font-family: AvenirNext-Regular, Arial,sans-serif;]").firstOrNull()
        val spans = tdElement?.select("span[style*=font-weight:bold;font-size:18px;]") ?: emptyList()
        val dateTime = spans.getOrNull(1)?.text() ?: ""
        var movieNameSpan = document.select("span[style*=font-weight:bold;font-size:18px;]").firstOrNull()
        if (movieNameSpan != null) {
            println("${movieNameSpan.text()} || $dateTime")
            return movieNameSpan.text()
        }
        //println("document: $document")
        movieNameSpan = document.select("span[itemprop=name]").firstOrNull { it.parent()?.attr("itemprop") == "movie" }
        val movieDateElement = document.select("time[itemprop=startDate]").first()
        val movieDate1 = movieDateElement?.attr("datetime")

        if (movieNameSpan != null) {
            println("${movieNameSpan.text()} || ${toReadableFormat(movieDate1!!)}")
            return "${movieNameSpan.text()} $movieDate1"
        }

        val movieInfoText = document.select("body").first()?.text() ?: return ""

        val movieName = movieInfoText.split("Here's your ticket info.")?.getOrNull(1)
            ?.split("Load images to see your QR code")?.getOrNull(0)
            ?.trim()

        val movieDatePattern = Regex("""\d{2}/\d{2}/\d{4} \d{2}:\d{2} [APM]{2}""")
        val movieDate = movieDatePattern.find(movieInfoText)?.value
        if (movieName != null || movieDate != null) println("$movieName || $movieDate")

        return "$movieName $movieDate"
    }

    @GetMapping("/")
    fun home(@RequestParam(name = "code", required = false) authorizationCode: String?): Any {
        //if (authorizationCode == null) return "<h1 style=\"color: green;\"> The backend service has started 😊 </h1>"

        //val CLIENTSECRETS_LOCATION = "backend-kotlin/src/main/kotlin/com/vidbox/backend/oauth2_secret.json"
        val REDIRECT_URI = "http://127.0.0.1:8081"

        val jsonFactory = GsonFactory.getDefaultInstance()
//        val clientSecretsFile = File(CLIENTSECRETS_LOCATION)
//        val inputStream = FileInputStream(clientSecretsFile)
        val inputStream = ByteArrayInputStream(gmailOauth2Secret.toByteArray())
        val clientSecrets = GoogleClientSecrets.load(
            jsonFactory,
            InputStreamReader(inputStream))

        val tokens = AuthorizationCodeTokenRequest(
            NetHttpTransport(),
            GsonFactory(),
            GenericUrl("https://oauth2.googleapis.com/token"),
            authorizationCode
        ).setRedirectUri(REDIRECT_URI).setClientAuthentication(BasicAuthentication(
            clientSecrets.details.clientId,
            clientSecrets.details.clientSecret)).execute()

        val credential = GoogleCredentials
            .newBuilder()
            .setAccessToken(AccessToken(tokens.accessToken, null))
            .build()

        val service = Gmail
            .Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                HttpCredentialsAdapter(credential))
            .setApplicationName(null)
            .build()

        // Print the labels in the user's account.
        val user = "me"
        val labels = service.users().labels().list(user).execute()
        val fandangoMessages = service.users().Messages().list(user).setQ("subject:'Fandango Purchase Confirmation'").execute()
        val atomMessages = service.users().Messages().list(user).setQ("subject:'Atom Order Confirmation'").execute()
        val messages = atomMessages.messages + fandangoMessages.messages
        //println("next page token ${messages.}")
//        val lastReceievedMsg = service.users().Messages().get(user, messages.messages[1].id).execute()
//
//        val msgData = lastReceievedMsg.payload.parts[0].body.data
//        val decodedMsg = try {
//            String(Base64.getDecoder().decode(msgData))
//        } catch (e: Exception) {
//            String(Base64.getUrlDecoder().decode(msgData))
//        }
//
//        return listOf(
//            extractMovieName(decodedMsg),
//            decodedMsg,
//            msgData,
//            messages.messages,
//        )

        val movieNames = messages.map { message ->
            val receivedMsg = service.users().Messages().get(user, message.id).execute()
            val msgData = try {
                receivedMsg.payload.parts[0].body.data
            } catch (e: Exception) {
                receivedMsg.payload.body.data
            }
            val decodedMsg = try {
                String(Base64.getDecoder().decode(msgData))
            } catch (e: Exception) {
                String(Base64.getUrlDecoder().decode(msgData))
            }
            extractMovieName(decodedMsg)
        }

        return RedirectView("https://google.com")

        return listOf(
            movieNames,
            messages
        )
        //return "<h1 style=\"color: green;\"> Tokens received: ${listResponse.labels} 😊 </h1>"
        //return RedirectView("http://localhost:3001/dashboard")
    }

    @GetMapping("/secret1")
    fun test(): String {
        return gmailService.getAuthorizationUrl()
        //return getAuthorizationUrl()
    }

    @Throws(IOException::class)
    fun accessSecretVersion(): String {
        // TODO(developer): Replace these variables before running the sample.
        val projectId = "vidbox-7d2c1"
        val secretId = "openai_secret"
        val versionId = "1"
        return accessSecretVersion(projectId, secretId, versionId)
    }

    // Access the payload for the given secret version if one exists. The version
    // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
    @Throws(IOException::class)
    fun accessSecretVersion(projectId: String?, secretId: String?, versionId: String?): String {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        SecretManagerServiceClient.create().use { client ->
            val secretVersionName = SecretVersionName.of(projectId, secretId, versionId)

            // Access the secret version.
            val response = client.accessSecretVersion(secretVersionName)

            // Verify checksum. The used library is available in Java 9+.
            val data = response.payload.data.toByteArray()
            val checksum: Checksum = CRC32C()
            checksum.update(data, 0, data.size)
            if (response.payload.dataCrc32C != checksum.getValue()) {
                System.out.printf("Data corruption detected.")
            }

            // Print the secret payload.
            //
            // WARNING: Do not print the secret in a production environment - this
            // snippet is showing how to access the secret material.
            val payload = response.payload.data.toStringUtf8()
            System.out.printf("Plaintext: %s\n", payload)
            return payload
        }
    }

    @GetMapping("/secret")
    fun getSecret(): Any {
        return accessSecretVersion()
    }

    @GetMapping("/test")
    fun getAllMovies(): Any {
        return """
            <h1> hi hows it goin </h1>
        """.trimIndent()
    }

}