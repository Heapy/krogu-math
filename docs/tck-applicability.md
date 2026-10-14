# Official TCK/JCK applicability and access evidence

Audit date: **2026-10-14**. Task: `local:552`. Repository baseline:
`11af6b92b8337f0a4a291184a9db21efe6dc7fe3`.

**Official TCK/JCK has not been run or passed for krogu-math.** This report
establishes an external access/applicability barrier under the presently
documented routes, rather than a passing result. It makes no certification claim.
The independently implemented Apache-2.0 library is outside the implementation
conditions of the published OpenJDK community route. No alternative agreement
authorizing this library, licensed JCK 25 payload/User Guide, or authorized
namespace/subset/harness configuration has been established in this task.
Whether Oracle could offer a suitable separate agreement remains unresolved.

## Project and compatibility scope

[README](../README.md), [NOTICE](../NOTICE), [LICENSE](../LICENSE) and
[COMPATIBILITY](../COMPATIBILITY.md) describe independent common Kotlin arithmetic
in `io.heapy.krogu.math`, under Apache-2.0, targeting the public Java SE 25
numerical contract. This is a math library, not an implementation of the Java SE
platform. These project facts are the basis for the applicability comparison;
this audit did not inspect external numerical implementation source.

The required migration remains **Java -> Kotlin using java.math -> Kotlin using
krogu-math**. Full mathematical behavior and Kotlin caller contracts remain
required and unproven as a whole. Java source call shapes are required only in
the TCK harness, where test-only adapters may provide constructors, factories
and subclass call shapes. They do not constrain production types to Java classes
or prohibit Kotlin interfaces/factories. Legacy/cross-format serialization is
excluded from the project's compatibility requirements. That exclusion does
not authorize dropping any test from an official kit: any selection would need
its own permission and must describe its limited scope accurately.

## What the official kit covers

