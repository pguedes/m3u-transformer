package pt.guedes.m3u_transformer.transformation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pt.guedes.m3u_transformer.parser.Attribute
import pt.guedes.m3u_transformer.parser.DefaultM3UItem
import pt.guedes.m3u_transformer.parser.ExtendedInformation

internal class M3UItemTransformationCompositeTest {

    @Test
    fun canEditManyFields() {

        val item = DefaultM3UItem(
            ExtendedInformation(
                -1.0f,
                listOf(
                    Attribute("name", "Movie: Some Movie [Multi-subs]"),
                    Attribute("id", "10"),
                ),
                "Some movie"
            ),
            "http://some.server.com/movie"
        )

        val edit = ItemTransformationComposite(listOf(
            AttributeEdit("name", "Some" to "Whatever"),
            AttributeEdit("id", "1" to "9"),
        ))

        val edited = edit(item)

        assertEquals("90", edited.extInfoAttribute("id"))
        assertEquals("Movie: Whatever Movie [Multi-subs]", edited.extInfoAttribute("name"))
    }

    @Test
    fun editsInOrder() {

        val item = DefaultM3UItem(
            ExtendedInformation(
                -1.0f,
                listOf(
                    Attribute("name", "Movie: Some Movie [Multi-subs]"),
                    Attribute("id", "10"),
                ),
                "Some movie"
            ),
            "http://some.server.com/movie"
        )

        val edit = ItemTransformationComposite(listOf(
            AttributeEdit("name", "Some" to "Whatever"),
            AttributeEdit("name", "Whatever" to "Real Nice"),
        ))

        val edited = edit(item)

        assertEquals("10", edited.extInfoAttribute("id"))
        assertEquals("Movie: Real Nice Movie [Multi-subs]", edited.extInfoAttribute("name"))
    }
}