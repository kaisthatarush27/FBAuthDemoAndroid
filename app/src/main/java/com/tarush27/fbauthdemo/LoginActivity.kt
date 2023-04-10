package com.tarush27.fbauthdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginWithGoogleBtn: SignInButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        firebaseAuth = FirebaseAuth.getInstance()
        initializeUi()

    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // ...

            val intentToMainActivity = Intent(this, MainActivity::class.java)
            startActivity(intentToMainActivity)
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
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
        val userEmail = email.text.toString()
        val userPwd = password.text.toString()

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "plz enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (userPwd.isEmpty()) {
            Toast.makeText(this, "plz enter password", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(userEmail, userPwd).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "login successful", Toast.LENGTH_SHORT).show()
                val intentToMainActivity = Intent(this, MainActivity::class.java)
                startActivity(intentToMainActivity)
            } else {
                Toast.makeText(this, "login un successful", Toast.LENGTH_SHORT).show()
            }
        }
    }
}