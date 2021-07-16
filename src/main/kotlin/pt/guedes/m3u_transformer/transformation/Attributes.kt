package pt.guedes.m3u_transformer.transformation

import pt.guedes.m3u_transformer.parser.M3UItem

typealias M3UItemTransformation = (M3UItem) -> M3UItem
typealias AttributeOverrides = Map<String, AttributeValueTransformation>
typealias AttributeValueTransformation = (String?) -> String?

interface ItemTransformation : M3UItemTransformation
// wrapper fn
fun itemTransformation(fn: M3UItemTransformation) : ItemTransformation {
    return object : ItemTransformation {
        override fun invoke(i: M3UItem) = fn(i)
    }
}

fun attributeTransformations(vararg transformations: ItemTransformation): AttributeTransformation {
    return AttributeTransformation(ItemTransformationComposite(transformations.asList()))
}

fun List<ItemTransformation>.asComposite() : PlaylistTransformation {
    return AttributeTransformation(ItemTransformationComposite(this))
}

// adapter for M3UItemTransformation -> PlaylistTransformation
data class AttributeTransformation (
    private val itemTransformation: ItemTransformation
) : PlaylistTransformation {

    override fun invoke(playlist: List<M3UItem>): List<M3UItem> {
        return playlist.map(itemTransformation)
    }
}

data class AttributeEdit (
    private val attribute: String,
    private val replacement: Pair<String, String>,
) : ItemTransformation {

    override operator fun invoke(item: M3UItem): M3UItem {
        val toReplace = replacement.first
        val attributeValue = item.extInfoAttribute(attribute)
        if (attributeValue?.contains(toReplace) == true) {
            return AttributeOverriddenM3uItem(item, mapOf(
                attribute to fixedValue(attributeValue.replace(toReplace, replacement.second)),
            ))
        }
        return item
    }
}

data class AttributeTag (
    private val sourceAttribute: String,
    private val targetAttribute: String,
    private val tags: Iterable<String>
) : ItemTransformation {

    override operator fun invoke(item: M3UItem): M3UItem {
        val attributeValue = item.extInfoAttribute(sourceAttribute)
        val tag = tags.find { t -> attributeValue?.contains(t) == true }
        if (tag != null) {
            return AttributeOverriddenM3uItem(item, mapOf(
                sourceAttribute to fixedValue(attributeValue?.replace(tag, "")?.replace(" +".toRegex(), " ")?.trim()),
                targetAttribute to fixedValue(tag)
            ))
        }
        return item
    }
}

internal fun fixedValue(value: String?): (String?) -> String? {
    return { _ -> value }
}

internal class AttributeOverriddenM3uItem (
    private val item: M3UItem,
    private val overrides: AttributeOverrides
) : M3UItem by item {

    override fun extInfoAttributes(): Iterable<String> {
        return (item.extInfoAttributes() + overrides.map { it.key }).distinct()
    }

    override fun extInfoAttribute(name: String): String? {
        return overrides[name]?.invoke(item.extInfoAttribute(name)) ?: item.extInfoAttribute(name)
    }
}

internal data class ItemTransformationComposite (
    private val transformations: List<ItemTransformation>
) : ItemTransformation {

    override fun invoke(item: M3UItem): M3UItem {
        // apply all transformations as composite t4(t3(t2(t1(item))))
        return transformations.reduce { acc, fn -> itemTransformation { fn(acc(it)) } }(item)
    }
}