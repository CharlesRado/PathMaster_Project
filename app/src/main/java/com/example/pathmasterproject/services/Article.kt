package com.example.pathmasterproject.services

import com.google.firebase.database.PropertyName

data class Article(
    val title: String = "",
    val url: String = "",
    val abstract: String = "",
    val category: String = "",
    val website: String = "",
    @get:PropertyName("pdf_url") @set:PropertyName("pdf_url")
    var pdfUrl: String? = null
)