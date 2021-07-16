package pt.guedes.m3u_transformer.transformation

import pt.guedes.m3u_transformer.parser.M3UItem
import pt.guedes.m3u_transformer.parser.M3UPlaylist

interface Filter {
    operator fun invoke(item: M3UItem) : Boolean
}

private fun asFilter(predicate: (M3UItem) -> Boolean) : Filter {
    return object : Filter {
        override fun invoke(item: M3UItem) = predicate(item)
    }
}

data class AttributeFilter(
    private val attribute : String,
    private val values : List<String>,
) : Filter {

    override fun invoke(item: M3UItem): Boolean {
        return item.extInfoAttribute(attribute) in values
    }
}

fun List<Filter>.asComposite() : Filter {

    if (this.isEmpty()) {
        return asFilter { true }
    }

    return when(this.size) {
        1 -> this.first()
        else -> this.reduce { acc, filterItem -> asFilter { acc(it) && filterItem(it) } }
    }
}

data class FilterTransformation(
    private val filter : Filter
) : PlaylistTransformation {

    override fun invoke(playlist: M3UPlaylist): M3UPlaylist {
        return playlist.filter { filter(it) }
    }
}

fun List<Filter>.asTransformation(): PlaylistTransformation {
    return FilterTransformation(this.asComposite())
}

