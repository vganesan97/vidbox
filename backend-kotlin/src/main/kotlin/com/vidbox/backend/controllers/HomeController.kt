package com.vidbox.backend.controllers

import com.google.cloud.secretmanager.v1.*
import com.google.cloud.spring.secretmanager.SecretManagerTemplate
import com.google.cloud.storage.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*
import java.util.zip.CRC32C
import java.util.zip.Checksum


@RestController
class HomeController() {

    @Autowired
    private val secretManagerTemplate: SecretManagerTemplate? = null

    @Value("\${sm://openai_secret}")
    private val appSecret: String? = null

    @GetMapping("/")
    fun home(): Any {
        return """
        <h1 style="color: green;"> The backend service has started 😊 </h1>
    """.trimIndent()
    }

    @Value("\${my-app-secret-2}")
    private val myAppSecret: String? = null

    @GetMapping("/secret1")
    fun test(): String {
        val x = secretManagerTemplate?.getSecretString("openai_secret")
        return "$myAppSecret $appSecret $x"
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
            if (response.payload.dataCrc32C !== checksum.getValue()) {
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