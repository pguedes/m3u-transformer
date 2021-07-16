package pt.guedes.m3u_transformer.io

import pt.guedes.m3u_transformer.parser.M3UItem
import pt.guedes.m3u_transformer.parser.M3UPlaylist
import java.io.OutputStream
import java.io.PrintWriter


class M3uWriter (
    private val output : OutputStream
) {

    fun write(playlist: M3UPlaylist) {
        PrintWriter(output).use { out ->
            out.println("#EXTM3U")
            playlist.forEach{ out.println(m3uString(it)) }
        }
    }

    private fun m3uString(item: M3UItem): String {
        return listOf(extInfo3uString(item), item.link()).joinToString(System.lineSeparator())
    }

    private fun extInfo3uString(item: M3UItem): String {
        val attributeString = item.extInfoAttributes().joinToString(" ") { """$it="${item.extInfoAttribute(it)}"""" }
        return "#EXTINF:${item.runtime().toInt()} $attributeString,${item.title()}"
    }
}