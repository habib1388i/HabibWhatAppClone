package com.example.habibwhatappclone.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habibwhatappclone.R
import com.example.habibwhatappclone.StatusActivity.Companion.getIntent
import com.example.habibwhatappclone.adapter.StatusListAdapter
import com.example.habibwhatappclone.listener.StatusItemClickListener
import com.example.habibwhatappclone.util.DATA_USERS
import com.example.habibwhatappclone.util.DATA_USER_CHATS
import com.example.habibwhatappclone.util.StatusListElement
import com.example.habibwhatappclone.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_status_list.*


class StatusListFragment : Fragment(), StatusItemClickListener {
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
// Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onItemClickListener(statusElement: StatusListElement) {
        startActivity(getIntent(context,statusElement))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusListAdapter.setOnItemClickListener(this)
        rv_status_list.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = statusListAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        onVisible()
        fab_status_list.setOnClickListener {
            onVisible()
        }
    }

    fun onVisible() {
        statusListAdapter.onRefresh()
        refreshList()
    }

    fun refreshList() {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) { // mengakses userChats dari User
                    val partners =
                        it[DATA_USER_CHATS] // mendapatkan id partner currentUser
                    for (partner in (partners as HashMap<String, String>).keys) {
                        firebaseDb.collection(DATA_USERS).document(partner).get()
                            .addOnSuccessListener { documentSnapshot ->
                                val partner = documentSnapshot.toObject(User::class.java)
                                if (partner != null) {
                                    if (!partner.status.isNullOrEmpty() || !partner.statusUrl.isNullOrEmpty()) {
                                        val newElement = StatusListElement(
                                            partner.name,
                                            partner.imageUrl,
                                            partner.status,
                                            partner.statusUrl,
                                            partner.statusTime
                                            // jika status dan statusUrl partner
// tidak kosong, variable newElement akan
// menampung data name, imageUrl, status,
// statusUrl, statusTime dari partner untuk
// selanjutnya dipasangkan di adapter
                                        )
                                        statusListAdapter.addElement(newElement)
                                    }
                                }
                            }
                    }
                }
            }
    }


}
