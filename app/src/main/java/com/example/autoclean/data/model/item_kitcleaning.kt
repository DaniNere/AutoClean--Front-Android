package com.example.autoclean.data.model.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemKitCleaning(
    val id: String,
    val title: String,
    val description: List<String> = emptyList()
) : Parcelable

