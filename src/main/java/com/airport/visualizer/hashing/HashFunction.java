package com.airport.visualizer.hashing;

import java.util.Locale;

/**
 * The "Simple Hash" used by every hashing lesson: make the key UPPERCASE, add up the
 * character code of every character, then keep the remainder after dividing by the table size.
 * <pre>  hash("Amy", 7) = (A 65 + M 77 + Y 89) % 7 = 231 % 7 = 0</pre>
 */
public final class HashFunction {

    public static final String ARROW = " → ";

    private HashFunction() {
    }

    /** Every number used while hashing one key, so the UI can show the whole calculation. */
    public static final class Calculation {

        private final String key;
        private final String upper;
        private final int[] codes;
        private final int sum;
        private final int size;
        private final int home;

        Calculation(String key, String upper, int[] codes, int sum, int size) {
            this.key = key;
            this.upper = upper;
            this.codes = codes;
            this.sum = sum;
            this.size = size;
            this.home = sum % size;
        }

        public String getKey() {
            return key;
        }

        /** The key after making it uppercase (so "amy" and "AMY" land in the same place). */
        public String getUpper() {
            return upper;
        }

        public int getSum() {
            return sum;
        }

        public int getSize() {
            return size;
        }

        /** The home bucket / slot = sum % size. */
        public int getHome() {
            return home;
        }

        public int[] getCodes() {
            return codes.clone();
        }

        /** "A(65) + M(77) + Y(89)". */
        public String codesText() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < upper.length(); i++) {
                if (i > 0) {
                    sb.append(" + ");
                }
                char c = upper.charAt(i);
                sb.append(c == ' ' ? "space" : String.valueOf(c)).append('(').append(codes[i]).append(')');
            }
            return sb.toString();
        }

        /** "A(65) + M(77) + Y(89) = 231". */
        public String sumText() {
            return codesText() + " = " + sum;
        }

        /** "231 % 7 = 0". */
        public String remainderText() {
            return sum + " % " + size + " = " + home;
        }

        /** "A(65) + M(77) + Y(89) = 231 → 231 % 7 = 0 → home index 0". */
        public String text() {
            return sumText() + ARROW + remainderText() + ARROW + "home index " + home;
        }
    }

    public static Calculation calculate(String key, int size) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (size < 1) {
            throw new IllegalArgumentException("table size must be at least 1");
        }
        String upper = key.toUpperCase(Locale.ROOT);
        int[] codes = new int[upper.length()];
        int sum = 0;
        for (int i = 0; i < upper.length(); i++) {
            codes[i] = upper.charAt(i);
            sum += codes[i];
        }
        return new Calculation(key, upper, codes, sum, size);
    }

    public static int hash(String key, int size) {
        return calculate(key, size).getHome();
    }
}
