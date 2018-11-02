package main

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ShopifyWSDAO {
    fun getProductPageFromShopify (productId: Long): String {
        val encoded: ByteArray = Base64.getEncoder().encode(
                (Shopify().API_KEY + ":" + Shopify().API_SECRET).toByteArray())
        val conn = URL(Shopify().BASE_URL + Shopify().PRODUCT_API + productId + ".json").
                openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Basic " + String(encoded))
        conn.connectTimeout = 5000

        var trial = 0
        var inputStream: InputStream
        do {
            inputStream = conn.inputStream
            trial++
        } while (conn.responseCode != 200 && trial < 20)

        if (conn.responseCode != 200)
            throw ConnectException("Shopify " + conn.responseCode + ":" + conn.responseMessage)
        return BufferedReader(InputStreamReader(inputStream)).readText()

    }
}