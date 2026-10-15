# Decimal normative finite controls — task541

The six new common methods passed all four complete local target inventories.
The exact tested sources are adopted in root; all six CI jobs pass on published
de5d2a17bae64feee587fe119eabf519891ce1b5. This is a finite audit tranche,
not completion of task541 or its G1–G6/P1–P6 obligations.

The isolated snapshot is based on ec195b2cb672ad774c5aaebe05be516e51575fd5.
All213 accepted V4 source files remain byte-identical; only two common test
classes were added. Full215-source manifest:
`e4a136545f2fe00f0f3c9a9e80fd6f6a54fbad96b04dc155e0992cbb83c52962`.
The224 selected source/build inputs initially hash to
`88150292eb44172ec5dc581a34db73aa64ac67e95171d10e2be3b821a84ebc04`;
the separate225-file preparation manifest also includes gitignore.

Test source hashes:

```text
2dfbbf27cad7cca7c59c3d0b210b04e75290c7c6be68c392d68be6d2f926d060  DecimalOverloadDispatch541Test.kt
196333f2dca246fc213c1fb4e92983d85a9470260121edaa7d694d0823f0013e  DecimalUlp541ContractTest.kt
```

All39 inventory entries have named finite call sites. Ordinary/truthful
coverage, selected actual Kotlin super entries and direct caller-owned
argument/result/exception identity checks have explicit scopes. New remainder
examples cover zero; retained older541 tests provide signed nonzero remainder,
rounding-mode, quotient-cohort and error controls. Neither universal internal
dispatch nor arbitrary coefficient/scale/resource domains are proved here.
The separate ulp argument covers the base unit coefficient at every stored
Int scale and no-context plus receiver identity.

Two earlier source drafts were rejected for incorrect literal scales/signs,
a nullable override and overstated coverage. They were never compiled/run
and remain preserved in their separate V1/V2 directories. Parent revision3
corrected the five residual expectations and received independent source
acceptance. The historical ulp handoff's11196333... pin was a transcription
error; the original and adopted copy both rehash to196333f2... above.

Pinned Kotlin2.4.21, Liberica25.0.4+9-LTS and Gradle9.8.1 execute serially.
The source-derived auditor is3d4e874a38ebdeca1d1dd222a6d5d66a82a2c82d0439b008fe888cdc30a89b51.
Its parser/status/missing/extra/duplicate rejection rules and old inventories
remain unchanged; exactly two classes/six methods extend the prior counts.

| Target | Suites/methods | Status |
| --- | --- | --- |
| JVM |145/498|PASS|
| JS |65/314|PASS|
| Wasm JS |65/314|PASS|
| macOS ARM64 |65/314|PASS|

Independent actual review accepted all four completed gates and their six
new methods, with zero failures/errors/skips or inventory anomalies. JVM
completed in1m11s, JS in6m15s, Wasm in1m4s and Native in3m1s. In total,
498+314+314+314 gives1440 method executions across targets, including24
executions of the six additions; this is not1440 distinct methods. FSEvents watcher warnings remain
in successful build logs. These durations are not performance comparisons.
No separate fresh JAR or new public-test campaign is claimed.

JVM and JS preserve the224-input manifest. Wasm adds only its generated lock:
raw225 manifest69a3945412babfeca1a71a58fb1536e199e75ea4c6053f2711a1903c7075e205,
lock970a85c50fa072f8543e444af2d28a16de073c3e7ead47b86527d83a557b4dcc.
Raw manifest/diff/output remain archived. Moving only that new output outside
the runtime restores the original224-input manifest; all old inputs are
unchanged, while the complete selected-input inventory changed during Wasm.

```text
635da1e4efb7922a76c4cbb7c1b23caef526ab962ddcd9aac6cc80d89cc69be3  jvm.log
cf882acf58acb4edd1f2b9d796126e20b5338009013384154d8f4c75ea2acfca  audit-jvm.log
6d2014a3e8132692246995fd19b74f5abfe2b372bd29b0cb48189cb6f40a1e0f  js.log
511c152481ac4b62f3d50bad2eef8f02ac8757d266a336febf8b07f5a327581e  audit-js.log
511ff5b60f5376882f51295151c865d1894f47f8a66c2b39cba1f57e49da0385  wasm.log
f45d665be9566a57422255d61847d941ecacd8fd49a5b82bded97a29a1134cec  audit-wasm.log
0bb6e78e678afa0aa60b6681f0a55493b1ea07b4a52220b03149b92b3c73c8ad  mac.log
63af417ae9dbf2e11678386e4f109c712c62963504bb3f48d4fedcde6b579c6e  audit-mac.log
```

Native's after-input snapshot also matches the restored224-input baseline.
Captured Gradle processes10271/11598/14324/15594 were absent after their
respective terminal invocations; each auditor ran separately after its build.

Root adoption preserves all213 base sources and the exact215-source manifest
above. Matching published-head CI38741870826 attempt1 completed successfully
on de5, including actual Linux ARM64 job116459826155. Auditor7c7a42ec...
compiled/executed with a fresh cache and256MiB heap; outer37022 terminated0.
It matched all314 unique native starts/passes against all65 accepted source-bound
Wasm XML suites, with no missing/extra/duplicate/failure/skip anomalies.
The raw ARM log is fbf84ea282e1e1956b7ba0308535269039cd94a1d5f89c44a654032463271e00;
audit output bf64dfe291893100a574499832296f6854dbeb93fbf9783e5ef8abab3292647a.
Final CI evidence capsule e68e83d7557ede9c0cb85eb8e8e258c86496f9cac828a01e4acbe6347533f7e9
and complete job/source bindings are recorded in the
[API integration report](non-null-api-573-verification.md#completed-matching-head-ci).
The earlier308-method d499 run remains a separate failed publication gate.
Official JCK, full public-suite, numerical resource and overall compatibility
claims remain outside this tranche.

Evidence archive: `/private/tmp/krogu-541-normative-evidence-v3.tar`, SHA-256
`2309567764116c15414dab148c960732a80ac4d55636283b9c5bcdb3395a6435`.
It includes the fresh225-file source archive, input manifests, all raw local
logs/XML and the preserved rejected/accepted source preparations. An initial
wrapper archive used an incorrect manifest filename and is retained separately
as incomplete; it is not the accepted evidence archive.
