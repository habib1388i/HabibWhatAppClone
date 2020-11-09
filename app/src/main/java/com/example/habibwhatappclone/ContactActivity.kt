package com.example.habibwhatappclone

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habibwhatappclone.adapter.ContactAdapter
import com.example.habibwhatappclone.listener.ContactsClickListener
import com.example.habibwhatappclone.util.Contact

import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : AppCompatActivity(), ContactsClickListener {

    private val contactList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        getContacts()
        setUpList()
    }

    private fun setUpList() {
        progress_layout.visibility = View.GONE
        val contactAdapter = ContactAdapter(contactList)
        contactAdapter.setOnItemClickListener(this)
        rv_contacts.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contactAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun getContacts() {
        progress_layout.visibility = View.VISIBLE
        contactList.clear()
        val newList = ArrayList<Contact>()
        val phone = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)

        while (phone!!.moveToNext()) {
            val name = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            newList.add(Contact(name, number))
        }
        contactList.addAll(newList)
        phone.close()
    }

    override fun onContactCliked(name: String?, phone: String?) {
        val intent = Intent()
        intent.putExtra(MainActivity.PARAM_NAME, name)
        intent.putExtra(MainActivity.PARAM_PHONE, phone)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
