# First focused JVM invocation

The first invocation on the task533 candidate completed with exit 1: six
methods, five passes and one failure. The four common methods and signed compact
division oracle passed. The remaining new JVM test stopped on its first zero
rounding case: public JDK25 returned `0@-19`, while krogu returned `0@0`.

Direct round/plus zero canonicalization is an established task567 contract in
`ZeroContextScale567Test` and the existing BigDecimal.plus implementation; it is
unrelated to this optimization. The new test had incorrectly assumed complete
host cohort equality for that documented boundary. Only the expected zero
cohort is normalized in the corrected JVM test. Nonzero value/scale assertions,
all modes and other scale/strip comparisons remain intact. No production fix
was made to make this test pass. The complete original log and both XML files
are retained unchanged here; they do not certify the corrected test.

Command: pinned Liberica25.0.4 `./gradlew --no-daemon jvmTest --tests
'*DecimalScaling533*' --console=plain`, owned exec session48210, terminal exit1.
The shared heavy slot was released after completion; a new run is pending.
