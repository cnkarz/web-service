package main

import java.sql.SQLException

fun main (args: Array<String>) {
    val dbDAO = DbDAO()
    val shopifyWSDAO = ShopifyWSDAO()
    val shopifyResponseParser = ShopifyResponseParser()

    println("Retrieving products with Shopify pages..")
    val productPageListInDb = dbDAO.getProductsWithShopifyPage()
    println("Downloading modified product pages to database..")
    var counter = 0
    for (pair in productPageListInDb) {
        counter++

        if (counter < 390) continue   // use when shopify throws exception
        val productJson = shopifyWSDAO.getProductPageFromShopify(pair.first)
        val modifiedAt = shopifyResponseParser.getProductModifiedAtTimestamp(productJson)
        if (pair.second === modifiedAt) continue
        val productPage = shopifyResponseParser.getProductPage(productJson)
        try { dbDAO.updateProductPageInDb(productPage) }
        catch (e: SQLException) { println(e.message) }
        if (counter % 15 == 0) println("$counter / ${productPageListInDb.size} completed..")
        Thread.sleep(1000)
    }
    println("Successfully updated product pages in database!")
}