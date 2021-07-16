package pt.guedes.m3u_transformer.transformation

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import pt.guedes.m3u_transformer.web.controller.TransformedPlaylist
import java.io.InputStream

class TransformationParser {
    private val objectMapper = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .registerModule(parserModule())

    fun parse(input: InputStream): TransformedPlaylist {
        return objectMapper.readValue(input)
    }

    // for unit tests
    internal inline fun <reified T> parse(input: String): T {
        return objectMapper.readValue(input)
    }
}

private fun parserModule() : Module {
    return SimpleModule()
        .addDeserializer(AttributeEdit::class.java, AttributeEditDeserializer())
        .addDeserializer(AttributeTag::class.java, AttributeTagDeserializer())
        .addDeserializer(Filter::class.java, FilterDeserializer())
        .addDeserializer(
            SelectByAttributePlaylistTransformation::class.java,
            SelectByAttributePlaylistTransformationDeserializer()
        )
        // this forces PlaylistTransformation to be a dummy interface instead of just a type alias :facepalm:
        .addDeserializer(PlaylistTransformation::class.java, TransformationDeserializer())
        .addDeserializer(ItemTransformation::class.java, ItemTransformationDeserializer())
}

internal class AttributeEditDeserializer : JsonDeserializer<AttributeEdit>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AttributeEdit {

        val node: JsonNode = p!!.codec!!.readTree(p)

        val attribute = node["attribute"].asText()
        val original = node["original"].asText()
        val replacement = node["replacement"].asText()

        return AttributeEdit(attribute, original to replacement)
    }
}

internal class AttributeTagDeserializer : JsonDeserializer<AttributeTag>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AttributeTag {

        val node: JsonNode = p!!.codec!!.readTree(p)

        val source = node["source"].asText()
        val target = node["target"].asText()
        val values = node["values"].asIterable().map { it.asText() }

        return AttributeTag(source, target, values)
    }
}

internal class SelectByAttributePlaylistTransformationDeserializer :
    JsonDeserializer<SelectByAttributePlaylistTransformation>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): SelectByAttributePlaylistTransformation {

        val node: JsonNode = p!!.codec!!.readTree(p)

        // TODO make this generic and not just a list of attributes
        val grouping = AttributesGrouping(node["grouping"]["attributes"].map { it.asText() }.toList())
        val selectionAttribute = node["selection-attribute"].asText()
        val values = node["preference"].asIterable().map { it.asText() }

        return SelectByAttributePlaylistTransformation(grouping, selectionAttribute, values)
    }
}

internal class ItemTransformationDeserializer : JsonDeserializer<ItemTransformation>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ItemTransformation {

        val mapper = p!!.codec!!
        val node: JsonNode = mapper.readTree(p)

        if (node.count() != 1) {
            throw IllegalStateException("expected node with just one item to identify transformation type: ${node.count()}")
        }

        return when {
            node.has("tag") -> mapper.treeToValue(node["tag"], AttributeTag::class.java)
            node.has("replace") -> mapper.treeToValue(node["replace"], AttributeEdit::class.java)
            else -> throw IllegalStateException("unknown transformation type: $node")
        }
    }
}

internal class TransformationDeserializer : JsonDeserializer<PlaylistTransformation>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PlaylistTransformation {

        val mapper = p!!.codec!!
        val node: JsonNode = mapper.readTree(p)

        val transformations = node.fieldNames().asSequence().map { field ->
            when (field) {
                "select-by-attribute" -> mapper.treeToValue(node["select-by-attribute"], SelectByAttributePlaylistTransformation::class.java)
                "filter" -> {
                    val type = object : TypeReference<List<Filter>>() {}
                    mapper.readValue(mapper.treeAsTokens(node["filter"]), type).asTransformation()
                }
                "attributes" -> {
                    val type = object : TypeReference<List<ItemTransformation>>() {}
                    mapper.readValue(mapper.treeAsTokens(node["attributes"]), type).asComposite()
                }
                else -> throw IllegalStateException("unknown transformation type: $field")
            }
        }.toList()

        return transformations.asComposite()
    }
}

internal class FilterDeserializer : JsonDeserializer<Filter>() {

    private fun attributesFilter(node: JsonNode) : Filter {

        return node.fieldNames().asSequence().map<String, Filter> { attribute ->
            val value = node.get(attribute)
            val values = when {
                value.isArray -> {
                    value.map { it.asText() }.toList()
                }
                else -> listOf(value.asText())
            }
            AttributeFilter(attribute, values)
        }.toList().asComposite()
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Filter {

        val mapper = p!!.codec!!
        val node: JsonNode = mapper.readTree(p)

        return when {
            node.has("attribute") -> attributesFilter(node.get("attribute"))
            else -> throw IllegalStateException("unknown filter type: " + node.get(0))
        }
    }
}
