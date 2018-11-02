package main

import com.beust.klaxon.Json

data class N11Order(
        val orderDetail: N11OrderDetail,
        val customer: N11Customer,
        val lineItems: MutableList<N11LineItem>,
        val addresses: HashMap<String, N11Address>
)

data class N11OrderDetail(
        @Json(name= "orderNumber")
        val orderId: String,
        @Json(name="createDate")
        var orderDatetime: String,
        @Json(ignored = true)
        var shippingCost: String = "",
        @Json(name= "id")
        val n11OrderId: String,
        @Json(name="status")
        val n11OrderStatus: String,
        val paymentType: String,
        @Json(name= "citizenshipId")
        val tcId: String
)

data class N11LineItem(
        @Json(name = "productSellerCode")
        val productSku: String,
        @Json(name = "sellerStockCode")
        val variantSku: String? = null,
        val productName: String,
        val quantity: String,
        val price: String,
        @Json(name = "sellerDiscount")
        val discountAmount: String,
        val commission: String,
        @Json(name= "id")
        val n11LineItemId: String,
        @Json(name="deliveryFeeType")
        val n11ShippingCostBearer: String,
        @Json(name = "shipmenCompanyCampaignNumber")
        var shippingCode: String,
        @Json(name = "productId")
        val n11ProductId: String,
        @Json(name= "sellerCouponDiscount")
        val aryozGiftCard: String,
        @Json(name= "totalMallDiscountPrice")
        val n11GiftCard: String,
        @Json(name= "installmentChargeWithVAT")
        val interestCost: String
)

data class N11Address(
        @Json(name = "fullName")
        var fullName: String,
        @Json(name = "gsm")
        var phone: String,
        @Json(name = "address")
        val address: String,
        @Json(name = "neighborhood")
        val district: String,
        @Json(name = "district")
        val town: String,
        val city: String,
        val postalCode: String,
        @Json(ignored = true)
        var taxOffice: String = "",
        @Json(ignored = true)
        var taxId: String = ""
)

data class N11Customer(
        val firstName: String,
        val lastName: String,
        val phone: String,
        val email: String,
        val tcId: String
)