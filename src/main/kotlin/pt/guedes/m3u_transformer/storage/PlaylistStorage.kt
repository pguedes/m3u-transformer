package pt.guedes.m3u_transformer.storage

import pt.guedes.m3u_transformer.transformation.TransformationParser
import pt.guedes.m3u_transformer.web.controller.TransformedPlaylist
import java.io.InputStream
import java.nio.file.Path

interface PlaylistStorage {
    operator fun get(id: String): TransformedPlaylist?
    // XXX this is weird but i do not want to deal with writing the yaml and
    // we will already have the yaml from the REST interface... so... yeah lazy
    operator fun set(id: String, playlist : InputStream)

    operator fun contains(id: String) : Boolean
}

class CachedPlaylistStorage(
    private val delegate: PlaylistStorage
) : PlaylistStorage by delegate {

    private val cache = hashMapOf<String, TransformedPlaylist>()

    override fun get(id: String): TransformedPlaylist? {
        if (id in cache) {
            return cache[id]
        }
        val item = delegate[id]
        item?.also { cache[id] = it }
        return item
    }
}

class FilePlaylistStorage(
    private val directory: Path
) : PlaylistStorage {

    override operator fun get(id: String): TransformedPlaylist? {
        val file = directory.resolve(id).toFile()
        if (!file.exists()) {
            return null
        }
        file.inputStream().use {
            return TransformationParser().parse(it)
        }
    }

    override operator fun set(id: String, playlist: InputStream) {
        // hmmm validation
        TransformationParser().parse(playlist)
        playlist.reset()

        directory.resolve(id)
            .toFile()
            .outputStream().use {
                playlist.copyTo(it)
            }
    }

    override fun contains(id: String): Boolean {
        return directory.resolve(id).toFile().exists()
    }
}