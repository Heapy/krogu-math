import io.heapy.krogu.math.*;
import java.util.concurrent.Callable;
public final class Accumulator535Probe {
    public static Callable<Object> operation(String name, boolean accumulating) {
        final boolean dot = name.startsWith("dot-");
        final boolean fma = name.startsWith("fma-");
        final boolean wide = name.contains("wide");
        final boolean mixed = name.contains("mixed");
        final boolean step = name.contains("step");
        final boolean reuse = name.contains("reuse");
        final DecimalRounding policy = step ? DecimalRounding.EACH_STEP : DecimalRounding.FINAL_RESULT;
        final MathContext context = (dot || fma) ? new MathContext(12, RoundingMode.HALF_EVEN) : MathContext.UNLIMITED;
        final int length = fma ? 2 : (wide ? 16 : 256);
        final BigDecimal[] left = new BigDecimal[length];
        final BigDecimal[] right = new BigDecimal[length];
        for (int i = 0; i < length; i++) {
            final String digits = wide ? "123456789".repeat(28) + (i + 11) : Integer.toString(i % 7 + 1);
            final int scale = mixed ? i % 13 : 2;
            left[i] = new BigDecimal(new BigInteger((i % 3 == 0 ? "-" : "") + digits), scale);
            right[i] = new BigDecimal(new BigInteger(wide ? "987654321".repeat(20) + (i + 13) : "123"), 2);
        }
        final DecimalAccumulator reusable = new DecimalAccumulator(context, policy, null, null);
        if (accumulating) return () -> {
            if (fma) return DecimalAccumulatorKt.fma(left[0], right[0], left[1], context, policy, null);
            final DecimalAccumulator acc = reuse ? reusable : new DecimalAccumulator(context, policy, null, null);
            if (reuse) acc.clear();
            if (dot) for (int i = 0; i < length; i++) acc.addProduct(left[i], right[i]);
            else for (BigDecimal term : left) acc.add(term);
            return acc.snapshot();
        };
        return () -> {
            if (fma) return step ? left[0].multiply(right[0], context).add(left[1], context)
                : left[0].multiply(right[0]).add(left[1]).round(context);
            BigDecimal result;
            if (dot) {
                result = step ? left[0].multiply(right[0], context) : left[0].multiply(right[0]);
                for (int i = 1; i < length; i++) {
                    final BigDecimal product = step ? left[i].multiply(right[i], context) : left[i].multiply(right[i]);
                    result = step ? result.add(product, context) : result.add(product);
                }
            } else {
                result = step ? left[0].round(context) : left[0];
                for (int i = 1; i < length; i++) result = step ? result.add(left[i], context) : result.add(left[i]);
            }
            return step ? result : result.round(context);
        };
    }
}