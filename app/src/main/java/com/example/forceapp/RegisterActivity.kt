package com.example.forceapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.forceapp.databinding.ActivityRegisterBinding
import com.jakewharton.rxbinding2.widget.RxTextView


@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var authService = AuthService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authService = AuthService(this)

        // Username Validation
        val usernameStream = binding.etUsername.editText?.let {
            RxTextView.afterTextChangeEvents(it)
                .skipInitialValue()
                .map { event ->
                    val username = event.view().text.toString()
                    username.isEmpty()
                }
        }
        usernameStream?.subscribe { isValid ->
            showTextMinimalAlert(isValid, "Username")
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
            usernameStream,
            emailStream,
            passwordStream
        ) { usernameInvalid: Boolean, emailInvalid: Boolean, passwordInvalid: Boolean ->
            !emailInvalid && !usernameInvalid && !passwordInvalid
        }
        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.btnReg.isEnabled = true
                binding.btnReg.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.temna_sub)
            } else {
                binding.btnReg.isEnabled = false
                binding.btnReg.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.temna_modra)
            }
        }
        // On click
        binding.btnReg.setOnClickListener {
            val email = binding.etEmail.editText?.text.toString().trim()
            val username= binding.etUsername.editText?.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()
            registerUser(username, email, password)

        }
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        when (text) {
            "Username" -> binding.etUsername.error = if (isNotValid) "$text is not valid" else null
            "Password" -> binding.etPassword.error = if (isNotValid) "$text is not valid" else null
            "email" -> binding.etEmail.error = if (isNotValid) "$text is not valid" else null
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        // Check username length
        if (username.length < 3) {
            Toast.makeText(
                applicationContext,
                "Username must be at least 3 characters long.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Check email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(applicationContext, "Invalid email format.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check password length and complexity
        val passwordRegex = ".*\\d.*"
        val uppercaseRegex = ".*[A-Z].*"
        val specialCharRegex = ".*[!@#$%^&*()-+=`~\\[\\]{}|\\\\;:'\",<.>/?].*"
        if (password.length < 14 || !password.matches(passwordRegex.toRegex()) || !password.matches(
                uppercaseRegex.toRegex()
            ) || !password.matches(specialCharRegex.toRegex())
        ) {
            Toast.makeText(
                applicationContext,
                "Password must be at least 14 characters long and contain at least one uppercase letter, one digit, and one special character.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        authService.register(username, email, password, object : AuthService.LoginCallback {
            override fun onSuccess() {
                // Registration successful
                Toast.makeText(
                    applicationContext,
                    "User registered successfully",
                    Toast.LENGTH_SHORT
                ).show()
                // Save the username in SharedPreferences
                val sharedPreferences =
                    applicationContext.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.apply()

                // Start the LoginActivity
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finish()


            }

            override fun onFailure(errorMessage: String) {
                // Registration failed
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
