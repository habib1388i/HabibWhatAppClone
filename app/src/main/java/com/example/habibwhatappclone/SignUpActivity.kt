package com.example.habibwhatappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.habibwhatappclone.util.DATA_USERS
import com.example.habibwhatappclone.util.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseListener = FirebaseAuth.AuthStateListener {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setTextChangedListener(edt_name, til_name)
        setTextChangedListener(edt_phone, til_phone)
        setTextChangedListener(edt_email, til_email)
        setTextChangedListener(edt_password, til_password)
        progress_layout.setOnTouchListener { v, event -> true }

        btn_singup.setOnClickListener {
            onSingUp()
        }

        tv_login.setOnClickListener {
            onLogin()
        }
    }

    private fun setTextChangedListener(edt: TextInputEditText?, til: TextInputLayout?) {
        edt?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til?.isErrorEnabled = true
            }
        })
    }

    private fun onSingUp() {
        // Kondisi proceed harus true apabila EditText ter isi
        var proceed = true
        if (edt_name.text.isNullOrEmpty()) {
            til_name.error = "Please fill column Name"
            til_name.isErrorEnabled = true
            // Proceed akan flase apa bila kolom EditText tidak diisi
            proceed = false
        }

        if (edt_phone.text.isNullOrEmpty()) {
            til_phone.error = "Please fill column phone number"
            til_name.isErrorEnabled = true
            proceed = false
        }

        if (edt_email.text.isNullOrEmpty()) {
            til_email.error = "Please fill column email"
            til_name.isErrorEnabled = true
            proceed = false
        }

        if (edt_password.text.isNullOrEmpty()) {
            til_password.error = "Please fill column password"
            til_name.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            progress_layout.visibility = View.GONE
            firebaseAuth.createUserWithEmailAndPassword(
                edt_email.text.toString(),
                edt_password.text.toString()
            ).addOnCompleteListener {
                if (!it.isSuccessful) {
                    progress_layout.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "SingUp Error: ${it.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (firebaseAuth.uid != null) {
                    val name = edt_name.text.toString()
                    val phone = edt_phone.text.toString()
                    val email = edt_email.text.toString()
                    val user = User(email, phone, name, "", "Hello world! i'm new", "", "")
                    firebaseDb.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                }
                progress_layout.visibility = View.GONE
            }.addOnFailureListener {
                progress_layout.visibility = View.GONE
                it.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseListener)
    }

    private fun onLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}