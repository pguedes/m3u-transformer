package pt.guedes.m3u_transformer.transformation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pt.guedes.m3u_transformer.parser.Attribute
import pt.guedes.m3u_transformer.parser.DefaultM3UItem
import pt.guedes.m3u_transformer.parser.ExtendedInformation

internal class AttributeEditTest {

    @Test
    fun canEditExtInf() {

        val edit = AttributeEdit("name", "Movie:" to "Movies:")

        val item = DefaultM3UItem(
            ExtendedInformation(-1.0f, listOf(Attribute("name", "Movie: Some Movie [Multi-subs]")), "Some movie"),
            "http://some.server.com/movie"
        )

        val edited = edit(item)

        assertEquals("Movies: Some Movie [Multi-subs]", edited.extInfoAttribute("name"))
    }
}