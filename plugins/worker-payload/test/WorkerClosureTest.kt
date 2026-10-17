package workerpayload

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkerClosureTest {
    private fun fixture(body: (Path) -> Unit) {
        val root = Files.createTempDirectory("krogu-worker-closure-test-")
        try { body(root) } finally {
            Files.walk(root).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::delete) }
        }
    }

    @Test fun retainsCompleteFlatClosureAndExcludesSourceMaps() = fixture { root ->
        root.resolve("worker.mjs").writeText("import './worker.import-object.mjs'")
        root.resolve("worker.import-object.mjs").writeText("export default {}")
        root.resolve("worker.wasm").writeText("wasm fixture")
        root.resolve("worker.mjs.map").writeText("source map fixture")
        assertEquals(listOf("worker.import-object.mjs", "worker.mjs", "worker.wasm"),
            workerClosureFiles(root).map { it.fileName.toString() })
    }

    @Test fun rejectsNestedRuntimeInsteadOfSilentlyDroppingIt() = fixture { root ->
        root.resolve("worker.mjs").writeText("import './nested/dependency.mjs'")
        val nested = Files.createDirectories(root.resolve("nested"))
        nested.resolve("dependency.mjs").writeText("export default {}")
        assertFailsWith<IllegalStateException> { workerClosureFiles(root) }
    }

    @Test fun rejectsUnsafeNames() = fixture { root ->
        root.resolve("worker with spaces.js").writeText("fixture")
        assertFailsWith<IllegalStateException> { workerClosureFiles(root) }
    }
}
