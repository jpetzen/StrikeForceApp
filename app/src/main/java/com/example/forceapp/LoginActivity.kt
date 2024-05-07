package com.example.forceapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.forceapp.databinding.ActivityLoginBinding
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class SharedPrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    companion object {
        const val backendURL = "http://192.168.240.231:8000"
    }

    // Function to check if the user is logged in
    fun isUserLogged(): Boolean {
        val email = sharedPreferences.getString("EMAIL", "")
        return email?.isNotEmpty() == true
    }

    // Function to log out the user
    fun logout() {
        editor.clear()
        editor.apply()
        // Clear session on the server
        clearSession()
    }
    private fun clearSession() {
        // Start a coroutine on the IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${backendURL}/clear_session/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Use Log.d to log successful response
                    Log.d("ClearSession", "Session cleared successfully")
                } else {
                    // Use Log.d to log failure response
                    Log.d("ClearSession", "Failed to clear session: $responseCode")
                }
            } catch (e: Exception) {
                // Use Log.e to log exceptions
                Log.e("ClearSession", "Exception: ${e.message}", e)
            }
        }
    }
}

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private var authService = AuthService(this)

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefManager = SharedPrefManager(this)
        authService = AuthService(this)
        val token = authService.getAccessToken()
        if (token != null) {
            authService.getCurrentUser(token)
            Log.d(authService.getCurrentUser(token).toString(), authService.getCurrentUser(token).toString())
        }
        // Check if user is logged in
        if (sharedPrefManager.isUserLogged()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Email Validation
        val emailStream = binding.etEmail.editText?.let {
            RxTextView.afterTextChangeEvents(it)
                .skipInitialValue()
                .map { event ->
                    val email = event.view().text.toString()
                    email.isEmpty()
                }
        }
        emailStream?.subscribe { isValid ->
            showTextMinimalAlert(isValid, "Email")
        }

        // Password Validation
        val passwordStream = binding.etPassword.editText?.let {
            RxTextView.afterTextChangeEvents(it)
                .skipInitialValue()
                .map { event ->
                    val password = event.view().text.toString()
                    password.isEmpty()
                }
        }
        passwordStream?.subscribe { isValid ->
            showTextMinimalAlert(isValid, "Password")
        }

        // Button enable true/false
        val invalidFieldStream = io.reactivex.Observable.combineLatest(
            emailStream,
            passwordStream
        ) { emailInvalid: Boolean, passwordInvalid: Boolean ->
            !emailInvalid && !passwordInvalid
        }
        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.btnWebLogin.isEnabled = true
                binding.btnWebLogin.setTextColor(ContextCompat.getColor(this, R.color.white))} else {
                binding.btnWebLogin.isEnabled = false
                binding.btnWebLogin.setTextColor(ContextCompat.getColor(this, R.color.temna_sub))}
        }

        binding.btnWebLogin.setOnClickListener {
            val email = binding.etEmail.editText?.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()

            CoroutineScope(Dispatchers.IO).launch {
                val token = authService.authenticateUser(email, password)
                withContext(Dispatchers.Main) {
                    if (token != null) {
                        sharedPrefManager.editor.putString("EMAIL", email)
                        sharedPrefManager.editor.apply()

                        // Start OtherActivity
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        when (text) {
            "Email" -> binding.etEmail.error = if (isNotValid) "$text field can't be empty!" else null
            "Password" -> binding.etPassword.error = if (isNotValid) "$text field can't be empty!" else null
        }
    }
}
