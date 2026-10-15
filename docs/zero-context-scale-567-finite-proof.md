# Task 567 finite residual policy proof

Native worker `099c66de`; parent `af5f5ab4`, Codex thread
`01a13482-0bb7-7752-b638-2ea87e5bcafb`. SOURCE ONLY. **UNCOMPILED / UNRUN.**
Changes are not committed. Task stays linked/in_progress on
`task/567-zero-context-scale`, HEAD `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`.

This milestone supersedes only the unresolved residual classifications in the V2
proof/handoff. Their historical source and runtime claims remain unchanged.
Parent comment2026 and `/private/tmp/task567-finite-residual-policy-source-grant.txt`
accept chosen base behavior versus reference callback observations. This is neither
another specification bug nor a numerical guarantee for nonconforming subclasses.
No D07457/C0625 attribution or task526 archival decision is reopened.

## Preserved evidence and actual coverage

Before any edit, all `src`, `docs`, `scripts`, `tools`, build files and wrapper
were copied into a unique read-only archive:
`/private/tmp/krogu-567-finite-original-099c66de/v2-source.tar`.
SHA256 `bb7a58635bb3ce9679fcb5c18c246aa21fb4b53cba0068a20081fc499a52866c`.
`original.sha256` beside it records the following original file hashes:

| Original file | SHA256 |
| --- | --- |
| BigDecimal.kt | 9cbaef673f966579e52f44bb9f6bc7c3b01f3ef9adae412f9d96c86c063a1446 |
| NumericExtensions.kt | 20fdf7c5c76ed4207c03859b4cb60247a16e2ec4337ee674c3a85ce6101e5123 |
| ZeroContextScale567Test.kt | 662cc1f3c913ddfa0d44c481b2c38548419fc1a2a5887e5abe09ec03ef647f04 |
| PortableOracleTest.kt | f522ed277c79c0b73cb7f1a9e07893602626cf1a1a92459faa60ff5424ad2580 |
| DecimalRoundSubclassTest.kt | 3ffe301bf9049aff297bc93e937c103f089e76efffef228df34936df795b8b9a |
| DecimalUnarySubclassTest.kt | afcf18525c48a0229cd967813a657dad6247e63e32735aebb8652d9bb8e89760 |
| DecimalNullUnarySubclassTest.java | c8041e66fcaf48a85fc3f09863e265c2694c06e5be3364706ec068359a0c27e0 |
| V1 proof | 5054a2a9110d41c8ad4f8226c1967ee9b67da5f23eb277e40166d2351cd1c4ac |
| V1 handoff | ab6f38b9701758f23df41655127071ad540ae67c321dc227892a69073f19ba76 |
| V2 proof | b12ea9ac19fd073735595149f63af13777a1e9b3b96c8648c96b20661c24547a |
| V2 handoff | e689bbbbefa224c48f120dc86d1aecff93b6d374f49abdb258220a7b43483e8a |

Other unchanged V2 source hashes, already recorded in the archived V2 handoff:
ScaleBoundaryOracle `f7fe6fc8cb8575c48161cde769a884845918c1ea887f67766d1607ae4fd5c18b`;
DecimalMultiplicationSubclass `e56c25895a07714c52dd25128f83081a78f85a04262a546b43020c4a6ec53559`;
ScaleBoundaryConformance `c756a1f609c99b6889acafba3ad63d7a10856cdbfecf05c64b0476b26eea34e6`.
The full archive also preserves the previously unmodified UnaryOracleEvaluator.

V1 immutable runtime: 115 suites,391 methods,382 PASS,9 FAIL; all9 new PASS.
V2 immutable runtime: 115 suites,394 methods,390 PASS,4 FAIL; all12 new PASS;
zero errors/skips/missing/extra/duplicates. Fresh V2 JAR
`64255072cdaea0e8815d6c77be68539b09ceb8dd24917b02099761d19321ed62`;
identical source/build manifests prefix `5df14a0d`.
`/private/tmp/krogu-567-v2-root-runtime/EVIDENCE.sha256` SHA256
`1280ad1b27c889f74d73e1197d6d4ed2697c287587e6b20a6258e37dedb83409`.
Portable reported212 mismatches (first80 printed), liveRound180 (first20),
liveUnary24 (first16). Java aborted on its FIRST failure; ten Java failures
were not observed. Those ten are source-selected until a complete future replay.
No fresh runtime evidence is claimed by this milestone.

## Exact input sets and independent requirements

Portable full key is `(family,operation,text,argument,precision,mode)`.
`FiniteZeroPolicy567` materializes these sets. Membership never inspects output.
Each consumer rejects duplicate selected keys and asserts equality of the observed
set with its entire expected set. Portable also guards sizes180/12/12/8.
Live round and unary map their original loop inputs to the same six-field keys.
Every selected row prints its complete frozen/live host outcome, flags, trace and
actual outcome, flags, trace, without capped diagnostic output. Original capped
failure summaries, all loops and all other comparisons remain.

