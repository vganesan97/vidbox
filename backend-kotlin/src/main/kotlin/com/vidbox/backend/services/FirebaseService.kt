package com.vidbox.backend.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.FileInputStream
import javax.servlet.http.HttpServletRequest

@Service
class FirebaseService {
    val filePath = "/app/vidbox-7d2c1-firebase-adminsdk-akp4p-f90c0efd75.json"
    val serviceAccount = FileInputStream(filePath)
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://vidbox-7d2c1.firebaseio.com")
        .build()

    val firebaseApp = FirebaseApp.initializeApp(options)

    fun getUidFromFirebaseToken(idToken: String? = null, request: HttpServletRequest? = null): String {
        val uid: String
        var token = idToken
        if (request != null) token = request.getHeader("Authorization").substring(7)

        if (token == null) throw IllegalArgumentException("idToken and request cannot both be null")

        // Extract the token from the Authorization header
        try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
            uid = decodedToken.uid
            // The token is valid, and we have the user's UID
        } catch (e: FirebaseAuthException) {
            // Handle error: The ID token was invalid
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to authenticate user.")
        }
        return uid
    }

}
