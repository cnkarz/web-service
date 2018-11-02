package main

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.bind.DatatypeConverter

class ShopifyResponseParser {
    fun getProductModifiedAtTimestamp (response: String): String {
        val responseObject = Parser().parse(StringBuilder(response)) as JsonObject
        val modifiedAt = responseObject.obj("product")?.string("updated_at")
        val calendar = DatatypeConverter.parseDateTime(modifiedAt)
        calendar.add(Calendar.HOUR_OF_DAY, -3) // convert to UTC
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return timestampFormat.format(calendar.time)
    }

    fun getProductPage (response: String): ProductPage {
        val responseObject = Parser().parse(StringBuilder(response)) as JsonObject
        val product = responseObject.obj("product")

        val updatedAt = product!!.string("updated_at")
        val calendar = DatatypeConverter.parseDateTime(updatedAt)
        calendar.add(Calendar.HOUR_OF_DAY, -3) // convert to UTC
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return ProductPage (
                sku = product.array<JsonObject>("variants")!![0].string("sku")!!,
                shopifyProductId = product.long("id")!!,
                title = product.string("title")!!,
                description = product.string("body_html")!!,
                imageUrlList = product.array<JsonObject>("images")!!.string("src"),
                tags = product.string("tags")!!,
                modified_at = timestampFormat.format(calendar.time)
        )
    }
}