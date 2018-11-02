package main

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import org.json.XML
import java.text.SimpleDateFormat
import java.util.*

class N11ResponseParser {

    fun getN11OpenOrders(): MutableList<N11Order>{
        val n11OrderService = N11OrderService()
        val n11Dao = N11SoapWebServiceDAO()

        // Get a list of open orders
        println("Fetching open orders..")
        val openOrderNumberListSoapMessage = n11OrderService.getSoapMessage(
                serviceType = N11().OrderService().ORDER_LIST, query = N11().SearchStatus().APPROVED)
        val openOrderNumberListResponse = n11Dao.getN11Response(
                url = N11().OrderService().URL, xmlMsg = openOrderNumberListSoapMessage)
        val orderNumberList = getN11OpenOrderNumberList(openOrderNumberListResponse)
        val n11OrderList = mutableListOf<N11Order>()

        // Capture each open order detail
        println("Reading open orders..")
        for (orderNumber in orderNumberList){
            val orderDetailSoapMessage = n11OrderService.getSoapMessage(
                    serviceType = N11().OrderService().ORDER_DETAIL, query = orderNumber)
            val orderDetailResponse = n11Dao.getN11Response(
                    url = N11().OrderService().URL, xmlMsg = orderDetailSoapMessage)
            val orderObject = parseN11Order(orderDetailResponse)
            n11OrderList.add(orderObject)
        }

        return n11OrderList
    }

    private fun getN11OpenOrderNumberList(xmlString: String): List<String> {
        val jsonObject = XML.toJSONObject(xmlString, true)
        val response = Parser().parse(StringBuilder(jsonObject.toString())) as JsonObject
        val orderList = response.obj("env:Envelope")?.obj("env:Body")?.
                obj("ns3:OrderListResponse")?.obj("orderList")

        try {
            return orderList?.array<JsonObject>("order")?.string("id") as List<String>
        } catch (e: ClassCastException) {
            return listOf(orderList?.obj("order")?.string("id")!!)
        }
    }

    private fun parseN11Order(xmlString: String): N11Order {
        fun getTimestampFromN11OrderDatetime(datetime: String): String {
            val n11DatetimeFormat = SimpleDateFormat("dd/MM/yyy HH:mm:ss")
            val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            val n11Datetime = n11DatetimeFormat.parse(datetime + ":00")
            val calendar = Calendar.getInstance()
            calendar.time = n11Datetime
            calendar.add(Calendar.HOUR_OF_DAY,-3) // CONVERT TO UTC
            return timestampFormat.format(calendar.time)
        }
        val responseJsonObj = XML.toJSONObject(xmlString, true)
        val orderJsonObj = Parser().parse(StringBuilder(responseJsonObj.toString())) as JsonObject
        val orderDetail = orderJsonObj.obj("env:Envelope")?.obj("env:Body")?.
                obj("ns3:OrderDetailResponse")?.obj("orderDetail")

        val n11OrderDetail = Klaxon().parseFromJsonObject<N11OrderDetail>(orderDetail!!)

        var lineItems = mutableListOf<N11LineItem>()
        try {
            lineItems.add(Klaxon().parseFromJsonObject<N11LineItem>(orderDetail.obj("itemList")?.
                    obj("item")!!)!!)
        } catch (e: ClassCastException) {
            lineItems = Klaxon().parseFromJsonArray<N11LineItem>(orderDetail.obj("itemList")?.
                    array<JsonObject>("item")!!)!!.toMutableList()
        }

        val addresses: HashMap<String, N11Address> = hashMapOf<String, N11Address>(
                "shipping" to Klaxon().parseFromJsonObject<N11Address>(orderDetail.obj("shippingAddress")!!)!!,
                "billing" to Klaxon().parseFromJsonObject<N11Address>(orderDetail.obj("billingAddress")!!)!!
        )

        addresses["shipping"]!!.phone = "+90" + addresses["shipping"]!!.phone
        addresses["billing"]!!.phone = "+90" + addresses["billing"]!!.phone

        // Add billing company info, if provided
        if (orderDetail.obj("buyer")?.string("taxOffice")!! !== "") {
            addresses["billing"]?.fullName = orderDetail.obj("buyer")!!.string("fullName")!!
            addresses["billing"]?.taxOffice = orderDetail.obj("buyer")!!.string("taxOffice")!!
            addresses["billing"]?.taxId = orderDetail.obj("buyer")?.string("taxId").toString()
        }

        n11OrderDetail!!.orderDatetime = getTimestampFromN11OrderDatetime(n11OrderDetail.orderDatetime)
        n11OrderDetail.shippingCost = orderDetail.obj("billingTemplate")!!.
                string("totalServiceItemOriginalPrice")!!

        val customerNameInList = addresses["shipping"]!!.fullName.split(" ")
        val n11Customer = N11Customer(
                firstName = customerNameInList[0].capitalize(),
                lastName = customerNameInList.subList(1, customerNameInList.size).joinToString(" "),
                phone = addresses["shipping"]!!.phone,
                email = orderDetail.obj("buyer")!!.string("email")!!,
                tcId = orderDetail.string("citizenshipId")!!
        )

        return N11Order(
                orderDetail =  n11OrderDetail,
                customer = n11Customer,
                lineItems = lineItems,
                addresses = addresses
        )
    }
}