package com.example.habibwhatappclone.util

import android.app.DownloadManager
import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.habibwhatappclone.R
import java.text.DateFormat
import java.util.*

fun populateImage(context: Context?, data: String?, imageView: ImageView, errorDrawable: Int = R.drawable.ic_empty) {
    if (context != null) {
        val option = RequestOptions().placeholder(progressDrawable(context)).error(errorDrawable)
        Glide.with(context).load(data).apply(option).into(imageView)
    }
}

fun progressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        // Ketebalan garis lingkaran
        strokeWidth = 5f
        // Diameter linkaran
        centerRadius = 30f
        // Memulai progresDrawable
        start()
    }

}

fun getTime(): String {
    val dateFormat = DateFormat.getDateInstance()
    return dateFormat.format(Date())
}
