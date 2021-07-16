package pt.guedes.m3u_transformer.web.controller

import io.javalin.http.Context
import org.hashids.Hashids
import pt.guedes.m3u_transformer.io.M3uWriter
import pt.guedes.m3u_transformer.parser.M3UParser
import pt.guedes.m3u_transformer.storage.CachedPlaylistStorage
import pt.guedes.m3u_transformer.storage.CachedSource
import pt.guedes.m3u_transformer.storage.FilePlaylistStorage
import pt.guedes.m3u_transformer.storage.Source
import pt.guedes.m3u_transformer.transformation.PlaylistTransformation
import java.io.OutputStream
import java.nio.file.Path

val parser = M3UParser()

data class TransformedPlaylist(
    private val url: String,
    private val transformation: PlaylistTransformation
) {
    fun render(output: OutputStream, source: Source) {
//        val source = URL(url)
//        val playlist = M3UParser().parse(source.openStream())
//
//        M3uWriter(output).write(transformation(playlist))

        val data = time("playlist download") { source[url] }
        val playlist = time("playlist parse") { parser.parse(data) }
        val transformed = time("applying transformation") { transformation(playlist) }
        time("playlist writing") { M3uWriter(output).write(transformed) }
    }
}

object PlaylistController {

    private val data = CachedPlaylistStorage(FilePlaylistStorage(Path.of("/tmp/playlists/transformations")))
    private val source = CachedSource(Path.of("/tmp/playlists/sources"))
    private val ids = Hashids("tens de meter sal sem ficar mto salgado!")

    fun create(ctx: Context) {
        val id = ids.encode(System.currentTimeMillis())
        try {
            data[id] = ctx.bodyAsInputStream()
        } catch (e : Exception) {
            ctx.res.sendError(500, "could not parse payload: ${e.message}")
        }
        ctx.result(id.toString())
    }

    fun render(ctx: Context) {

        val transformedPlaylist = time("playlist loading") { data[ctx.pathParam(":id")] }
        if (transformedPlaylist == null) {
            ctx.res.sendError(404, "playlist id not found")
        }
        time("playlist rendering") { transformedPlaylist?.render(ctx.res.outputStream, source) }
    }

    fun update(ctx: Context) {
        try {
            val id = ctx.pathParam<String>(":id")
                .check({ it in data }, "id does not exist")
                .get()
            data[id] = ctx.bodyAsInputStream()
        } catch (e : Exception) {
            ctx.res.sendError(500, "could not parse payload: ${e.message}")
        }
    }
}

private inline fun <T> time(label: String, f: () -> T) : T {
    val currentTimeMillis = System.currentTimeMillis()
    val v = f()
    println("ran $label in ${System.currentTimeMillis() - currentTimeMillis}ms")
    return v
}