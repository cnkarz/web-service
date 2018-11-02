package main

import org.json.JSONObject
import java.util.*
import javax.xml.stream.XMLStreamWriter

fun XMLStreamWriter.envelope (soapPrefix: String,
                              soapNamespaceUri: String,
                              localName: String,
                              servicePrefix: String,
                              serviceNamespaceUri: String,
                              init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(soapPrefix, localName, soapNamespaceUri)
    this.writeNamespace(soapPrefix, soapNamespaceUri)
    this.writeNamespace(servicePrefix, serviceNamespaceUri)
    this.init()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument()
    this.init()
    this.writeEndDocument()
    return this
}

fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String="") {
    element(name) {
        writeCharacters(content)
    }
}

fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)

// Extension functions to create JSON objects

fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
    return JsonObjectBuilder().json(build)
}

class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = ArrayDeque()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    infix fun <T> String.To(value: T) {
        deque.peek().put(this, value)
    }
}