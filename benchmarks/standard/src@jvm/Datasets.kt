package bench
internal actual fun benchmarkDatasets() = listOf("krogu" to kroguWorkloads(), "jdk" to jdkWorkloads())
