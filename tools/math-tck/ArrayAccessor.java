// SPDX-License-Identifier: Apache-2.0
// Test-only representation adapter, not a numerical implementation.
package mathharness;

public final class ArrayAccessor {
    private ArrayAccessor() {}
    public static int[] mag(io.heapy.krogu.math.BigInteger value) {
        try {
            var type = io.heapy.krogu.math.BigInteger.class;
            var kind = type.getDeclaredField("representation");
            var field = type.getDeclaredField("wideWords");
            kind.setAccessible(true);
            field.setAccessible(true);
            int tag = kind.getInt(value);
            int[] retained = (int[]) field.get(value);
            final int[] words;
            if (tag == 2 && retained != null) {
                // Inspect retained wide storage directly: normalization here would hide the defect.
                words = retained.clone();
            } else if (tag == 1 && retained == null) {
                // Compact storage has no retained magnitude array. Project its primitive value
                // through the implementation's own boundary, never through host java.math.
                var materialize = type.getDeclaredMethod("materializeMagnitude");
                materialize.setAccessible(true);
                words = ((int[]) materialize.invoke(value)).clone();
            } else {
                throw new AssertionError("Inconsistent krogu compact/wide representation tag " + tag);
            }
            // krogu stores least-significant word first; upstream asserts the leading word.
            for (int left = 0, right = words.length - 1; left < right; left++, right--) {
                int temporary = words[left]; words[left] = words[right]; words[right] = temporary;
            }
            return words;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("krogu compact/wide representation unavailable; no host fallback", failure);
        }
    }
}
