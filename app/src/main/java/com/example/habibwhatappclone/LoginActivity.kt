package com.example.habibwhatappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setTextChangedListener(edt_email_login,til_email_login)
        setTextChangedListener(edt_password_login,til_email_login)
        progress_layout_login.setOnTouchListener { v, event -> true  }

        btn_login.setOnClickListener {
            onLogin()
        }

        txt_signup.setOnClickListener {
            onSignup()
        }
    }

    private fun setTextChangedListener(edt: TextInputEditText?, til: TextInputLayout?) {
        edt?.addTextChangedListener( object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                // fungsi ketika text dalam editText setelah diubah
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // fungsi ketika text dalam editText sebelum diubah
            }

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // fungsi ketika text dalam editText sedang diubah
                // textInputLayout tidak menunjukan pesan error
                til?.isErrorEnabled = false
            }
        })
    }

    private fun onLogin(){
        var procced = true
        if (edt_email_login.text.isNullOrEmpty()){
            til_email_login.error = "Required Email"
            til_email_login.isErrorEnabled = true
            procced = false
        }

        if (edt_password_login.text.isNullOrEmpty()){
            til_password_login.error = "Required Password"
            til_password_login.isErrorEnabled = true
            procced = false
        }

        if (procced){
         progress_layout_login.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(
                edt_email_login.text.toString(),
                edt_password_login.text.toString()
            ).addOnCompleteListener {
                 if (!it.isSuccessful){
                    progress_layout_login.visibility = View.GONE
                    Toast.makeText(this, "Login Error: ${it.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                    progress_layout_login.visibility = View.GONE
                    it.printStackTrace()
                }
        }
    }

    override fun onStart(){
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop(){
        super.onStop()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    private fun onSignup() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}