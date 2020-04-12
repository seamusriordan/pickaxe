package services

import io.mockk.every
import io.mockk.mockkClass
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory

object MockURLStreamHandler: URLStreamHandler() {
    private var mockStreamHandlerFactory: URLStreamHandlerFactory = mockkClass(URLStreamHandlerFactory::class)

    init {
        every {mockStreamHandlerFactory.createURLStreamHandler(any())} returns this
        URL.setURLStreamHandlerFactory(mockStreamHandlerFactory)
    }

    private val connections: HashMap<URL, URLConnection> = HashMap()

    override fun openConnection(u: URL): URLConnection {
        return connections[u]!!
    }

    fun setConnection(u: URL, connection: URLConnection) {
        connections[u] = connection

    }
}