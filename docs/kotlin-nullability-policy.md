# Kotlin nullability policy

Nullability expresses the accepted input domain. A reference parameter that immediately rejects null with `NullPointerException` must be non-null in the public Kotlin API. Declaring `T?` solely to evaluate `value ?: throw NullPointerException()` does not satisfy this policy.

Kotlin migration may require matching nullability edits in callers and subclass overrides. The API does not need to preserve both nullable and non-null override spellings accepted by JDK platform types. Java source call shapes belong to the TCK and its test-only adapters.

The audit reviewed conditional validation and virtual forwarding separately: their current staging does not grant permission to accept null. Its final target is 48 non-null input slots, retaining the three nullable `equals(Any?)` inputs. This includes constructor parameters that are currently nullable. The input rule does not automatically change return-type nullability. Valid non-null inputs retain the numerical contract; invalid-input checks and frozen JDK observations must be preserved or explicitly classified when tests move to adapters. VM messages and unspecified internal callback order remain observations under the conformance audit.

This global policy was explicitly confirmed by the user on 2026-10-15. Constructor proposals570/571 and nullable-context pow proposal572 were closed without adoption. Task549 supplies the inventory; task573 implements the input migration. This document does not claim that every current declaration already conforms.
