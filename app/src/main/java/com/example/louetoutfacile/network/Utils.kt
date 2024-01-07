package com.example.feedarticlesjetpack.network

import android.content.Context
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

import com.example.louetoutfacile.R
import com.squareup.picasso.Picasso
import javax.inject.Inject


class ResourceProvider @Inject constructor(private val context: Context) {
    fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }
}