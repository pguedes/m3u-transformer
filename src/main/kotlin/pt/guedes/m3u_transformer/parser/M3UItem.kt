package pt.guedes.m3u_transformer.parser

typealias M3UPlaylist = List<M3UItem>

interface M3UItem {
    fun runtime() : Float
    fun title() : String
    fun link(): String
    fun extInfoAttributes(): Iterable<String>
    fun extInfoAttribute(name: String): String?
}

internal data class DefaultM3UItem (
    private val extendedInformation: ExtendedInformation,
    private val link: String
) : M3UItem {

    override fun runtime(): Float {
        return extendedInformation.runtime
    }

    override fun title(): String {
        return extendedInformation.title
    }

    override fun extInfoAttributes(): Iterable<String> {
        return extendedInformation.attributeNames()
    }

    override fun extInfoAttribute(name: String): String? {
        return extendedInformation.namedAttribute(name)
    }

    override fun link(): String {
        return link
    }
}

internal data class ExtendedInformation(
    val runtime: Float,
    private val attributes: List<Attribute>,
    val title: String
) {

    fun attributeNames() : Iterable<String> {
        return attributes.map { it.name }
    }

    fun namedAttribute(attribute: String): String? {
        return attributes.find { it.name == attribute }?.value
    }
}

internal data class Attribute (val name: String, val value: String)