package pt.guedes.m3u_transformer.parser

import org.jparsec.Parser
import org.jparsec.Scanners
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class M3UParserTest {

    @Test
    fun canParseIdentifierAndValue() {
        val parser = attributeExpressionParser()
        val value = Attribute("something", "this is it!")
        assertEquals(value, parser.parse("something='this is it!'"))
        assertEquals(value, parser.parse("something=\"this is it!\""))
    }

    @Test
    fun canParseIdentifierAndValueWithWhitespace() {
        val parser: Parser<Attribute> = attributeExpressionParser()
        val value = Attribute("something", "this is it!")
        assertEquals(value, parser.parse("     something  = 'this is it!'"))
        assertEquals(value, parser.parse("     something  ='this is it!'"))
        assertEquals(value, parser.parse("something='this is it!'"))
    }

    @Test
    fun canParseManyAttributesWithWhitespace() {
        val parser: Parser<List<Attribute>> = attributeExpressionsParser()
        val value = Arrays.asList(
            Attribute("something", "this is it!"),
            Attribute("id", "1"),
            Attribute("owner", "Mr Lebowski"),
            Attribute("master", "The dude")
        )
        assertEquals(
            value,
            parser.parse("   something  = 'this is it!' id='1' owner=  \"Mr Lebowski\" master=\"The dude\"")
        )
    }

    @Test
    fun canParseExtInfLine() {
        val parser: Parser<ExtendedInformation> = extInfParser()
        val attributes = attributes(
            "",
            "Spycraft S01 E08",
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2/kjolcJPFXiLqtJXb4fVosop6sba.jpg",
            "Series: Netflix"
        )
        val extInfLine =
            "#EXTINF:-1 tvg-id=\"\" tvg-name=\"Spycraft S01 E08\" tvg-logo=\"https://image.tmdb.org/t/p/w600_and_h900_bestv2/kjolcJPFXiLqtJXb4fVosop6sba.jpg\" group-title=\"Series: Netflix\",Spycraft S01 E08"
        val extendedInformation = ExtendedInformation(-1.0f, attributes, "Spycraft S01 E08")
        assertEquals(extendedInformation, parser.parse(extInfLine))
    }

    @Test
    fun canParseLink() {
        val parser = linkParser()
        assertEquals(
            "http://nothing.com/here/72627.mkv",
            parser.parse("http://nothing.com/here/lPrUYO833/72627.mkv")
        )
    }

    @Test
    fun canParseRandomComments() {
        Scanners.isChar('#').next(Scanners.notChar('\n').many()).parse("#EXTM3U")
        Scanners.isChar('#').next(Scanners.notChar('\n').many()).parse("#Whatever")
    }

    @Test
    fun canParseMetadata() {
        val parser: Parser<ExtendedInformation> = metadataParser()
        val attributes = attributes(
            "",
            "Spycraft S01 E08",
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2/kjolcJPFXiLqtJXb4fVosop6sba.jpg",
            "Series: Netflix"
        )
        val text = """#EXTM3U
#EXTINF:-1 tvg-id="" tvg-name="Spycraft S01 E08" tvg-logo="https://image.tmdb.org/t/p/w600_and_h900_bestv2/kjolcJPFXiLqtJXb4fVosop6sba.jpg" group-title="Series: Netflix",Spycraft S01 E08
#NONEXISTENT: and some non-sense"""
        assertEquals(
            ExtendedInformation(-1.0f, attributes, "Spycraft S01 E08"),
            parser.parse(text)
        )
    }

    @Test
    fun canParseItem() {
        val parser: Parser<M3UItem> = itemParser()
        val text = """#EXTM3U
#EXTINF:-1 tvg-id="" tvg-name="Spycraft S01 E08" tvg-logo="https://image.tmdb.org/t/p/w600_and_h900_bestv2/kjolcJPFXiLqtJXb4fVosop6sba.jpg" group-title="Series: Netflix",Spycraft S01 E08
#NOTE: i just wrote this here as my own comment... so what?
http://nothing.com/here/72628.mkv"""
        val attributes = attributes(
            "",
            "Spycraft S01 E08",
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2/kjolcJPFXiLqtJXb4fVosop6sba.jpg",
            "Series: Netflix"
        )
        assertEquals(
            DefaultM3UItem(
                ExtendedInformation(-1.0f, attributes, "Spycraft S01 E08"),
                "http://nothing.com/here/72628.mkv"
            ),
            parser.parse(text)
        )
    }

    @Test
    fun canParseItAll() {
        val parser: Parser<List<M3UItem>> = parser()
        val text = """#EXTM3U
#EXTINF:-1 tvg-id="" tvg-name="▬ beIN Media Group ▬" tvg-logo="https://picon-13398.kxcdn.com/beinmediagroupflag.jpg" group-title="Live: beIN Media Group",▬ beIN Media Group ▬
http://nothing.com/here/16504
#EXTINF:-1 tvg-id="AMC.qa" tvg-name="beIN SPORTS 1 4K QA" tvg-logo="https://picon-13398.kxcdn.com/beinsports1.jpg" group-title="Live: beIN Media Group",beIN SPORTS 1 4K QA
http://nothing.com/here/24697
#EXTINF:-1 tvg-id="BeinSports3.fr" tvg-name="beIN SPORTS 1 FHD QA" tvg-logo="https://picon-13398.kxcdn.com/beinsport1hd.jpg" group-title="Live: beIN Media Group",beIN SPORTS 1 FHD QA
http://nothing.com/here/22157"""
        assertEquals(
            listOf(
                DefaultM3UItem(
                    ExtendedInformation(
                        -1.0f,
                        attributes(
                            "",
                            "▬ beIN Media Group ▬",
                            "https://picon-13398.kxcdn.com/beinmediagroupflag.jpg",
                            "Live: beIN Media Group"
                        ),
                        "▬ beIN Media Group ▬"
                    ),
                    "http://nothing.com/here/16504"
                ),
                DefaultM3UItem(
                    ExtendedInformation(
                        -1.0f,
                        attributes(
                            "AMC.qa",
                            "beIN SPORTS 1 4K QA",
                            "https://picon-13398.kxcdn.com/beinsports1.jpg",
                            "Live: beIN Media Group"
                        ),
                        "beIN SPORTS 1 4K QA"
                    ),
                    "http://nothing.com/here/24697"
                ),
                DefaultM3UItem(
                    ExtendedInformation(
                        -1.0f,
                        attributes(
                            "BeinSports3.fr",
                            "beIN SPORTS 1 FHD QA",
                            "https://picon-13398.kxcdn.com/beinsport1hd.jpg",
                            "Live: beIN Media Group"
                        ),
                        "beIN SPORTS 1 FHD QA"
                    ),
                    "http://nothing.com/here/22157"
                )
            ),
            parser.parse(text)
        )
    }

    private fun attributes(
        id: String,
        name: String,
        logo: String,
        group: String
    ): List<Attribute> {
        return listOf(
            Attribute("tvg-id", id),
            Attribute("tvg-name", name),
            Attribute("tvg-logo", logo),
            Attribute("group-title", group)
        )
    }
}