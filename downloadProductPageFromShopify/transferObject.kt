package main

import com.beust.klaxon.JsonArray

data class ProductPage (
        val sku: String,
        val shopifyProductId: Long,
        val title: String,
        val description: String,
        val imageUrlList: JsonArray<String?>,
        val tags: String,
        val modified_at: String
)