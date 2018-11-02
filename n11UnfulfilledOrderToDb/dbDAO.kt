package main

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Types

class DbDAO {
    fun writeToAllTables (orderList: List<N11Order>) {
        val conn = DriverManager.getConnection(Database().URL, Database().USERNAME, Database().PASSWORD)
        conn.autoCommit = false
        try {
            insertIntoSalesOrder(conn, orderList)
            insertIntoSalesOrderLine(conn, orderList)
            insertIntoSalesOrderN11Data(conn, orderList)
            insertIntoSalesOrderLineN11Data(conn, orderList)
            insertIntoCustomer(conn, orderList)
            conn.commit()
        } catch (e: SQLException) {
            println(e.message)
            println(e.stackTrace)
        } finally {
            conn.close()
        }
    }

    private fun insertIntoSalesOrder (conn: Connection, orderList: List<N11Order>) {
        val insertString = "INSERT IGNORE INTO sales_order(id, order_timestamp, payment_gateway_id, " +
                "currency, sales_channel, shipping_cost, email, phone, shipping_name, shipping_address, " +
                "shipping_town, shipping_city, billing_name, billing_address, billing_town, billing_city) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);"
        val preparedStatement = conn.prepareStatement(insertString)

        for (order in orderList) {
            preparedStatement.setLong(1, order.orderDetail.orderId.toLong())
            preparedStatement.setString(2, order.orderDetail.orderDatetime)
            preparedStatement.setInt(3, (order.orderDetail.paymentType).toInt())
            preparedStatement.setString(4, "TRY") // Currency
            preparedStatement.setString(5, "n") // Sales channel n11
            preparedStatement.setFloat(6, order.orderDetail.shippingCost.toFloat())
            preparedStatement.setString(7, order.customer.email)
            preparedStatement.setString(8, order.customer.phone)
            preparedStatement.setString(9, order.addresses["shipping"]!!.fullName)
            preparedStatement.setString(10, order.addresses["shipping"]!!.address)
            preparedStatement.setString(11, order.addresses["shipping"]!!.town)
            preparedStatement.setString(12, order.addresses["shipping"]!!.city)
            preparedStatement.setString(13, order.addresses["billing"]!!.fullName)
            preparedStatement.setString(14, order.addresses["billing"]!!.address)
            preparedStatement.setString(15, order.addresses["billing"]!!.town)
            preparedStatement.setString(16, order.addresses["billing"]!!.city)
            preparedStatement.addBatch()
        }
        preparedStatement.executeBatch()
        preparedStatement.close()
    }

    private fun insertIntoSalesOrderLine (conn: Connection, orderList: List<N11Order>) {
        val insertString = "INSERT IGNORE INTO sales_order_line(order_id, sku, quantity, price, " +
                "discount_amount, order_status) VALUES (?,?,?,?,?,?);"
        val preparedStatement = conn.prepareStatement(insertString)

        for (order in orderList) {
            for (lineItem in order.lineItems) {
                preparedStatement.setLong(1, order.orderDetail.orderId.toLong())
                preparedStatement.setString(2, if (lineItem.variantSku.isNullOrBlank())
                    lineItem.productSku else lineItem.variantSku)
                preparedStatement.setInt(3, lineItem.quantity.toInt())
                preparedStatement.setFloat(4, lineItem.price.toFloat())
                preparedStatement.setFloat(5, lineItem.discountAmount.toFloat())
                preparedStatement.setString(6, OrderStatus().OPEN)
                preparedStatement.addBatch()
            }
        }
        preparedStatement.executeBatch()
        preparedStatement.close()
    }

    private fun insertIntoSalesOrderN11Data (conn: Connection, orderList: List<N11Order>) {
        val insertString = "INSERT IGNORE INTO sales_order_n11_data " +
                "(order_id, order_n11_id, tax_office, tax_id) VALUES (?,?,?,?);"
        val preparedStatement = conn.prepareStatement(insertString)

        for (order in orderList) {
            preparedStatement.setLong(1, order.orderDetail.orderId.toLong())
            preparedStatement.setLong(2, order.orderDetail.n11OrderId.toLong())
            if (order.addresses["billing"]?.taxOffice.isNullOrEmpty())
                preparedStatement.setNull(3, Types.VARCHAR)
            else preparedStatement.setString(3, order.addresses["billing"]?.taxOffice)
            if (order.addresses["billing"]?.taxId.isNullOrEmpty())
                preparedStatement.setNull(4, Types.CHAR)
            else preparedStatement.setString(4, order.addresses["billing"]?.taxId)
            preparedStatement.addBatch()
        }
        preparedStatement.executeBatch()
        preparedStatement.close()
    }

    private fun insertIntoSalesOrderLineN11Data (conn: Connection, orderList: List<N11Order>) {
        val insertString = "INSERT IGNORE INTO sales_order_line_n11_data (order_id, sku, line_item_id, " +
                "gift_card, interest_cost, shipping_code) VALUES (?,?,?,?,?,?);"
        val preparedStatement = conn.prepareStatement(insertString)

        for (order in orderList) {
            for (lineItem in order.lineItems) {
                preparedStatement.setLong(1, order.orderDetail.orderId.toLong())
                preparedStatement.setString(2,
                        if (lineItem.variantSku.isNullOrEmpty()) lineItem.productSku else lineItem.variantSku)
                preparedStatement.setLong(3, lineItem.n11LineItemId.toLong())
                preparedStatement.setFloat(4, lineItem.n11GiftCard.toFloat())
                preparedStatement.setFloat(5, lineItem.interestCost.toFloat())
                preparedStatement.setString(6,lineItem.shippingCode)
                preparedStatement.addBatch()
            }
        }
        preparedStatement.executeBatch()
        preparedStatement.close()
    }

    private fun insertIntoCustomer (conn: Connection, orderList: List<N11Order>) {
        val insertString = "INSERT IGNORE INTO customer (first_name, last_name, tr_citizen_id, email, phone, " +
                "accepts_marketing, created_at) VALUES (?,?,?,?,?,?,?);"
        val preparedStatement = conn.prepareStatement(insertString)

        for (order in orderList) {
            preparedStatement.setString(1, order.customer.firstName)
            preparedStatement.setString(2, order.customer.lastName)
            preparedStatement.setString(3, order.customer.tcId)
            preparedStatement.setString(4, order.customer.email)
            preparedStatement.setString(5, order.customer.phone)
            preparedStatement.setByte(6, 1)
            preparedStatement.setString(7, order.orderDetail.orderDatetime.split(" ")[0])
            preparedStatement.addBatch()
        }
        preparedStatement.executeBatch()
        preparedStatement.close()
    }
}