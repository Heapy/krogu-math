# Task549: null exceptions, message observations and remaining obligations

Historical source-only memo adopted after root review. Original private bytes
remain at `/private/tmp/krogu-549-npe-message-policy-audit.md`, SHA-256
`1dce32242bb672eda55f7acc026c7bbb5df500ef9886514b685b980919ee0bb1`.
The adoption changes no exception, test assertion or required rejection rule.

Finite SOURCE/policy audit, 2026-10-15. This memo creates only this private
document. It changes no implementation, test, fixture, evaluator, public document,
task or Git state. No script, compiler, helper, certificate, numerical operation,
probe, test, build or benchmark was executed. Own source/test/policy text,
retained task metadata and official public API documentation were read; no foreign
numerical implementation source/body/bytecode was consulted.

## Decision

No missing normative requirement to reproduce VM/compiler-generated NPE wording
was identified for the audited helpers. Their normalization does not by itself
leave an unmet mathematical/API compatibility requirement. It leaves historical
message observations unverified wherever the corpus never recorded the messages.
That evidence limitation must remain stated; it is not evidence of message parity.

This conclusion does not close549, classify every oracle field, authorize deletion
of an assertion or fixture, or exempt all exceptions/subclasses from review.
Required input rejection, documented exception conditions/types, explicit caller
dispatch and applicable result/exception identities remain independent obligations.

## Task chronology and governing policy

The retained `/private/tmp/krogu-549-current-task.json` contains the original body:
it requests exact result/type/message/order comparison and calls normalized NPE
messages open. Its later comment1586 explicitly limits Java source compatibility
to TCK adapters and classifies unspecified VM/compiler messages and internal
callback order as observations under526. Comment2232 reiterates that NPE wording
and internal priority remain diagnostic;2268 records focused573–578 acceptance
without closing549. Comments2249/2281/2283 record the separate conditional lexical,
rounding and byte arguments. Those later scope decisions must be read with the
body, not silently replaced by the older wording.

Current publication policy agrees:

* `COMPATIBILITY.md:1–38` states the Kotlin migration and accepted48-input NN
  migration; `:132–145` distinguishes specified conditions/types/identities from
  observed wording/internal call order; `:198–212` confines Java call shape to
  the TCK and excludes legacy/cross-format serialization.
* `docs/kotlin-nullability-policy.md:3–9` requires NN rejecting inputs, permits
  matching Kotlin caller/override edits, retains three nullable equals inputs,
  and preserves/classifies displaced invalid-input observations.
* `docs/non-null-575/SOURCE-HANDOFF.md:73–85` keeps VM wording, internal callback
  timing and malformed replacement arrays competing with null as diagnostics;
  valid-input invalid-return/array controls remain active.
* `docs/non-null-578/SOURCE-HANDOFF.md:42–77` separates otherwise-valid ordinary
  null obligations from competing-invalid observations and distinguishes real
  Java super, virtual reflection and direct caller-owned overrides.

The575/578 handoffs' original SOURCE-only labels are historical milestones.
Current acceptance comes from the573 verification and later task feed, not a
claim that this audit compiled their sources again.

## Public API basis

