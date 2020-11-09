package com.example.habibwhatappclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habibwhatappclone.R
import com.example.habibwhatappclone.listener.ContactsClickListener
import com.example.habibwhatappclone.util.Contact

import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact.*


class ContactAdapter(val contact: ArrayList<Contact>):
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private lateinit var clickListener: ContactsClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))
    }

    override fun getItemCount() = contact.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bindItem(contact[position], clickListener)
    }

    class ContactViewHolder(override val containerView: View): LayoutContainer,RecyclerView.ViewHolder(containerView) {
        fun bindItem(contact: Contact, listener: ContactsClickListener) {
            tv_contact_name.text = contact.name
            tv_contact_number.text = contact.phone
            itemView.setOnClickListener {
                listener.onContactCliked(contact.name, contact.phone)
            }
        }
    }

    fun setOnItemClickListener(listener: ContactsClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }
}