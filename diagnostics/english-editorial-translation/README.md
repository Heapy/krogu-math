# English editorial revision

This revision translates Russian prose throughout the current tracked repository, including task snapshots and text members of evidence archives. It also improves the readability of the library comparison report. It does not rerun benchmarks, revise measured results, rewrite Git history or change production code.

The [translation map](translation-map.json) records original and editorial hashes for every changed existing file and archive member. Original bytes remain available at commit `3e5ba2638ea6f170ea0f4f165c7b5b793ff2a3ec`. The [deduplicated prose dictionary](prose-translations.json) contains only English translations, keyed by the SHA256 of the original string. Original Russian strings are not duplicated in this tracked record.

## Evidence and integrity

Original runtime, source, transport and audit hashes continue to describe the original executions. They must not be read as claims that the translated archive was used during measurement. Current archive-member inventories, canonical file indexes and SHA ledgers describe the editorial copies. Historical reviewed-candidate indexes and original-workspace references remain historical; no missing files or new execution evidence is invented.

Numerical samples, inputs, expected values, IDs, acceptance clauses and limitations are preserved. Executables, JAR/KLIB/class/Wasm/native binaries, generated numerical JS/MJS and unrelated archive members remain byte-identical. Git bundles are opaque historical evidence, not prose to rewrite. No numerical suite was repeated for translation.

## Unicode data

Russian, Belarusian and Ukrainian yargs UI/error catalogs in the archived Mocha tooling now use the matching English catalog while retaining keys, plural forms and formatting placeholders. These are genuine message translations. Only the actual Unicode input/output example literals in the explicitly listed camelcase documentation/type examples use Unicode escapes; their decoded values are unchanged. Escapes are not used to conceal untranslated prose.

Unchanged HTML entity-codec tables and Unicode ranges in `he/he.js` and bundled Mocha JS/source maps also contain escaped Cyrillic codepoints. These are machine character mappings, not prose or UI messages. Their exact paths are listed separately in the translation map; runtime bytes remain unchanged.

## Review

The three disjoint prose chunks were cross-reviewed for meaning, not only character counts: 527 reviewed the coordinator's 42 values and the report; 536 reviewed 527's 50 values; the coordinator reviewed 536's 45 values. Six previously translated duplicate values are reused. All 143 deduplicated values retain their original numerical-token multisets and source hashes. Narrow wording corrections preserve the null-input rule, fixed-scale division, two's-complement meaning and coefficient bounds. Final recursive content and current integrity checks are recorded separately after the full diff is frozen.
