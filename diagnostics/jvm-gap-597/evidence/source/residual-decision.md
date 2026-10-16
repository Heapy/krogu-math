# Residual small-context division

Frozen after-first-fix artifact47ec988d/probed303 was profiled on claws using one unchanged10s profile and one10s non-JFR control, not a repeat timing campaign. Independent audit: independent-residual-profile-audit.json.

955/963 CPU samples include own frames; quotientExponent591/963, BigInteger.toString555/963, normalizedComparison306/963 are overlapping inclusive counts. Exact thread allocation62124982488/22806528 =2724B/op. JFR allocation weights estimate1.064x exact. Single pair wall ratio0.959 is variance, not negative instrumentation cost.

The original small fixture uses MC34 and scaled numerator shift32/33. Existing529 Long-scaled quotient rejects shift>18 and finite result coefficients exceedLong. It does not cover this specific metadata/String normalization cost. Implement here: exact immutable stored-coefficient gate, positive compact magnitudes excludingMIN, digit counts and quotient/remainder prefix normalized comparison. Retain complete legacy method sequence for subtypes, wide andMIN. Full supported fallback remains.

Earlier900 samples and first-fix regression remain separately bound to earlier candidate. Final new arithmetic source requires fresh focused and full correctness plus same-host final paired proof. No global JDK parity or full attribution claim.
