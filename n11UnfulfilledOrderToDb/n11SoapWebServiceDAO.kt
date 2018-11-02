package main

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.HashMap


class N11SoapWebServiceDAO {
    fun getN11Response(url: String, xmlMsg: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8")
        conn.doOutput = true
        conn.connectTimeout = 5000

        var trial = 0
        val postMsg = OutputStreamWriter(conn.outputStream)
        do {
            postMsg.write(xmlMsg)
            postMsg.flush()
            trial++
        } while (conn.responseCode != 200 && trial < 20)

        postMsg.close()

        if (conn.responseCode != 200)
            throw ConnectException("N11 " + conn.responseCode + ":" + conn.responseMessage)

        return BufferedReader(InputStreamReader(conn.inputStream)).readText()
    }
}