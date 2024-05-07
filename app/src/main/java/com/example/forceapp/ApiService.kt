package com.example.forceapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
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


class ApiService(context: Context) {
    private var client: OkHttpClient
    private val authService: AuthService = AuthService(context)
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()
    companion object {
        const val backendURL = "http://192.168.240.231:8000"
    }

    init {
        val clientBuilder = OkHttpClient.Builder()

        // Add the authentication interceptor
        clientBuilder.addInterceptor(AuthInterceptor(authService))

        // Build the OkHttpClient
        client = clientBuilder.build()

        this.sharedPreferences = context.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
    }

    /*
    EVIDENCA
    */

    // fragmet ADD
    fun fetchEvidenca(): List<String> {
        val request = Request.Builder()
            .url("${backendURL}/opravila/")
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseData = response.body?.string()
        Log.d("Successfully loaded the table from the database.", responseData.toString())
        return gson.fromJson(responseData, Array<String>::class.java).toList()
    }


    fun submitData(context: Context, opravilo: String, done: Boolean, datum: String) {
        val sharedPreferences = context.applicationContext.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val currentUsername = sharedPreferences.getString("username", "")
        Log.d(currentUsername,currentUsername.toString())
        val requestBody = JSONObject()
        try {
            requestBody.put("user_username", currentUsername)
            requestBody.put("opravilo", opravilo)
            requestBody.put("done", done)
            requestBody.put("datum", datum)
        } catch (e: JSONException) {
            e.printStackTrace()
            return
        }
        val json = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = requestBody.toString().toRequestBody(json)
            Log.d("ApiService", "Data being sent: $requestBody")
        val request = Request.Builder()
            .url("${backendURL}/evidenca")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("Network", "Failed to submit data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("Network", "Failed to submit data: ${response.message}")
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Successfully added.", Toast.LENGTH_SHORT).show()
                    }
                    Log.i("Network", "Data submitted successfully")
                }
            }
        })
    }

    // fragment SHOW

    fun getEvidenca(param: (List<Opravila>) -> Unit) {
        val request = Request.Builder()
            .url("${backendURL}/evidenca/")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("Network", "Failed to get data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("Network", "Failed to get data: ${response.message}")
                } else {
                    val responseData = response.body?.string()
                    Log.i("Network", "Data received successfully: $responseData")
                    val opravilaList = gson.fromJson(responseData, Array<Opravila>::class.java).toList()
                    param(opravilaList)
                }
            }
        })
    }

    /*
    SREDSTVA
    */

    // fragmet ADD
    fun fetchCistila(): List<String> {
        val request = Request.Builder()
            .url("${backendURL}/cistila/")
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseData = response.body?.string()
        Log.d("Successfully loaded the table from the database.", responseData.toString())
        return gson.fromJson(responseData, Array<String>::class.java).toList()
    }


    fun submitData(context: Context, cistila: String, stevilo: Int, denar: Double, selectedDate: String) {
        val sharedPreferences = context.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val currentUsername = sharedPreferences.getString("username", "")

        val requestBody = JSONObject()
        try {
            requestBody.put("user_username", currentUsername)
            requestBody.put("cistila", cistila)
            requestBody.put("stevilo", stevilo)
            requestBody.put("denar", denar)
            requestBody.put("date", selectedDate)
        } catch (e: JSONException) {
            e.printStackTrace()
            return
        }

        val client = OkHttpClient()
        val json = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = requestBody.toString().toRequestBody(json)
        Log.d("ApiService", "Data being sent: $requestBody")
        val request = Request.Builder()
            .url("${backendURL}/sredstva")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("Network", "Failed to submit data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("Network", "Failed to submit data: ${response.message}")
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Successfully added.", Toast.LENGTH_SHORT).show()
                    }
                    Log.i("Network", "Data submitted successfully")
                }
            }
        })
    }

    // fragment SHOW

    fun getSredstva(callback: (List<Sredstva>) -> Unit) {
        val request = Request.Builder()
            .url("${backendURL}/sredstva/")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("Network", "Failed to get data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("Network", "Failed to get data: ${response.message}")
                } else {
                    val responseData = response.body?.string()
                    Log.i("Network", "Data received successfully: $responseData")
                    val sredstvaList = gson.fromJson(responseData, Array<Sredstva>::class.java).toList()
                    callback(sredstvaList)
                }
            }
        })
    }

}