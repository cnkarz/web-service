package main

fun main(args: Array<String>){

    println("Getting unfulfilled orders from n11..")
    val n11OpenOrders = N11ResponseParser().getN11OpenOrders()
    println("Uploading " + n11OpenOrders.size + " open orders to database..")
    DbDAO().writeToAllTables(n11OpenOrders)
    println("Saved all open orders from n11 successfully!")
}