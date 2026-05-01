package github.titandea.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtils {
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
