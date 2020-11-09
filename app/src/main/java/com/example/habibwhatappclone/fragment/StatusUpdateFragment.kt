package com.example.habibwhatappclone.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.habibwhatappclone.R
import com.example.habibwhatappclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_status_update.*

/**
 * A simple [Fragment] subclass.
 */
class StatusUpdateFragment : Fragment() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress_layout.setOnTouchListener { _, _ -> true }
        fab_status.setOnClickListener { onUpdate() }
        populateImage(context, imageUrl, img_status_update)

        lay_status.setOnClickListener {
            if (isAdded) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    private fun storeImage(imageUri: Uri?) {
        if (imageUri != null && userId != null) {
            Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show()
            progress_layout.visibility = View.VISIBLE
            // menyimpan gambar pada status di firebase storage dengan nama tambahan _status
            val filePath = firebaseStorage.child(DATA_IMAGE).child("${userId}_status")

            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener { taskSnapshot ->
                            val url = taskSnapshot.toString()

                            firebaseDb.collection(DATA_USERS) // mengakses table data_user
                                .document(userId)             // memperbarui data status Url sesuai data dari file gambar
                                .update(DATA_USER_STATUS, url)
                                .addOnSuccessListener {
                                    imageUrl = url     // mengisi property imageUrl dengan url gambar
                                    populateImage(context, imageUrl, img_status_update)
                                }
                            progress_layout.visibility = View.GONE
                        }
                        .addOnFailureListener { onUploadFailure() }
                }
                .addOnFailureListener { onUploadFailure() }
        }
    }

    private fun onUpdate() {
        progress_layout.visibility = View.VISIBLE
        val map = HashMap<String, Any>()
        map[DATA_USER_STATUS] = edt_status_update.text.toString()   // data status
        map[DATA_USER_STATUS_URL] = imageUrl                        // data status Url
        map[DATA_USER_STATUS_TIME] = getTime()                      // data status Time

        firebaseDb.collection(DATA_USERS)
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                progress_layout.visibility = View.GONE
                Toast.makeText(activity, "Status updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progress_layout.visibility = View.GONE
                Toast.makeText(activity, "status update failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onUploadFailure() {
        Toast.makeText(activity, "image upload failed. please try again later", Toast.LENGTH_SHORT).show()
        progress_layout.visibility = View.GONE
    }

}
