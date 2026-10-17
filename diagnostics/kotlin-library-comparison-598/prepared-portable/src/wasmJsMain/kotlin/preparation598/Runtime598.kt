package preparation598
private fun nodeVersion598(): String = js("process.versions.node")
actual fun portableRuntime598(): String {
    val version = nodeVersion598()
    check(version == "24.16.0") { "Wrong Node runtime: $version" }
    return "Wasm/Node$version"
}
