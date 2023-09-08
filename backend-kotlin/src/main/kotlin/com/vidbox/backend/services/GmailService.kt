package com.vidbox.backend.services

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

@Service
class GmailService(@Value("\${gmail-oauth2-secret}") private val gmailOauth2Secret: String) {

    //private const val CLIENTSECRETS_LOCATION = gmailOauth2Secret
    private val REDIRECT_URI = "http://127.0.0.1:8081"
    private val SCOPES = listOf(
        "https://www.googleapis.com/auth/gmail.addons.current.message.metadata",
        "https://www.googleapis.com/auth/gmail.addons.current.message.readonly",
        "https://www.googleapis.com/auth/gmail.labels",
        "https://www.googleapis.com/auth/gmail.readonly"
    )

    open class GetCredentialsException(val authorizationUrl: String) : Exception()
    class CodeExchangeException(authorizationUrl: String) : GetCredentialsException(authorizationUrl)
    class NoRefreshTokenException(authorizationUrl: String) : GetCredentialsException(authorizationUrl)
    class NoUserIdException : Exception()

    // TODO: Implement these functions to work with your database.
    fun getStoredCredentials(userId: String): Nothing = throw NotImplementedError()
    fun storeCredentials(userId: String, credentials: String): Nothing = throw NotImplementedError()

    fun getAuthorizationUrl(): String {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        //val clientSecretsFile = File(CLIENTSECRETS_LOCATION)
        //val inputStream = FileInputStream(gmailOauth2Secret)
        val inputStream = ByteArrayInputStream(gmailOauth2Secret.toByteArray())
        val clientSecrets = GoogleClientSecrets.load(
            jsonFactory,
            InputStreamReader(inputStream)
        )
        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow
            .Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
            .setAccessType("offline")
            .build()
//    val receiver: LocalServerReceiver.Builder().setPort(8888).build()
//    val credential: Credential = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")

//    val flow = GoogleAuthorizationCodeFlow.Builder(
//        httpTransport,
//        jsonFactory,
//        clientSecrets,
//        SCOPES
//    ).setAccessType("offline").build()

        return flow.newAuthorizationUrl()
            .setRedirectUri(REDIRECT_URI)
            //.setState(state)
            .build()
    }

    fun getCredentials(authorizationCode: String, state: String): String {
        // Your logic here to handle the authorization code and state
        // ...
        return ""
    }
}