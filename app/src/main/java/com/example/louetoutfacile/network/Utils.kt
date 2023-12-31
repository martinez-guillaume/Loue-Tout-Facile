package com.example.feedarticlesjetpack.network

import android.content.Context
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

import com.example.louetoutfacile.R
import com.squareup.picasso.Picasso
import javax.inject.Inject

/*
@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, imageUrl: String?) {
    imageUrl?.let {
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.feedarticles_logo)
                .error(R.drawable.feedarticles_logo)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.feedarticles_logo)
        }
    }
}*/

class ResourceProvider @Inject constructor(private val context: Context) {
    fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }
}