package com.kremlev.mlkit.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.kremlev.mlkit.R
import com.kremlev.mlkit.safe.activity.SafeMainActivity
import kotlinx.android.synthetic.main.activity_user_adding.*
import kotlinx.android.synthetic.main.activity_vault_auth.*
import kotlinx.android.synthetic.main.dialog_reset_password.*


class VaultAuth : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault_auth)
        Log.e("Life", "Vault onCreate")

        buttonClicks()

    }

    private fun buttonClicks() {
        reg_btn.setOnClickListener {
            singUpUser()
        }
        auth_btn.setOnClickListener {
            loginUser()
        }
        forget_btn.setOnClickListener {
            resetPass()
        }
    }

    private fun resetPass() {

        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_reset_password, null)
        val builder = AlertDialog
                .Builder(this)
                .setView(dialogLayout)
        val dialog_add_editText = dialogLayout.findViewById<EditText>(R.id.dialog_reset_password_et)

        val mAlertDialog = builder.show()

        mAlertDialog.dialog_reset_password_btn_reset.setOnClickListener {
            val mail = dialog_add_editText.text.toString()
            if (mail.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnCompleteListener { taskReset ->
                    if (taskReset.isSuccessful) {
                        Toast.makeText(
                                this@VaultAuth,
                                "Mail sent to $mail",
                                Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                                this@VaultAuth,
                                taskReset.exception!!.toString(),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                mAlertDialog.dismiss()
            } else {
                Toast.makeText(
                        this@VaultAuth,
                        "ENTER EMAIL",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

        //btn -
        mAlertDialog.dialog_reset_password_btn_cancel.setOnClickListener {
            Log.e("Password Reset", "Cancel button was clicked")
            mAlertDialog.dismiss()
        }
    }

    private fun singUpUser() {
        val email: String = vault_login_et.text.toString()
        val password: String = vault_pass_et.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { taskSingUP ->
                if (taskSingUP.isSuccessful) {
                    val firebaseUser = taskSingUP.result!!.user!!

                    Toast.makeText(
                            this@VaultAuth,
                            "Successful SingUp",
                            Toast.LENGTH_SHORT
                    ).show()
                    val safeIntent = Intent(this@VaultAuth, SafeMainActivity::class.java)
                    safeIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    safeIntent.putExtra("user_id", firebaseUser.uid)
                    startActivity(safeIntent)
                    finish()
                } else {
                    Toast.makeText(
                            this@VaultAuth,
                            taskSingUP.exception!!.toString(),
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                    this@VaultAuth,
                    "ENTER LOGIN AND PASSWORD",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loginUser() {
        val email: String = vault_login_et.text.toString()
        val password: String = vault_pass_et.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { taskLogin ->
                        if (taskLogin.isSuccessful) {
                            Toast.makeText(
                                    this@VaultAuth,
                                    "Successful Login",
                                    Toast.LENGTH_SHORT
                            ).show()
                            val safeIntent =
                                    Intent(this@VaultAuth, SafeMainActivity::class.java)
                            safeIntent.flags =
                                    (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            safeIntent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                            startActivity(safeIntent)
                            finish()
                        } else {
                            Toast.makeText(
                                    this@VaultAuth,
                                    taskLogin.exception!!.toString(),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        } else {
            Toast.makeText(
                    this@VaultAuth,
                    "ENTER LOGIN AND PASSWORD",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        ifUserIsLoggedIn()
    }

    private fun ifUserIsLoggedIn() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val safeIntent = Intent(this@VaultAuth, SafeMainActivity::class.java)
            startActivity(safeIntent)
            finish()
        }
    }
}

