package com.example.habibwhatappclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.habibwhatappclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var imagerUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (userId.isNullOrEmpty()) {
            finish()
        }

        btn_camera.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }

        progress_layout.setOnTouchListener { view, motionEvent -> true }

        btn_apply.setOnClickListener {
            onApply()
        }

        btn_delete_account.setOnClickListener {
            onDelete()
        }

        populateInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            storeImage(data?.data)
        }
    }

    private fun storeImage(data: Uri?) {
        if (data != null) {
            Toast.makeText(this, "Uploading image", Toast.LENGTH_SHORT).show()
            progress_layout.visibility = View.VISIBLE

            // membuat folder
            val filePath = firebaseStorage.child(DATA_IMAGE).child(userId!!)
            filePath.putFile(data)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString()
                            firebaseDb.collection(DATA_USERS)
                                .document(userId)
                                .update(DATA_USER_IMAGE_URL, url)
                                .addOnCompleteListener {
                                    imagerUrl = url
                                    populateImage(this, imagerUrl, img_profile, R.drawable.ic_user)
                                }
                            progress_layout.visibility = View.GONE
                        }.addOnFailureListener {
                            onUploadFailur()
                        }
                }.addOnFailureListener {
                    onUploadFailur()
                }
        }
    }

    private fun onUploadFailur() {
        progress_layout.visibility = View.GONE
        Toast.makeText(this, "Image upload failed. Please try again later", Toast.LENGTH_SHORT)
            .show()
    }

    private fun populateInfo() {
        progress_layout.visibility = View.VISIBLE
        firebaseDb.collection(DATA_USERS).document(userId!!).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            imagerUrl = user?.imageUrl
            edt_name_profile.setText(user?.name, TextView.BufferType.EDITABLE)
            edt_email_profile.setText(user?.email, TextView.BufferType.EDITABLE)
            edt_phone_profile.setText(user?.phone, TextView.BufferType.EDITABLE)
            if (imagerUrl != null) {
                populateImage(this, user?.imageUrl, img_profile, R.drawable.ic_user)
            }
            progress_layout.visibility = View.GONE
        }.addOnFailureListener {
            it.printStackTrace()
            finish()
        }
    }

    private fun onApply() {
        progress_layout.visibility = View.VISIBLE
        val name = edt_name_profile.text.toString()
        val email = edt_email_profile.text.toString()
        val phone = edt_phone_profile.text.toString()    // data text dalam EditText akan diubah
        val map = HashMap<String, Any>()                 // menjadi String lalu ditampung di variabel
        map[DATA_USER_NAME] = name                       // yang nantinya di koleksi oleh HashMap
        map[DATA_USER_EMAIL] = email                     // untuk kemudian dikirimkan ke table user
        map[DATA_USER_PHONE] = phone                     // di database Firebase sebagai pembaruan
        firebaseDb.collection(DATA_USERS).document(userId!!).update(map) // perintah update
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                progress_layout.visibility = View.GONE
            }
    }
    private fun onDelete() {
        progress_layout.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This will delete your Profile Information, Are you sure?")
            .setPositiveButton("Yes") { dialog, which ->
                firebaseDb.collection(DATA_USERS).document(userId!!).delete()
                firebaseStorage.child(DATA_IMAGE).child(userId).delete()
                firebaseAuth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        finish()
                    }?.addOnFailureListener {
                        finish()
                    }

            }.setNegativeButton("No") { dialog, which ->
                progress_layout.visibility = View.GONE

            }.setCancelable(false).show()
    }
}