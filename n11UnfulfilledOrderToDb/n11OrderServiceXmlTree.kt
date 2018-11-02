package main

import java.io.StringWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

class N11OrderService {

    fun getSoapMessage(serviceType: String, query: String): String {
        val xmlString = StringWriter()
        val xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(xmlString)

        when (serviceType) {
            "OrderList" -> getOrderList(xmlStreamWriter, serviceType, query)
            "DetailedOrderList" -> getDetailedOrderList(xmlStreamWriter, serviceType, query)
            "OrderDetail" -> getOrderDetail(xmlStreamWriter, serviceType, query)
            else -> return "Not included in the list"
        }

        return xmlString.toString()
    }

    private fun getOrderList(xmlStreamWriter: XMLStreamWriter, serviceType: String, query: String) {
        xmlStreamWriter.document {
            envelope(Soap().PREFIX, Soap().NAMESPACE_URI, Soap().ENVELOPE_LOCAL_NAME,
                    N11().NAMESPACE, N11().NAMESPACE_URI) {
                element("soapenv:Header")
                element("soapenv:Body") {
                    element("sch:${serviceType}Request") {
                        element("auth") {
                            element("appKey", N11().API_KEY)
                            element("appSecret", N11().API_SECRET)
                        }
                        element("searchData") {
                            element("productId")
                            element("status",query)
                            element("buyerName")
                            element("orderNumber")
                            element("productSellerCode")
                            element("recipient")
                            element("sameDayDelivery")
                            element("period") {
                                element("startDate")
                                element("endDate")
                            }
                            element("sortForUpdateDate", "false")
                            element("pagingData") {
                                element("currentPage", "0")
                                element("pageSize", "100")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDetailedOrderList(xmlStreamWriter: XMLStreamWriter, serviceType: String, query: String) {
        xmlStreamWriter.document {
            envelope(Soap().PREFIX, Soap().NAMESPACE_URI, Soap().ENVELOPE_LOCAL_NAME,
                    N11().NAMESPACE, N11().NAMESPACE_URI) {
                element("soapenv:Header")
                element("soapenv:Body") {
                    element("sch:${serviceType}Request") {
                        element("auth") {
                            element("appKey", N11().API_KEY)
                            element("appSecret", N11().API_SECRET)
                        }
                        element("searchData") {
                            element("productId")
                            element("status")
                            element("buyerName")
                            element("orderNumber")
                            element("productSellerCode")
                            element("recipient")
                            element("sameDayDelivery")
                            element("period") {
                                element("startDate", "29/06/2018")
                                element("endDate", "30/06/2018")
                            }
                            element("sortForUpdateDate", "true")
                        }
                        element("pagingData") {
                            element("currentPage", "0")
                            element("pageSize", "100")
                            element("totalCount")
                            element("pageCount")
                        }
                    }
                }
            }
        }
    }

    private fun getOrderDetail(xmlStreamWriter: XMLStreamWriter, serviceType: String, query: String) {
        xmlStreamWriter.document {
            envelope(Soap().PREFIX, Soap().NAMESPACE_URI, Soap().ENVELOPE_LOCAL_NAME,
                    N11().NAMESPACE, N11().NAMESPACE_URI) {
                element("soapenv:Header")
                element("soapenv:Body") {
                    element("sch:${serviceType}Request") {
                        element("auth") {
                            element("appKey", N11().API_KEY)
                            element("appSecret", N11().API_SECRET)
                        }
                        element("orderRequest") {
                            element("id", query)
                        }
                    }
                }
            }
        }
    }
}