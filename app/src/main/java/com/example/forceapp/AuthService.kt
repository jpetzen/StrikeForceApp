package com.example.forceapp

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest


class AuthService(private val context: Context) {
    val backendURL = ApiService.Constants.backendURL
    private val client = OkHttpClient()

    fun register(username: String, email: String, password: String, callback: LoginCallback) {
        val jsonBody = JSONObject()
        val hashedPWD=hashPassword(password)
        jsonBody.put("username", username)
        jsonBody.put("email", email)
        jsonBody.put("password", hashedPWD)
        jsonBody.put("role", "normal_user")

        val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${backendURL}/sign-up")
            .post(body)
            .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            (context as Activity).runOnUiThread {
                callback.onFailure(e.message ?: "Unknown error")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                println("Response code: ${response.code}")
                println("Response body: ${response.body?.string()}")
                (context as Activity).runOnUiThread {
                    callback.onFailure("Failed to register. Please try again.")
                }
                return
            }

            try {
                val jsonResponse = JSONObject(response.body!!.string())
                (context as Activity).runOnUiThread {
                    callback.onSuccess()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                (context as Activity).runOnUiThread {
                    callback.onFailure("Error parsing server response.")
                }
            }
        }
    })
}


fun authenticateUser(email: String, password: String): String? {
    val hashedPassword = hashPassword(password).toString()
    val url = "${backendURL}/auth_user"

    Log.d("AuthenticateUser", "Email: $email")
    Log.d("AuthenticateUser", "Hashed Password: $hashedPassword")

    val client = OkHttpClient()

    val jsonBody = JSONObject()
    jsonBody.put("email", email)
    jsonBody.put("password", hashedPassword)

    val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val jsonResponse = JSONObject(responseBody)
        val tokenStr = jsonResponse.getString("token")
        // Save the access token
        saveAccessToken(tokenStr)
        getCurrentUser(tokenStr)
        return tokenStr
    }
}

    fun getUserInfo(token: String): JSONObject? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$backendURL/get-user-info")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null

            val responseBody = response.body?.string() ?: return null
            return JSONObject(responseBody)
        }
    }

    interface LoginCallback {
        fun onSuccess()
        fun onFailure(errorMessage: String)
    }

    private fun saveAccessToken(token: String) {
        val sharedPreferences =
            context.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("accessToken", token)
        editor.apply()
    }

    fun getCurrentUser(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val userInfo = getUserInfo(token)
            val username = userInfo?.getString("username")
            if (username != null) {
                Log.d(username,"username pri getCurrentUser")
            }
            if (username != null) {
                withContext(Dispatchers.Main) {
                    saveUsername(username)
                }
            }
        }
    }

    private fun saveUsername(username: String) {
        val sharedPreferences = context.applicationContext.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.apply()
        Log.d("SaveUsername", "Saved username: $username")
    }

    fun getAccessToken(): String? {
        val sharedPreferences = context.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("accessToken", null)
    }

    fun hashPassword(password: String): String? {
        return try {
            // Create a SHA-512 hash object
            val md = MessageDigest.getInstance("SHA-512")

            // Update the hash object with the password bytes
            val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))

            // Convert the byte array to a hexadecimal string
            val stringBuilder = StringBuilder()
            for (b in hashedBytes) {
                stringBuilder.append(String.format("%02x", b))
            }

            stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
