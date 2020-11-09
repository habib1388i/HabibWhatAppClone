package com.example.habibwhatappclone

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habibwhatappclone.adapter.ConversationAdapter
import com.example.habibwhatappclone.util.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {
    // nah kalau ini objek nya berfungsi untuk mengambil uid pada user
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    // untuk mengakses fire store
    private val firebaseDb = FirebaseFirestore.getInstance()
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null
    private var phone: String? = null

    companion object {
        // jadi setiap kalian membuat new chat nah disitu dia mengcreate id unik di firebase
        private val PARAM_CHAT_ID = "Chat_id"
        // ini untuk image url untuk menampilkan profile user di chat
        private val PARAM_IMAGE_URL = "Image_url"
        // jadi ini ngechat ke id lain
        private val PARAM_OTHER_USER_ID = "Other_user_id"
        //untuk nama user yang disave di contac
        private val PARAM_CHAT_NAME = "Chat_name"

        fun newIntent(
            context: Context?,
            chatId: String?,
            imageUrl: String?,
            otherUserId: String?,
            chatName: String?
        ): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID, chatId)
            intent.putExtra(PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID, otherUserId)
            intent.putExtra(PARAM_CHAT_NAME, chatName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        setSupportActionBar(toolbar_conversation)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_conversation.setNavigationOnClickListener {
            onBackPressed()
        }
        chatId = intent.extras?.getString(PARAM_CHAT_ID)
        imageUrl = intent.extras?.getString(PARAM_IMAGE_URL)
        chatName = intent.extras?.getString(PARAM_CHAT_NAME)
        otherUserId = intent.extras?.getString(PARAM_OTHER_USER_ID)

        if (chatId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chat Room Error", Toast.LENGTH_SHORT).show()
            finish()
        }
// untuk menampilkan gambar user yang dichat
        populateImage(this, imageUrl, img_toolbar, R.drawable.ic_user)
        // nama useryang dichat
        txt_toolbar.text = chatName
        // recyler view untuk item chat
        rv_message.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
// untuk menambahkan chat pada firebase
        firebaseDb.collection(DATA_CHATS)
            .document(chatId!!)
            .collection(DATA_CHAT_MESSAGE).orderBy(DATA_CHAT_MESSAGE_TIME)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    firebaseFirestoreException.printStackTrace()
                    return@addSnapshotListener
                } else {
                    if (querySnapshot != null) {
                        for (change in querySnapshot.documentChanges) { // ngelooping data dari DATA_CHAT_MESSAGE

                            when (change.type) {
                                DocumentChange.Type.ADDED -> {
                                    val message =
                                        change.document.toObject(Message::class.java) //data ditampnug
                                    if (message != null) {
                                        conversationAdapter.addMessage(message) // adapter menambah pesan
                                        rv_message.post {
                                            // itemCount - 1 berarti di urutkan dari data pertama
                                            rv_message.smoothScrollToPosition(conversationAdapter.itemCount - 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
//         buttom untuk mengirim pesan
        imbtn_send.setOnClickListener {
            if (!edt_message.text.isNullOrEmpty()) {
                val message =
                    Message(userId, edt_message.text.toString(), System.currentTimeMillis())

                firebaseDb.collection(DATA_CHATS)   // menambahkan data message ke dalam
                    .document(chatId!!)             // database pada table Chats
                    .collection(DATA_CHAT_MESSAGE)
                    .document()
                    .set(message)
                edt_message.setText(
                    "",
                    TextView.BufferType.EDITABLE
                ) // membersihkan EditText
            }
        }

        // fungsi call
        firebaseDb.collection(DATA_USERS).document(userId!!).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            phone = user?.phone
        }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_conversation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.action_call -> {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}


