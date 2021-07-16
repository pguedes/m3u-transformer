package pt.guedes.m3u_transformer.storage

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.concurrent.thread

interface Source {
    operator fun get(url: String): InputStream
}

class UrlSource : Source {
    override fun get(url: String) = URL(url).openStream()!!
}

private fun String.shortUrl(): String {
    // hash String with MD5
    val hashBytes = MessageDigest.getInstance("MD5").digest(this.toByteArray(Charsets.UTF_8))
    // transform to human readable MD5 String
    // return truncated MD5 String
    return String.format("%032x", BigInteger(1, hashBytes)).take(6)
}

class CachedSource(
    private val directory: Path
) : Source {

    private val httpClient: HttpClient = HttpClient.newHttpClient()

    override fun get(url: String): InputStream {

        val uri = URI.create(url)
        // do a HEAD request first to check if we need to re-load
        val headRequest = HttpRequest.newBuilder(uri)
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .build()

        val contentLength = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding())
            .headers()
            .firstValue("Content-Length")

        val basePath = directory.resolve(url.shortUrl())

        return contentLength.map { basePath.resolve(it).toFile() }
            .filter { it.exists() }
            .map<InputStream> { it.inputStream() }
            .orElseGet {
                println("not found... loading from remote server")
                val getRequest = HttpRequest.newBuilder(uri).GET().build()
                val body = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofByteArray()).body()

                if (contentLength.isPresent) {
                    thread(start = true) {
                        if (basePath.toFile().exists()) {
                            for (path in basePath) {
                                path.toFile().delete()
                            }
                        } else {
                            Files.createDirectory(basePath)
                        }
                        Files.write(basePath.resolve(contentLength.get()), body)
                    }
                }

                ByteArrayInputStream(body)
            }
    }
}