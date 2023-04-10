package com.tarush27.fbauthdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val welcomeMessageTv: TextView = findViewById(R.id.welcomeMsgTv)
        val logoutBtn: Button = findViewById(R.id.btnLogout)
        firebaseRemoteConfig = Firebase.remoteConfig
        firebaseAuth = FirebaseAuth.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                /*
                get the string from the backend and store in the text view.
                */
                Toast.makeText(this, "values received successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "values received un-successful", Toast.LENGTH_SHORT).show()
            }
            val welcomeMessage = firebaseRemoteConfig.getString("welcome_message")
            welcomeMessageTv.text = welcomeMessage
        }
        logoutBtn.setOnClickListener {

            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    /*
                    get the string from the backend and store in the text view.

                    parameter -> type -> json -> deserialize -> value in some kotlin object
                    the values in that kotlin object will be applied to the details to be shown in
                    dialog box

                    model -> values -> to be applied in dialog box's contents
                    */

                    val jsonResp = firebaseRemoteConfig.getValue("logout_alert")
                    val confirmationMessage =
                        Gson().fromJson(jsonResp.asString(), ConfirmationMessage::class.java)
                    val logoutMessage = confirmationMessage.logout_message.toString()
                    val cancelButton = confirmationMessage.btn_no.toString()
                    val confirmButton = confirmationMessage.btn_yes.toString()
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setMessage(logoutMessage)
                    dialogBuilder.setPositiveButton(confirmButton) { dialogInterface, which ->
                        firebaseAuth.signOut()
                        val intentToLoginScreen = Intent(this, LoginActivity::class.java)
                        startActivity(intentToLoginScreen)
                    }

                    dialogBuilder.setNegativeButton(cancelButton) { dialogInterface, which ->

                    }
                    dialogBuilder.show()
                } else {
                    Toast.makeText(this, "values received un-successful", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}