package com.example.habibwhatappclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.habibwhatappclone.adapter.SectionPagerAdapter
import com.example.habibwhatappclone.fragment.ChatsFragment
import com.example.habibwhatappclone.listener.FailurCallback
import com.example.habibwhatappclone.util.DATA_USERS
import com.example.habibwhatappclone.util.DATA_USER_PHONE
import com.example.habibwhatappclone.util.PERMISSION_REQUEST_READ_CONTACT
import com.example.habibwhatappclone.util.REQUEST_NEW_CHAT
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FailurCallback {

    private lateinit var mSectionPagerAdapter: SectionPagerAdapter
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaeDb = FirebaseFirestore.getInstance()
    private val chatsFragment = ChatsFragment()

    companion object {
        const val PARAM_NAME = "name"
        const val PARAM_PHONE = "phone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chatsFragment.setFailureCallbackListener(this)

        setSupportActionBar(toolbar)
        mSectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)

        container.adapter = mSectionPagerAdapter
        fab.setOnClickListener {
            onNewChat()
        }

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTabs()
        tabs.getTabAt(1)?.select()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fab.hide()
                    1 -> fab.show()
                    2 -> fab.hide()
                }
            }

        })
    }

    private fun onNewChat() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Contact Permission")
                    .setMessage("This app requires access to your contacts to initiation a conversation")
                    .setPositiveButton("ok") { dialogInterface, i ->
                        requestContactsPermision()
                    }
                    .setNegativeButton("no") { dialogInterface, i ->

                    }
                    .show()
            } else {
                requestContactsPermision()
            }
        } else {
            startNewActivity()
        }
    }

    private fun requestContactsPermision() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSION_REQUEST_READ_CONTACT
        )
    }

    private fun startNewActivity() {
        startActivityForResult(Intent(this, ContactActivity::class.java), REQUEST_NEW_CHAT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_NEW_CHAT) {
            val name = data?.getStringExtra(PARAM_NAME) ?: ""
            val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
            CheckNewChatUser(name, phone)
        }
    }

    private fun CheckNewChatUser(name: String, phone: String) {
        if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
            firebaeDb.collection(DATA_USERS).whereEqualTo(DATA_USER_PHONE, phone).get()
                .addOnSuccessListener {
                    if (it.documents.size > 0) {
                        chatsFragment.newChat(it.documents[0].id)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("User not found")
                            .setMessage("$name doesn't have an account. Send then ab SNS to install this app")
                            .setPositiveButton("Ok") { dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra(
                                    "sms_body",
                                    "Hi i'm using thes new cool WhatsAppCloneApp you should install it too. So we can chat there"
                                )
                                startActivity(intent)
                            }.setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show()

                    }

                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "An error occured. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                    it.printStackTrace()
                }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startNewActivity()
                }
            }
        }
    }

    private fun resizeTabs() {
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> onProfile()
            R.id.action_logout -> onLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun onProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }


    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun userError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
