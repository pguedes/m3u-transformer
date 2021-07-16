package pt.guedes.m3u_transformer.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.guedes.m3u_transformer.web.controller.TransformedPlaylist

internal class TransformationParserTest {
    private val parser = TransformationParser()

    @Test
    fun canParseAttributeEdit() {

        val att: AttributeEdit = parser.parse(
            """
          attribute: "group-title"
          original: "Movie:"
          replacement: "Movies:"
        """.trimIndent()
        )

        assertEquals(AttributeEdit("group-title", "Movie:" to "Movies:"), att)
    }

    @Test
    fun canParseAttributeTag() {

        val att: AttributeTag = parser.parse(
            """
          source: "tvg-name"
          target: "quality"
          values:
            - "SD"
            - "FHD"
            - "HD"
            - "4K"
        """.trimIndent()
        )

        assertEquals(AttributeTag("tvg-name", "quality", listOf("SD", "FHD", "HD", "4K")), att)
    }

    @Test
    fun canParseList() {

        val att: List<ItemTransformation> = parser.parse(
            """
          - tag:
              source: "tvg-name"
              target: "quality"
              values:
                - "SD"
                - "FHD"
                - "HD"
                - "4K"
          - tag:
              source: "tvg-name"
              target: "country"
              values:
                - "ES"
                - "PT"
                - "TR"
                - "US"
                - "UK"
                - "US"
                - "RU"
                - "CZ"
                - "IT"
                - "RS"
                - "LU"
                - "FR"
                - "DE"
                - "BE"
                - "NL"
                - "BR"
                - "AU"
                - "CA"
                - "TH"
                - "QA"
                - "CH"
                - "AR"
                - "HU"
                - "SI"
                - "AL"
                - "JP"
                - "BG"
                - "AT"
                - "NO"
                - "SE"
                - "PL"
          - replace:
              attribute: "group-title"
              original: "Movie:"
              replacement: "Movies:"
        """.trimIndent()
        )

        assertEquals(
            listOf(
                AttributeTag("tvg-name", "quality", listOf("SD", "FHD", "HD", "4K")),
                AttributeTag(
                    "tvg-name", "country", listOf(
                        "ES", "PT", "TR", "US", "UK",
                        "US", "RU", "CZ", "IT", "RS", "LU", "FR", "DE", "BE", "NL", "BR", "AU", "CA", "TH", "QA",
                        "CH", "AR", "HU", "SI", "AL", "JP", "BG", "AT", "NO", "SE", "PL"
                    )
                ),
                AttributeEdit("group-title", "Movie:" to "Movies:"),
            ),
            att
        )
    }

    @Test
    fun canParseAttributesTransformation() {

        val att: PlaylistTransformation = parser.parse(
            """
          attributes:
              - tag:
                  source: "tvg-name"
                  target: "quality"
                  values:
                    - "SD"
                    - "FHD"
                    - "HD"
                    - "4K"
              - tag:
                  source: "tvg-name"
                  target: "country"
                  values:
                    - "ES"
                    - "PT"
                    - "TR"
                    - "US"
                    - "UK"
                    - "US"
                    - "RU"
                    - "CZ"
                    - "IT"
                    - "RS"
                    - "LU"
                    - "FR"
                    - "DE"
                    - "BE"
                    - "NL"
                    - "BR"
                    - "AU"
                    - "CA"
                    - "TH"
                    - "QA"
                    - "CH"
                    - "AR"
                    - "HU"
                    - "SI"
                    - "AL"
                    - "JP"
                    - "BG"
                    - "AT"
                    - "NO"
                    - "SE"
                    - "PL"
              - replace:
                  attribute: "group-title"
                  original: "Movie:"
                  replacement: "Movies:"
        """.trimIndent()
        )

        assertEquals(
            attributeTransformations(
                AttributeTag("tvg-name", "quality", listOf("SD", "FHD", "HD", "4K")),
                AttributeTag(
                    "tvg-name", "country", listOf(
                        "ES",
                        "PT",
                        "TR",
                        "US",
                        "UK",
                        "US",
                        "RU",
                        "CZ",
                        "IT",
                        "RS",
                        "LU",
                        "FR",
                        "DE",
                        "BE",
                        "NL",
                        "BR",
                        "AU",
                        "CA",
                        "TH",
                        "QA",
                        "CH",
                        "AR",
                        "HU",
                        "SI",
                        "AL",
                        "JP",
                        "BG",
                        "AT",
                        "NO",
                        "SE",
                        "PL"
                    )
                ),
                AttributeEdit("group-title", "Movie:" to "Movies:"),
            ),
            att
        )
    }

    @Test
    fun canParseSelectByAttributeTransformation() {

        val att: SelectByAttributePlaylistTransformation = parser.parse(
            """
          grouping:
            attributes:
              - "tvg-name"
              - "country"
          selection-attribute: "quality"
          preference:
            - "SD"
            - "FHD"
            - "HD"
        """.trimIndent()
        )

        assertEquals(
            SelectByAttributePlaylistTransformation(
                AttributesGrouping(listOf("tvg-name", "country")),
                "quality", listOf("SD", "FHD", "HD")
            ),
            att
        )
    }