1. **S180 / liveRound180.** Behavior `{super,sequence}`, all8 modes. Stored
   text `0.00`: report `{-1,0}` × p`{0,1,7}` gives96; report3 × p`{0,7}`
   gives32; report9 × p`{0,1}` gives32. Additional20: report9,p1,
   behavior `{super,sequence}`, text `-123.456` with
   `{DOWN,CEILING,HALF_UP,HALF_DOWN,HALF_EVEN}`, or text `999.99` with
   `{DOWN,FLOOR,HALF_UP,HALF_DOWN,HALF_EVEN}`.
   Require canonical `0`, self=false, base=true, and EXACT old trace.
   Stored zero proves coefficient zero independently of poisoned precision/sign.
   For the twenty nonzero inputs, the first false report9 at p1 discards8 digits:
   |-123456| and99999 are below half of100000000. The selected directed modes
   do not increment the quotient; all selected half modes likewise return zero.
   Intermediate scales are3-8=-5 and2-8=-6. Base contextual plus then chooses
   canonical0@0. This explains the chosen base behavior; it is NOT correct
   rounding of the original nonzero values. Sequence's later return1 cannot
   change this bounded first reduction. Replacement/throw/other modes stay raw.
2. **C direct12 / liveUnary direct12.** `plusContext:{-1,1}`,stored`0.00`,
   reserved argument field`0`,
   p`{0,1,7}`,modes`{HALF_EVEN,UNNECESSARY}`. Require0@0/canonical0,
   self=false/base=true and exact original `plusContext;` trace. Stored coefficient,
   not the poisoned virtual sign, proves zero. Existing truthful six stay separate.
3. **C abs12 / liveUnary abs12.** `absContext:{0,1}`,stored`0.00`,
   reserved argument field`0`,
   same six contexts. Require unchanged0@2/`0.00`,self=true/base=false and
   receiver coefficient0/scale2. Retained host must be exactly
   `0.00|true|false|sign;plusContext;`. Current exact trace is `sign;`:
   initial sign selects the nonnegative branch; private precision-only seam
   preserves cohort and receiver while bypassing internal virtual plus.
4. **C nullAbs8.** `nullAbs:{return,self,throw,super}`,stored`0.00`,sign`{0,1}`,
   p0,HALF_EVEN (these key context fields identify a null call). Require exception
   TYPE NullPointerException and exact `sign;` trace; coefficient0/scale2 unchanged.
   Context validation occurs after the initial sign, before precision or plus.
   Full host return/exception/trace remains printed and frozen, separately.
5. **Java nullAbs10, source-selected.** Full loop key
   `(text,sign,behavior,abs)` = `0.00` × `{0,1}` ×
   `{return,self,null,throw,super}` × `true`; context is always null.
   Require the same exception type/trace and immutable receiver pair.
   Host value `42.00`, self `0.00`, null, callback exception or NPE and full
   `sign;plus:true;` trace remain separately captured on future replay.
   All abs=false, negative-branch and other-text keys retain exact raw comparison.

Portable's new disjoint sets total212, matching the reported mismatch COUNT.
That is a source explanation, not proof that every individual residual replayed.
Live unary's12+12 explain24. Java's ten are expressly source-only selections.
Existing ordinary/truthful D5/L160/S48/C6 classifiers, constructors, cursor/final
counts, H assertions, frozen fixtures and generators remain untouched.

## Separate independent controls prepared before acceptance

`ZeroNullForwarding567Test.java` adds ONE method: Java stored-zero plus override
returns null for null/p0/p1/p7 contexts. Direct plus, inherited round and explicit
caller-owned super.round must return that exact null, each with one exact plus
trace; stored pair stays0@2. Returning null violates the override's value contract;
the test protects explicit Java return/dispatch only, with no production ABI change.

`ZeroUnaryPrecision567Test.kt` adds TWO methods. The first covers abs on the zero
receiver and negate on a distinct receiver returning that zero: all8 modes,
p0/1/7. Positive p must throw the SAME precision sentinel object; p0 returns the
SAME zero without precision callback. Null context still performs initial sign or
negate, then NPE without precision callback. The plus override fails if invoked.
Both stored receivers remain immutable. This protects the precision kernel seam.

The second covers ordinary and truthful precision-subclass counterparts for both
nonzero texts at p1/all8 modes. Literal coefficient/scale pairs:
-123.456 => -2@-2 for UP/FLOOR, -1@-2 for other five non-UNNECESSARY modes;
999.99 =>9@-2 for DOWN/FLOOR,1@-3 for other five non-UNNECESSARY modes.
UNNECESSARY throws for each because discarded remainder is nonzero.
For the negative input the remainder is below half; for the positive input it is
above half and carrying9 to10 reduces to1@-3. These expected pairs are literals,
independent of host output or false precision. Receiver pairs are preserved.

Frozen twelve-method common file and production are unchanged. Prepared inventory
adds3 methods to V2's394: expected397 methods /117 suites on later full JVM replay,
subject to independent source inventory. No compilation or behavioral validation.

## Review and next gates

Independent read-only actual-diff/predicate/control review is required before the
final handoff. Its terminal disposition and final source/doc hashes are recorded
in the new finite handoff and task comment. No scripts, helpers, numerical work,
compiler/build/test/probe/generator/benchmark or foreign numerical implementation
bodies are used. Root2024 released its runtime window; idle is not authorization.
Later separately authorized replay must retain every selected complete raw row,
finish original loops, and produce fresh artifact/full retained JVM then JS/Wasm/
macARM and supported exact-head CI evidence. All those gates remain open.
