package pt.guedes.m3u_transformer.parser

import org.jparsec.Parser
import org.jparsec.Parsers
import org.jparsec.Scanners
import org.jparsec.pattern.CharPredicates
import org.jparsec.pattern.Patterns
import java.io.InputStream
import java.util.function.Function

internal fun ignoredWhitespace(): Parser<Void?> {
    return Scanners.WHITESPACES.optional(null)
}

internal fun attributeExpressionParser(): Parser<Attribute> {
    val attributeIdentifier = Patterns.isChar(
        CharPredicates.or(
            CharPredicates.IS_ALPHA_NUMERIC_,
            CharPredicates.among("-#$%><[]{}()") // i don't really know.. there is no spec
        )
    ).many().toScanner("attributeIdentifier").source()
    return Parsers.sequence(
        ignoredWhitespace(),
        attributeIdentifier,
        ignoredWhitespace().next(Scanners.string("=")).next(ignoredWhitespace()),
        Scanners.SINGLE_QUOTE_STRING.or(Scanners.DOUBLE_QUOTE_STRING)
            .map { it.replace("\"", "").replace("'", "") }
    ) { _, name: String, _, value: String ->
        Attribute(name, value)
    }
}

internal fun attributeExpressionsParser(): Parser<List<Attribute>> {
    return attributeExpressionParser().many()
}

internal fun extInfParser(): Parser<ExtendedInformation> {
    val signedDecimal = Patterns.regex("-?\\d+")
        .toScanner("decimal")
        .source()
        .map<Float> { s: String -> s.toFloat() }
    return Parsers.sequence(
        Scanners.string("#EXTINF:"),
        signedDecimal,
        attributeExpressionsParser().optional(emptyList()),
        ignoredWhitespace().next<Void>(Scanners.string(",")).next<Void?>(ignoredWhitespace()),
        Scanners.notChar('\n').many().source()
    ) { _, runtime: Float, attributes: List<Attribute>, _, title: String ->
        ExtendedInformation(
            runtime,
            attributes,
            title
        )
    }
}

internal fun linkParser(): Parser<String> {
    return Scanners.notChar('#').next<List<Void>>(Scanners.notChar('\n').many()).source()
}

internal fun metadataParser(): Parser<ExtendedInformation> {
    return Parsers.or<Any>(
        extInfParser(),
        Scanners.isChar('#').next<List<Void>>(Scanners.notChar('\n').many())
    ).sepBy(Scanners.isChar('\n'))
        .map(Function<List<Any>, ExtendedInformation> { objects: List<Any> ->
            objects.stream()
                .filter { o: Any? -> o is ExtendedInformation }
                .map(Function<Any, ExtendedInformation> { `object`: Any? ->
                    ExtendedInformation::class.java.cast(
                        `object`
                    )
                })
                .findFirst().orElse(null)
        })
}

internal fun itemParser(): Parser<M3UItem> {
    return Parsers.sequence(
        metadataParser(),
        Scanners.isChar('\n'),
        linkParser()
    ) { extendedInformation: ExtendedInformation, _, link: String ->
        DefaultM3UItem(
            extendedInformation,
            link
        )
    }
}

internal fun parser(): Parser<List<M3UItem>> {
    return itemParser().sepBy(Scanners.isChar('\n'))
}

class M3UParser {
    private val parser: Parser<List<M3UItem>> = parser()

    fun parse(input: InputStream): M3UPlaylist {
        return parser.parse(input.reader())
    }
}