    @Test
    fun canParseFilterByAttribute() {

        val parsed: List<Filter> = parser.parse("""
        - attribute:
            country:
              - "QA"
              - "BG"
        """.trimIndent()
        )

        assertEquals(
            listOf(
                listOf(
                    AttributeFilter("country", listOf("QA", "BG")),
                ).asComposite()
            ),
            parsed
        )
    }

    @Test
    fun canParseTransformation() {

        val att: PlaylistTransformation = parser.parse("""
          attributes:
            - tag:
                source: "tvg-name"
                target: "quality"
                values:
                  - "SD"
                  - "FHD"
                  - "HD"
                  - "4K"
            - tag:
                source: "tvg-name"
                target: "country"
                values:
                  - "ES"
                  - "PT"
                  - "TR"
                  - "US"
                  - "UK"
                  - "US"
                  - "RU"
                  - "CZ"
                  - "IT"
                  - "RS"
                  - "LU"
                  - "FR"
                  - "DE"
                  - "BE"
                  - "NL"
                  - "BR"
                  - "AU"
                  - "CA"
                  - "TH"
                  - "QA"
                  - "CH"
                  - "AR"
                  - "HU"
                  - "SI"
                  - "AL"
                  - "JP"
                  - "BG"
                  - "AT"
                  - "NO"
                  - "SE"
                  - "PL"
            - replace:
                attribute: "group-title"
                original: "Movie:"
                replacement: "Movies:"
          filter:
            - attribute:
                country:
                  - "QA"
                  - "BG"
          select-by-attribute:
            grouping:
              attributes:
                - "tvg-name"
                - "country"
            selection-attribute: "quality"
            preference:
              - "SD"
              - "FHD"
              - "HD"
        """.trimIndent()
        )

        assertEquals(
                TransformationComposite(listOf(
                    attributeTransformations(
                        AttributeTag("tvg-name", "quality", listOf("SD", "FHD", "HD", "4K")),
                        AttributeTag(
                            "tvg-name", "country", listOf("ES", "PT", "TR", "US", "UK",
                                "US", "RU", "CZ", "IT", "RS", "LU", "FR", "DE", "BE", "NL", "BR", "AU", "CA", "TH", "QA",
                                "CH", "AR", "HU", "SI", "AL", "JP", "BG", "AT", "NO", "SE", "PL"
                            )
                        ),
                        AttributeEdit("group-title", "Movie:" to "Movies:"),
                    ),
                    listOf(
                        listOf(
                            AttributeFilter("country", listOf("QA", "BG")),
                        ).asComposite()
                    ).asTransformation(),
                    SelectByAttributePlaylistTransformation(
                        AttributesGrouping(listOf("tvg-name", "country")),
                        "quality", listOf("SD", "FHD", "HD")
                    )
                )),
            att
        )
    }

    @Test
    fun canParseFullObject() {

        val att: TransformedPlaylist = parser.parse(
            """
            url: "http://nothing.com/here"
            transformation:
              attributes:
                - tag:
                    source: "tvg-name"
                    target: "quality"
                    values:
                      - "SD"
                      - "FHD"
                      - "HD"
                      - "4K"
                - tag:
                    source: "tvg-name"
                    target: "country"
                    values:
                      - "ES"
                      - "PT"
                      - "TR"
                      - "US"
                      - "UK"
                      - "US"
                      - "RU"
                      - "CZ"
                      - "IT"
                      - "RS"
                      - "LU"
                      - "FR"
                      - "DE"
                      - "BE"
                      - "NL"
                      - "BR"
                      - "AU"
                      - "CA"
                      - "TH"
                      - "QA"
                      - "CH"
                      - "AR"
                      - "HU"
                      - "SI"
                      - "AL"
                      - "JP"
                      - "BG"
                      - "AT"
                      - "NO"
                      - "SE"
                      - "PL"
                - replace:
                    attribute: "group-title"
                    original: "Movie:"
                    replacement: "Movies:"
              select-by-attribute:
                grouping:
                  attributes:
                    - "tvg-name"
                    - "country"
                selection-attribute: "quality"
                preference:
                  - "SD"
                  - "FHD"
                  - "HD"
        """.trimIndent().byteInputStream()
        )

        assertEquals(
            TransformedPlaylist(
                "http://nothing.com/here",
                TransformationComposite(
                    attributeTransformations(
                        AttributeTag("tvg-name", "quality", listOf("SD", "FHD", "HD", "4K")),
                        AttributeTag(
                            "tvg-name", "country", listOf(
                                "ES",
                                "PT",
                                "TR",
                                "US",
                                "UK",
                                "US",
                                "RU",
                                "CZ",
                                "IT",
                                "RS",
                                "LU",
                                "FR",
                                "DE",
                                "BE",
                                "NL",
                                "BR",
                                "AU",
                                "CA",
                                "TH",
                                "QA",
                                "CH",
                                "AR",
                                "HU",
                                "SI",
                                "AL",
                                "JP",
                                "BG",
                                "AT",
                                "NO",
                                "SE",
                                "PL"
                            )
                        ),
                        AttributeEdit("group-title", "Movie:" to "Movies:"),
                    ),
                    SelectByAttributePlaylistTransformation(
                        AttributesGrouping(listOf("tvg-name", "country")),
                        "quality", listOf("SD", "FHD", "HD")
                    )
                )
            ),
            att
        )
    }
}