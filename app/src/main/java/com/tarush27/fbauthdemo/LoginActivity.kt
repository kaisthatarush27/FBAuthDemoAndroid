package com.tarush27.fbauthdemo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.SignInButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var emailError: TextInputLayout
    private lateinit var passwordError: TextInputLayout
    private lateinit var loginWithGoogleBtn: SignInButton
    private lateinit var simpleDateFormat: SimpleDateFormat
    private lateinit var sharedPreferences: SharedPreferences
    private var isUserLoggedInWithEmail = false
    private var isUserLoggedInWithGoogle = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("FBAuth", MODE_PRIVATE)
        isUserLoggedInWithEmail =
            sharedPreferences.getBoolean("loginOccurredUsingEmail", isUserLoggedInWithEmail)
        isUserLoggedInWithGoogle =
            sharedPreferences.getBoolean("loginOccurredUsingGoogle", isUserLoggedInWithGoogle)
        if (isUserLoggedInWithEmail) {
            val intentToMainActivity = Intent(this, MainActivity::class.java)
            startActivity(intentToMainActivity)
            finish()
        }

        if (isUserLoggedInWithGoogle) {
            val intentToMainActivity = Intent(this, MainActivity::class.java)
            startActivity(intentToMainActivity)
            finish()
        }
        setContentView(R.layout.login_activity)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = Firebase.analytics
        initializeUi()
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            simpleDateFormat = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault())
            val googleLoginTimeStamp = simpleDateFormat.format(Date())
            firebaseAnalytics.logEvent("Login") {
                param("event_name", "Login")
                param("event_time", googleLoginTimeStamp)
                param("login_method", "Google")
            }
            sharedPreferences = getSharedPreferences("FBAuth", MODE_PRIVATE)
            val sharedPrefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
            isUserLoggedInWithGoogle = true
            sharedPrefsEditor.putBoolean("loginOccurredUsingGoogle", isUserLoggedInWithGoogle)
            sharedPrefsEditor.apply()
            finish()
            val intentToMainActivity = Intent(this, MainActivity::class.java)
            startActivity(intentToMainActivity)
        } else {
            simpleDateFormat = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault())
            val googleLoginFailedTimeStamp = simpleDateFormat.format(Date())
            firebaseAnalytics.logEvent("Login_Failed") {
                param("event_name", "LoginFailed")
                param("event_time", googleLoginFailedTimeStamp)
                param("login_method", "Google")
                param("error_msg", "${result.idpResponse?.error?.message}")
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()


    private fun initializeUi() {
        email = findViewById(R.id.txtEmail)
        password = findViewById(R.id.txtPwd)
        emailError = findViewById(R.id.txtUserNameEmail)
        passwordError = findViewById(R.id.txtEnterYourPwd)
        loginWithGoogleBtn = findViewById(R.id.googleSignInBtn)
        val loginBtn: Button = findViewById(R.id.loginButton)
        loginBtn.setOnClickListener {
            loginUsersAccount()
        }
        loginWithGoogleBtn.setOnClickListener {
            loginWithGoogle()
        }
    }

    private fun loginWithGoogle() {
        signInLauncher.launch(signInIntent)
    }

    private fun loginUsersAccount() {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault())
        val loginTimeStamp = sdf.format(Date())
        val loginFailedTimeStamp = sdf.format(Date())
        val userEmail = email.text.toString()
        val userPwd = password.text.toString()

        if (userEmail.isEmpty()) {
            emailError.error = "PLease enter email"
            return
        }



        if (userPwd.isEmpty()) {
            passwordError.error = "PLease enter password"
            return
        }

        firebaseAuth.signInWithEmailAndPassword(userEmail, userPwd).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseAnalytics.logEvent("Login") {
                    param("event_name", "Login")
                    param("event_time", loginTimeStamp)
                    param("user_email", userEmail)
                    param("login_method", "Email")
                }
                sharedPreferences = getSharedPreferences("FBAuth", MODE_PRIVATE)
                val sharedPrefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
                isUserLoggedInWithEmail = true
                sharedPrefsEditor.putBoolean("loginOccurredUsingEmail", isUserLoggedInWithEmail)
                sharedPrefsEditor.apply()
                val intentToMainActivity = Intent(this, MainActivity::class.java)
                startActivity(intentToMainActivity)
                finish()
            } else {
                firebaseAnalytics.logEvent("Login_Failed") {
                    param("event_name", "LoginFailed")
                    param("event_time", loginFailedTimeStamp)
                    param("user_email", userEmail)
                    param("login_method", "Email")
                    param("error_msg", "${task.exception?.message}")
                }
            }
        }
    }
}