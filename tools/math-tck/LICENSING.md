# Public math test licensing boundary

The pinned inventory contains 56 OpenJDK test/subtree support files and one
external support file, `test/lib/jdk/test/lib/RandomFactory.java`. Each of the
57 files has been checked for the header specifying **GNU General Public
License version 2 only**, and for the absence of a Classpath exception.
Their classification is **GPL-2.0-only**, not GPL with Classpath Exception.
The per-file hashes, copyright notices and license classification are in the
generated inventory; the hashes are also frozen in `source-lock.tsv`.

Downloaded originals, rewritten derivatives, eventual patches replayed
onto those derivatives, and compiled test/combined harness artifacts belong
only under ignored `build/math-tck`. Original notices stay unchanged in each
generated Java file. Any future source-specific patch carrying upstream text
must be identified as GPL-2.0-only; it must not acquire an Apache header.
`adapt.main.kts` contains the pinned source-specific transformation rules and
is explicitly **GPL-2.0-only**. Generated source-specific changes preserve the
original header and are recorded per file with an adapted-source hash. There
is no copied complete test source in the tracked tools directory.

The independently authored metadata/orchestration scripts and Java helpers
(`Launcher`, `CoreGuard`, `ArrayAccessor`, `HostBridge`, `ChildJvm`, `ChildEntry`) are marked
Apache-2.0. Importing the GPL adaptation module does not relicense it or its
output as Apache-2.0. This does not relicense OpenJDK sources
or their transformed output. The production Kotlin implementation remains
independent Apache-2.0. No downloaded test source, derivative, compiled test,
test-support binary or combined harness binary may be included in library
sources, published jars, source jars, native artifacts or documentation bundles.
This harness has no connection to those publication tasks.

## Pinned framework and runtime dependencies

`dependency-lock.tsv` pins jars **and POMs**, official Maven Central checksum
algorithms/values and locally computed SHA-256 hashes. Official SHA-512 is used
where supplied; several older artifacts publish SHA-1 only. Their stronger
SHA-256 lock was computed after matching that official checksum over HTTPS.
No sources or class implementations are inspected; only POMs and bundled notices
are read after the binary hashes pass. Every verified artifact stays in ignored
`build/math-tck/lib`. Notice entry names and extracted text stay in
`build/math-tck/notices`.

| Artifact | License evidence |
|---|---|
| TestNG 7.11.0 | POM and bundled Apache-2.0 notice |
| SLF4J API 2.0.16 | Bundled MIT copyright/license |
| JCommander 1.83 | POM Apache-2.0 |
| jquery WebJar 3.7.1 | POM MIT; TestNG's non-optional reporting dependency retained |
| JUnit console standalone 1.13.4 | POM/JUnit EPL-2.0 plus bundled JUnit4 EPL-1.0, Apache-2.0 and Hamcrest BSD-3-Clause notices |
| Kotlin stdlib 2.4.21 and annotations 13.0 | POM Apache-2.0 |

The standalone JUnit jar supplies the actual Jupiter/Platform/Vintage engines,
OpenTest4J, API Guardian, console dependencies and their notices as one pinned
artifact. TestNG's non-optional POM closure is SLF4J API, JCommander and jquery;
its optional Guice/YAML dependencies are unused (no injection/YAML runner).
Kotlin stdlib requires only annotations in its ordinary POM dependencies;
dependencyManagement entries are not extra runtime dependencies.
Libraries, GPL tests and combined test binaries are not distributed or included
in production publications. The bundled EPL-1.0 material makes a later combined
distribution a separate licensing decision; this local harness does not authorize it.

Reference: [GPL version 2 text](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
Verbatim official text is retained in `COPYING-GPL-2.0.txt`, downloaded from
`https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt`, SHA-256
`edaef632cbb643e4e7a221717a6c441a4c1a7c918e6e4d56debc3d8739b233f6`.
For an eventual standalone distribution of the derived harness, retain the GPL
text, notices, source and modification provenance together and review that
distribution separately. This task currently prepares a local test workspace.
