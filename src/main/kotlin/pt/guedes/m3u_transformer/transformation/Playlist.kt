package pt.guedes.m3u_transformer.transformation

import pt.guedes.m3u_transformer.parser.M3UItem
import pt.guedes.m3u_transformer.parser.M3UPlaylist

typealias Transformation<T> = (T) -> T

interface PlaylistTransformation : (M3UPlaylist) -> M3UPlaylist

// we need the interface for jackson (java interop) and so this is a wrapper just to make syntax look more kotlin OMG
// https://stackoverflow.com/questions/33590646/kotlin-use-a-lambda-in-place-of-a-functional-interface
fun playlistTransformation(fn: (M3UPlaylist) -> M3UPlaylist): PlaylistTransformation {
    return object : PlaylistTransformation {
        override fun invoke(p: M3UPlaylist) = fn(p)
    }
}

internal data class TransformationComposite(
    private val transformations: List<PlaylistTransformation>
) : PlaylistTransformation {

    constructor(vararg transformations: PlaylistTransformation) : this(transformations.asList())

    override fun invoke(input: M3UPlaylist): M3UPlaylist {
        // apply all transformations as composite t4(t3(t2(t1(it))))
        return transformations.reduce { acc, fn -> playlistTransformation { fn(acc(it)) } }(input)
    }
}

fun List<PlaylistTransformation>.asComposite() : PlaylistTransformation {
    if (this.size == 1) {
        return this[0]
    }
    return TransformationComposite(this)
}

interface Grouping : (M3UItem) -> String

data class AttributesGrouping(
    private val attributes : List<String>
) : Grouping {

    override fun invoke(item: M3UItem): String {
        return attributes.map { item.extInfoAttribute(it) ?: "" }.reduce { acc, attribute -> acc + attribute }
    }
}

data class SelectByAttributePlaylistTransformation(
    private val grouping: Grouping,
    private val selectionAttribute: String,
    private val valuePreference: List<String>,
) : PlaylistTransformation {

    override operator fun invoke(playlist: M3UPlaylist): M3UPlaylist {
        return playlist
            .groupBy(grouping)
            .flatMap { (_, links) ->
                links
                    .sortedBy { it.extInfoAttribute(selectionAttribute)?.let { v -> 1 - valuePreference.indexOf(v) } }
                    .filterIndexed { index, _ -> index == 0 }
            }
    }
}