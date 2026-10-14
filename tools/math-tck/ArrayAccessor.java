// SPDX-License-Identifier: Apache-2.0
// Test-only representation adapter, not a numerical implementation.
package mathharness;

public final class ArrayAccessor {
    private ArrayAccessor() {}
    public static int[] mag(io.heapy.krogu.math.BigInteger value) {
        try {
            var field = io.heapy.krogu.math.BigInteger.class.getDeclaredField("words");
            field.setAccessible(true);
            int[] words = ((int[]) field.get(value)).clone();
            // krogu stores least-significant word first; upstream asserts the leading word.
            for (int left = 0, right = words.length - 1; left < right; left++, right--) {
                int temporary = words[left]; words[left] = words[right]; words[right] = temporary;
            }
            return words;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("krogu words representation unavailable; no host fallback", failure);
        }
    }
}
