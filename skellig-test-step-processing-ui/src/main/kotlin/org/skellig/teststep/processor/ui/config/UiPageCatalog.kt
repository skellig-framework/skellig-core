package org.skellig.teststep.processor.ui.config

import com.typesafe.config.Config
import java.net.URI
import javax.xml.xpath.XPathFactory

/** Runtime page definitions; resolving a control never navigates to its page. */
internal class UiPageCatalog(config: Config) {
    private val baseUrl = if (config.hasPath("base-url")) absoluteUrl(config.getString("base-url")) else null
    private val pages: Map<String, Page>

    init {
        val definitions = config.getConfig("pages")
        pages = definitions.root().keys.associateWith { name ->
            require(name.isNotBlank() && !name.contains('.')) { "UI page names must be non-empty and cannot contain dots: '$name'" }
            val page = definitions.getConfig(ConfigUtil.quote(name))
            val path = if (page.hasPath("path")) page.getString("path") else null
            path?.let { resolveUrl(it) }
            val elements = if (page.hasPath("elements")) page.getConfig("elements").root().keys.associateWith { control ->
                require(control.isNotBlank() && !control.contains('.')) { "UI control names must be non-empty and cannot contain dots: '$control'" }
                val xpath = page.getConfig("elements").getString(ConfigUtil.quote(control))
                require(xpath.isNotBlank()) { "XPath for '$name.$control' must not be empty" }
                XPathFactory.newInstance().newXPath().compile(xpath)
                xpath
            } else emptyMap()
            Page(path, elements)
        }
    }

    fun pageUrl(name: String): String {
        val page = pages[name] ?: error("Unknown UI page '$name'")
        return resolveUrl(page.path ?: error("UI page '$name' has no navigation path"))
    }

    fun xpath(target: String): String {
        val page = target.substringBefore('.')
        val control = target.substringAfter('.', "")
        return pages[page]?.elements?.get(control) ?: error("Unknown UI control '$target'; expected page.control")
    }

    fun targets(page: String? = null): Set<String> {
        val selected = if (page == null) pages else mapOf(page to (pages[page] ?: error("Unknown UI page '$page'")))
        return selected.flatMap { (name, definition) -> definition.elements.keys.map { "$name.$it" } }.toSet()
    }

    private fun resolveUrl(path: String): String {
        val uri = URI(path)
        return absoluteUrl(if (uri.isAbsolute) path else (baseUrl ?: error("ui.base-url is required for relative page paths")).resolve(uri).toString()).toString()
    }

    companion object {
        fun absoluteUrl(url: String): URI {
            val uri = URI(url)
            require(uri.scheme in setOf("http", "https") && !uri.host.isNullOrEmpty()) { "UI navigation URL must be an absolute HTTP(S) URL: '$url'" }
            return uri
        }
    }

    private data class Page(val path: String?, val elements: Map<String, String>)
}

internal object ConfigUtil {
    fun quote(name: String): String = com.typesafe.config.ConfigUtil.quoteString(name)
}