Confirmed: [JSR 400, sections 2.1 and 2.15](https://jcp.org/en/jsr/detail?id=400)
identifies Java SE 25 as a platform specification and its TCK as **JCK 25**.
[Oracle's JCK 25 accessibility report](https://docs.oracle.com/en/corporate/accessibility/templates/t2-16569.html),
“Product Description” (report dated 12-JUN-25), describes tests derived from
Java SE 25 platform specification statements. It describes JavaTest configuration,
command-line execution and some interactive tests. Its “602.2 Accessibility and
Compatibility Features” says the User Guide is bundled with the product.
This is evidence of a platform kit, not a public math-only manifest.

The [Java SE 25 java.math package specification](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/package-summary.html)
lists BigInteger, BigDecimal, MathContext and RoundingMode. It specifies the
package's purpose; it does not publish an official TCK test list or a license
to run a renamed subset. The exact JCK 25 math test inventory, coverage,
exclusions, mandatory configuration and permitted mapping are **unverified**
without the applicable licensed kit and guide. No test count is asserted here.

### Standalone math kit investigation

No currently accessible official standalone `java.math` TCK for this independent
renamed library was established. This is a bounded research finding, not proof
that no such kit or agreement can exist.

The historical [JSR 13 Decimal Arithmetic Enhancement page](https://jcp.org/en/jsr/detail?id=13)
mentions a potential CTS and IBM test cases in its original proposal (sections 2
and 3); that historical statement does not establish a current licensed SE 25
kit. Its [Final Release page](https://jcp.org/aboutJava/communityprocess/final/jsr013/index.html)
lists J2SE 5.0 and sends its TCK link to the same Java Partner portal. Neither
page establishes a separately downloadable current math-only kit. No linked
IBM binaries, tests or implementations were downloaded or read.

The words “Stand-Alone TCK License Agreement” in a license title concern the
agreement offered for a specified TCK. They do not themselves identify a
standalone math test suite; the linked agreement's exhibit identifies a platform
specification, as discussed below.

## Access routes and their limits

### OpenJDK community route: concrete scope mismatch

[Gaining Access to the JCK](https://openjdk.org/groups/conformance/JckAccess/index.html),
opening paragraphs, “Process” and “Requirements for JCK access”, offers no-charge
access for compatible OpenJDK-derived implementations or OpenJDK research,
fixes, enhancements and ports. It requires an active project meeting OCTLA,
application screening, signed OCTLA and OCA, then Oracle's download instructions.
Being outside the OpenJDK community does not itself disqualify a project; the
implementation/license conditions still apply.

The page links [OCTLA 4.0 for Java SE 21 or later](https://openjdk.org/legal/OCTLA-JDK21+.pdf)
(dated 15 September 2023). Relevant clauses:

| Clause / PDF page | Confirmed requirement | Consequence for this audit |
|---|---|---|
| 1.9 / 2 | The defined implementation must be substantially derived from the specification and OpenJDK code, GPL-distributed if distributed, and compatible or a good-faith step toward a compatible release. | Independent Apache-2.0 krogu-math does not match that defined scope. |
| 2.1(a) / 3 | Internal TCK use is solely for those defined implementations; distribution must be under the GPL. | This grant does not establish permission for the present library. |
| 1.1 / 1; 1.12 / 2 | Compatibility requires the full specification and applicable User Guide requirements; the kit includes related documentation. | A renamed math subset cannot establish platform compatibility. |
| 2.1(b)(ii) / 3 | Derivative TCK works are prohibited. | Rewriting test imports is not assumed permissible; guide/Oracle authorization is needed. |

**Applicability inference:** the mismatch follows from those primary clauses and
the project's independent implementation/distribution facts. It is not an Oracle
screening decision or a ruling about every possible license. Relicensing alone
would not resolve the derivation and platform-scope conditions. This audit does
not propose changing the project license or adopting OpenJDK code.

### Java Partner / separately agreed terms: unresolved

The [JSR 400 Final Release page](https://jcp.org/aboutJava/communityprocess/final/jsr400/index.html),
“Reference Implementation and Technology Compatibility Kit”, directs access to
[Java Partner](https://javapartner.oracle.com/). Its scholarship paragraph refers
to qualified individuals/not-for-profits. This identifies a route to investigate;
it does not grant this project access. The public portal response was a
JavaScript-dependent application, with no kit/guide obtained. No authenticated
access or account entitlement was tested.

[Oracle's Compatibility Testing Scholarship page](https://www.oracle.com/java/scholarship-program.html)
specifically sends qualified applicants developing an OpenJDK-derived compatible
Java SE implementation to the community process. General scholarship language
does not waive the implementation conditions. This task established neither
applicant qualification nor an award.

JSR 400 section 2.17 links the public
[SATCK document](https://jcp.org/aboutJava/communityprocess/licenses/jsr400/SATCK-JSR-400-SE-25-11.06.24.pdf).
Its filename names SE 25 and headers say September 2025, but **Exhibit A-1,
I.A-D (PDF pages 16-17) names Java SE 21 / JSR 396**, including the TCK, User
Guide and release notes. Treat it as published evidence requiring clarification,
not confirmed operative JCK 25 terms. In that document, sections 1.3, 1.7 and
1.14 tie scope to the exhibit; 1.9 requires a full-specification Product and guide
compliance; 2.1(a) limits use to developing/testing Products; 2.1(b)(ii) restricts
derivative TCKs. Those clauses do not establish permission for this library's
renamed subset. No executed agreement or corrected exhibit was obtained.

“Stand-Alone” therefore does not remove the uncertainty. Neither this template
nor its ambiguity establishes that a suitable separately negotiated agreement is
impossible. Fees, field of use, qualification, permitted independent suites and
disclosure terms must be established under the actual offered agreement; this
report does not present the ambiguous exhibit as a current JCK 25 price or offer.

## Exact prerequisites for an authorized official run

The current barrier consists of missing authority and licensed/configuration
evidence, not a failed arithmetic test. To resolve it without changing the
project's intended scope, the following would be needed:

1. **Applicable access agreement:** an identified licensee and Oracle-confirmed
   terms covering JCK 25 use for an independent Apache-2.0 KMP math library,
   with the exact specification/kit exhibit and any qualification, distribution,
   independent-test and disclosure restrictions resolved. Any outreach,
   application, payment or agreement acceptance requires separate authorization.
2. **Licensed materials:** entitled access to the exact JCK version/update,
   User Guide, release notes, official exclusion lists and required harness/tools.
   A public license template or an installed reference JDK is not this payload.
3. **Written/guide-supported applicability:** whether package-level math testing
   is allowed; the authorized inventory, exclusion reasons and meaning of the
   result; whether `java.math` can be mapped to `io.heapy.krogu.math`; whether
   source/bytecode transformations or test-only Java facades are allowed; and
   how to avoid testing the host JDK's math instead of this library. No mapping,
   rewriting or subset is presumed authorized here.
4. **Reproducible harness:** after permission is established, record library
   revision/artifact hashes, kit/update and authorized configuration, adapter
   source/configuration, test selection/exclusions, harness and JVM/OS/architecture,
   launch commands and class/module loading evidence. Record runtime differences
   from the project's BellSoft Liberica 25.0.4+9 reference. Keep Java adaptation
   confined to test infrastructure.
5. **Results and claim boundaries:** retain complete outcomes, failures and
   reruns in the location permitted by the agreement. Publish only permitted
   evidence. State the tested scope accurately; a permitted math subset would
   not itself demonstrate full Java SE platform compatibility, KMP target parity,
   Kotlin source guarantees or trademark/certification rights.

## Research and verification record

All URLs above were checked on **2026-10-14**. The task's earlier read-only audit
was used as a lead; the documents and clauses were independently rechecked.
The browser research tool could retrieve the JSR 400 detail/final, scholarship,
JSR 13 detail and java.math API pages. It failed on some OpenJDK/Oracle pages
(403/cache miss); direct read-only `curl --fail --location` successfully fetched
the OpenJDK access page, Oracle accessibility report, JSR 13 final page and both
public license PDFs. These retrieval failures are not evidence of TCK ineligibility.

Primary-domain searches (`oracle.com`, `jcp.org`, `openjdk.org`) used the phrases
`"java.math" "TCK"`, `"standalone" "java.math" "TCK"`, `"math TCK"`,
`"java.math" "JCK"` and `"Java Compatibility Kit" "25"`. The historical JSR 13
lead was followed to its official final-release TCK link. No accessible current
standalone math kit was found within that search; an authenticated catalog was
not inspected. Search absence cannot establish universal nonexistence.

The public PDFs were extracted with `pdftotext -layout`; relevant definition,
grant and exhibit pages were also rendered with `pdftoppm` and visually checked.
Downloaded document fingerprints (SHA-256, for identifying this audit's public
documents; they are not TCK payload hashes):

| Public document | SHA-256 |
|---|---|
| OCTLA-JDK21+.pdf | `3cf6cf66752b375d3263fbff593e92587e6f8aac32a40ce9880e801201e95979` |
| SATCK-JSR-400-SE-25-11.06.24.pdf | `a066a6bda29fb38b30b6ec9568b4a377bd674784804d6322f5afa35e2306f326` |

Copies were used only as temporary research inputs, not added to this repository.
The report paraphrases the clauses; consult the linked documents for full terms.

Repository checks verified the expected worktree, branch
`task/552-tck-applicability`, baseline HEAD and initially clean status. Ref-less
`kotgent task show` identified `local:552`, `in_progress`, with session
`85664731` as the sole linked worker; the project was active. No repository
AGENTS.md/CLAUDE.md was found; the supplied instructions and CONTRIBUTING.md
were followed. Semantic search reported no repository index; known documentation
was read directly. The final documentation diff, local link target and citations
were reviewed, with `git diff --check` used for whitespace validation.

No Gradle, Native compilation, numerical workload or official TCK was run.
No licensed payload or non-public guide was downloaded; no agreements were
accepted, applications filed or external contacts sent. No OpenJDK, Kotlin
stdlib or third-party numerical implementation source was read or copied.
The delegated research phase did not use Git add/commit/push/merge/checkout/reset.

## Task outcome and continuing obligation

For `local:552`, this report supplies the conditional **external-barrier evidence**:
the community route's specific implementation/license mismatch and the missing
alternative agreement, kit/guide and authorized mapping/configuration. It does
not claim every possible route is closed. Review may accept that evidence branch
without recording an official passing run.

Own differential suites, custom mathematical oracles and existing test-only Java
fixtures remain separate compatibility evidence. They are not official TCK/JCK.
The full mathematical and Kotlin migration objective remains active; this task's
access conclusion neither narrows it nor proves it complete.
