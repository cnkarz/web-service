package main

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DbDAO {
    fun getProductsWithShopifyPage(): List<Pair<Long, String>> {
        val conn = DriverManager.getConnection(Database().URL, Database().USERNAME, Database().PASSWORD)
        val selectString = "SELECT product_id_shopify, modified_at FROM product_page " +
                "WHERE product_id_shopify IS NOT NULL;"

        val preparedStatement = conn.prepareStatement(selectString)

        val resultSet = preparedStatement.executeQuery()
        return resultSet.use {
            generateSequence {
                if (it.next()) Pair(it.getLong(1), it.getString(2))
                else null
            }.toList()
        }
    }

    fun updateProductPageInDb(productPage: ProductPage) {
        val conn = DriverManager.getConnection(Database().URL, Database().USERNAME, Database().PASSWORD)
        conn.autoCommit = false
        try {
            updateProductPageData(conn, productPage)
            deleteExistingImages(conn,productPage)
            insertProductImages(conn, productPage)
            conn.commit()
        } catch (e: SQLException) {
            println(e.message)
        } finally {
            conn.close()
        }
    }

    private fun updateProductPageData(conn: Connection, productPage: ProductPage) {
        val updateString = "UPDATE product_page SET title=?, description=?,tags=?,modified_at=? " +
                "WHERE product_id_shopify=?;"
        val preparedStatement = conn.prepareStatement(updateString)

        preparedStatement.setString(1, productPage.title)
        preparedStatement.setString(2, productPage.description)
        preparedStatement.setString(3, productPage.tags)
        preparedStatement.setString(4, productPage.modified_at)
        preparedStatement.setLong(5, productPage.shopifyProductId)
        preparedStatement.executeUpdate()
    }

    private fun deleteExistingImages(conn: Connection, productPage: ProductPage) {
        val deleteString = "DELETE FROM product_image WHERE sku = ?;"
        val preparedStatement = conn.prepareStatement(deleteString)

        preparedStatement.setString(1,productPage.sku)
        preparedStatement.executeUpdate()
    }

    private fun insertProductImages(conn: Connection, productPage: ProductPage) {
        val insertString = "INSERT IGNORE INTO product_image (sku, position, url) VALUES (?,?,?);"
        val preparedStatement = conn.prepareStatement(insertString)

        var position = 1
        for (imageUrl in productPage.imageUrlList){
            preparedStatement.setString(1, productPage.sku)
            preparedStatement.setInt(2, position++)
            preparedStatement.setString(3, imageUrl)
            preparedStatement.addBatch()
        }
        preparedStatement.executeBatch()
    }
}