The [BigInteger class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
and [BigDecimal class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
specify NPE for null object inputs. Method-specific contracts and the adopted
nullable equals policy still apply; this is not a demand to reject equals(null).
For otherwise valid arguments reaching a rejecting library entry, the required
outcome is the appropriate NPE category, not a VM-selected English sentence.

[MathContext constructors](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/MathContext.html)
separately specify invalid-precision/grammar IllegalArgumentException and null
rounding/string NPE. [RoundingMode.valueOf](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html)
specifies the invalid-name/legacy-integer IllegalArgumentException categories
and null-name NPE. These clauses supply conditions/types, not exact error text or
a general ordering rule for inputs violating two independent preconditions.

The [NPE.getMessage contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/NullPointerException.html#getMessage())
explicitly allows implementation-specific text or null when no non-null message
was supplied. It returns the supplied message when a non-null message was passed
to the constructor. Thus null-dereference/helpful-NPE diagnostics need not match
across HotSpot and Kotlin parameter checks. Merely fixing one VM configuration,
including fast-throw flags, makes an observation reproducible; it does not turn
the observation into a public math guarantee.

## Exact current helper classifications

| Current own source | What is actually asserted/retained | Scope and limitation |
|---|---|---|
| `src/commonTest/kotlin/io/heapy/krogu/math/UnaryOracleEvaluator.kt:3–11` |576-selected null rows cannot enter this common evaluator; its generic outcome formatter drops the message of any caught NPE while preserving non-NPE messages. | This does not compare NPE text, identify its origin or establish the classification of every remaining row. Valid numerical fields remain separate. A future row involving a caller-created NPE must be classified from its input/entry, not automatically waived by this formatter. |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalContextDispatchOracleTest.kt:45–65` |Formatter drops NPE text, retains other text and trace.384 null-input rows are moved to Java-boundary replay; retained rows still have their frozen comparisons. | Normalization supplies no NPE-message parity. It does not authorize blanket removal of traces or relabel all75,728 outcomes; the526 field policy remains separate. |
| `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalUnarySubclassTest.kt:30–54` |Generic formatter drops NPE wording. Selected null-context keys print host/port outcome/trace and require own NPE; non-null keys retain result/trace comparisons. | The null partition has a real576 Java boundary, not a Kotlin null cast. Its required NPE is not an assertion that an inherited host callback must happen before rejection. Non-null override behavior is not waived by that null classification. |
| `src/jvmTest/kotlin/io/heapy/krogu/math/NullPrecedenceOracleTest.kt:18–40,44–94` |Captures actual message separately for output. Otherwise-valid inputs require NPE; compound-invalid keys require rejection. Frozen host rows are consumed and counted; zero/null old observations are explicitly retained.705 ordinary and2,757 compound keys are accounted within3,462 rows. | It does not assert exact messages or a preferred exception category for every compound key. A broad Exception rejection assertion does not itself establish each documented isolated invalid-input category; those need their own valid-other-input controls/proof. Different frozen observations are evidence, not silently rewritten expectations. |
| `src/jvmTest/java/io/heapy/krogu/math/JavaApiContractTest.java:64–79` |Otherwise-valid random/null entries, including zero bits, require NPE. Three invalid-size-plus-null cases use `randomDiagnostic`, print old type/current type/message and require an Exception. | These three diagnostics do not prove isolated negative-numBits IllegalArgumentException or bitLength<2 ArithmeticException with a non-null RNG. They also do not freeze invalid-null priority. |

These are classifications of what the present tests prove, not new runtime passes.
The formatters' catch of NPE is broader than the VM-generated subset. Therefore
the policy reason must accompany each selected input class; normalization alone
cannot distinguish a library boundary NPE from a callback-created NPE, a returned
null later formatted, or a test setup error. Existing575/578 adapters and nullable
target-result handling explicitly address those attribution risks in their owned
rows. This audit has not independently proved every helper or adapter in the repo.

## Conditions, priority, caller exceptions and traces

1. **Otherwise valid rejecting entry plus null:** require NPE at the real library
   boundary. Preserve the NN Kotlin declaration and permitted migration edits.
   Distinct language/compiler wording is diagnostic. Library acceptance of null
   cannot be justified because a pinned host accepted it on a shortcut path.
2. **One isolated non-null invalid argument:** preserve the documented rejection
   condition and exception category, including parser/radix/array bounds, invalid
   precision/legacy mode, nonfinite construction and inexact UNNECESSARY. An NN
   migration does not excuse NumberFormatException/IllegalArgumentException/
   ArithmeticException/IndexOutOfBoundsException confusion on these valid-other-
   input paths. Diagnostic classification of a message does not waive the error.
3. **Two independently invalid inputs:** retain the exact key, old and current
   observations and rejection requirement. No universal JDK priority is inferred
   merely from an implementation's validation order. If a particular public
   clause expressly fixes priority, cause or wrapping, that clause remains
   mandatory and needs a specific binding; this audit found no such general
   null-before-invalid or invalid-before-null rule in the cited API clauses.
4. **Caller-owned explicit override/exception:** a direct overridable call still
   invokes the caller's override. Existing direct-caller controls must preserve
   its arguments and applicable result/exception identity, including a marker
   NPE or another exception deliberately constructed with a message. Do not
   replace a caller's object or discard its supplied message under a claim about
   VM-generated NPEs. A language-legal Java override accepting null may receive
   it on that direct call; inherited base/super NN entries are a different route.
5. **Library's incidental internal call path:** no new requirement to invoke an
   override, to invoke it a certain number of times or to expose a certain order
   follows from the mere fact that the method is open. Documented delegation and
   correct leaf/numerical contracts still need their own proof. Truthful subtypes
   cannot be excluded wholesale; a poisoned getter, replacement or callback must
   be classified specifically. Bad subtypes do not waive unrelated ordinary data.
6. **Library-defined text/cause:** current math/API clauses audited here generally
   specify a condition/type rather than exact wording. This is not an inventory
   proving that no API ever specifies text/cause, and it is not authorization to
   erase deliberate library diagnostics or current message assertions. Keep
   existing non-NPE wording comparisons and any specific message/cause guarantees
   until individually classified. Documented canonical result text, enum names
   and MathContext string syntax are value contracts, not exception-message
   observations, and remain required.

## Precise remaining549 work after the accepted finite milestones

The27-constructor inventory (`docs/constructor-overload-inventory-549.md:9–16`)
accounts for shape: eight BI, sixteen BD, three MC, no public RM constructors,
plus two separately identified Kotlin Random factories. Its accepted three
ConstructorRandom549 methods provide finite common evidence; the introduction
records their later four-target acceptance. Neither shape nor those three methods
is the full public API/error matrix. In particular:

* Finish a per-entry/per-precondition ledger for the remaining public methods,
  factories and constructor error branches across all four types, identifying
  required type/condition, valid-other-input evidence/proof, current NN boundary,
  direct-caller controls and diagnostic-only competing-invalid fields. The
  complete constructor inventory, finite suites and accepted lexical/byte/
  contextual-rounding proofs are inputs to that ledger, not reasons to repeat
  their covered work or announce all methods proved.
* Keep isolated bridge-side random-size categories distinguished from the
  three Java compound diagnostics. The inventory:151–174 explicitly distinguishes
  common Kotlin Random factories from PlatformRandom constructors; the current
  common three-method acceptance does not by itself prove every JVM bridge
  invalid-input path. This is a coverage/accounting obligation, not a newly
  established implementation defect or an automatic demand for another test.
* Retain the conditional proof assumptions: UTF-16 digit mapping, exact supported
  integer leaves, stable arrays, platform primitives and successful required
  allocations. The proofs of arbitrary lexical forms and byte representations
  exceed their finite corpus counts, but do not settle all548 resource/range or
  all541 subtype compositions. The later parser/radix and580 changes require
  honest source binding; their accepted reports must not be relabelled as fresh
  execution of older constructor memos.
* Resolve/record the two-argument BigInteger/context constructor scale-zero
  wording tension under526. The accepted rounding argument and16-call diagnostic
  establish significant-digit behavior under their stated assumptions; they do
  not silently satisfy an unconditional literal final-scale-zero interpretation.
* Preserve the four original null-plus-invalid char-range calls as adaptation
  losses/historical failures. Non-null adapted ranges and the separate3,462-row
  matrix do not reproduce those exact calls. Lack of a specified priority means
  they are not automatically four missing mathematical regressions; the exact
  coverage/diagnostic classification still belongs in the ledger.
* Keep wider Kotlin call-site/overload/operator/extension coverage with525/557,
  range/allocation with548, subtype dispatch with541, and mixed oracle-field
  classification with526. Real Java new/constructor references/super source
  shape is required only where the TCK adapters need it. No legacy proxy, old
  binary format or java.math stream interchange obligation is revived.

For the NPE-message item specifically, the appropriate remaining statement is:
**historical exact wording unverified where normalized; no additional mandatory
VM-wording compatibility gap identified under the accepted policy**. A parent
task-body clarification could state this chronology while keeping549 in_progress.
This memo neither edits the tracker nor authorizes closing it.

## Exact read-time pins

| Input | SHA-256 |
|---|---|
| private current549 task/feed JSON | `14f56f2e77b128146f4804a87cdd8bbe82b23c204cbbd39c842938210ed18e88` |
| COMPATIBILITY.md | `de494ec98579de48f31eead6185bfdb237a9461a5a235f5ea00deb8e5e224a81` |
| docs/kotlin-nullability-policy.md | `4726939101b821569afe4f879bdabd0fb2957789f20a11abbe8bc1b48360efa1` |
| docs/non-null-575/SOURCE-HANDOFF.md | `580922367059542b2ab6384f1b81203a49e39cb55d3e6d98211ead5393829af8` |
| docs/non-null-578/SOURCE-HANDOFF.md | `10f60032ac41ec2f3abe7f19bfc160ff5f80f5555d1d7696affaef877dbba48f` |
| docs/constructor-contract-549-coverage.md | `2b758ac6be30096ae54bb676a4c1d98abe9dd9da1c76de2ebf8230560214c174` |
| docs/constructor-overload-inventory-549.md | `4d4b29dabe64335753c061085a721b08941f72b0c3bdf065a183e27d1f36b60e` |
| docs/contextual-constructor-rounding-549-proof.md | `b801d4e0777d763b445c4f742ddccbcccc8f7245b62c807947c6d55d2525fce6` |
| docs/integer-byte-constructor-549-proof.md | `6b112b1c67e687e5ee69c24d07d912ec567ccc6eeab4e19e62e6277758e214e3` |
| UnaryOracleEvaluator.kt | `9e5779dc42889dfc04558ee504ceb379caa26addded31c5aee35a79868bd330d` |
| DecimalContextDispatchOracleTest.kt | `bb61d3f8a5f61850fb208f0e8f5ba814c4b3c7c68b0cb658a0db62081ea41300` |
| DecimalUnarySubclassTest.kt | `02c8c0baff56978edd43d22bfb8e9bc3107d112fa641b6f06f3e261b85525be4` |
| NullPrecedenceOracleTest.kt | `cd996aabc597ec1ddee157c079aa2bb1796e06e5c295fdc6595bb09987211757` |
| JavaApiContractTest.java | `169894cb303ee9de556ed94595a92e237ef71f27dc7016d4ef0bbf50350a8d50` |
| current BigInteger.kt | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| current BigDecimal.kt | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| current MathContext.kt | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |
| current RoundingMode.kt | `e47d7aa314ddaa9c79ba13b79a00beadb8be96cbb473cd4ad2dfae3e0501a5ee` |

All historical source hashes inside the accepted proofs retain their historical
meaning. This read-time table is provenance, not an independently executed
revalidation of all those proofs or any hosted result. Finite memo complete;
no future runtime window is requested or awaited.
