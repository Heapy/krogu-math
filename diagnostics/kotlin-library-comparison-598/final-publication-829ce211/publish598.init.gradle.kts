import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.provider.Property

// External destination/version/runtime binding only; producer source is unchanged.
allprojects {
    plugins.whenPluginAdded {
        val pluginClass = javaClass
        val envClass = when {
            pluginClass.name.startsWith("org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin") -> "org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec"
            pluginClass.name.startsWith("org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin") -> "org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec"
            else -> null
        }
        if (envClass != null) {
            val spec = extensions.getByType(pluginClass.classLoader.loadClass(envClass))
            @Suppress("UNCHECKED_CAST")
            (spec.javaClass.getMethod("getVersion").invoke(spec) as Property<String>).set("24.16.0")
            @Suppress("UNCHECKED_CAST")
            (spec.javaClass.getMethod("getDownload").invoke(spec) as Property<Boolean>).set(false)
        }
    }
    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories { maven { name = "Delivery598"; url = uri(providers.gradleProperty("delivery598Repository").get()) } }
        }
    }
    afterEvaluate {
        version = providers.gradleProperty("delivery598Version").get()
        extensions.findByType(PublishingExtension::class.java)?.publications?.withType(MavenPublication::class.java)?.configureEach {
            version = providers.gradleProperty("delivery598Version").get()
        }
    }
}
