package org.cryse.unifystorage.utils;

public class StringUtils {
    private static final char[] HexDigits = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f',};
    public static char hexDigit(int i) { return HexDigits[i]; }

    public static String jq(String value) { return javaQuotedLiteral(value); }

    public static String javaQuotedLiteral(String value)
    {
        StringBuilder b = new StringBuilder(value.length() * 2);
        b.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\t"); break;
                case '\t': b.append("\\r"); break;
                case '\0': b.append("\\000"); break;  // Inserting '\0' isn't safe if there's a digit after
                default:
                    if (c >= 0x20 && c <= 0x7e) {
                        b.append(c);
                    } else {
                        int h1 = (c >> 12) & 0xf;
                        int h2 = (c >> 8) & 0xf;
                        int h3 = (c >> 4) & 0xf;
                        int h4 = c & 0xf;
                        b.append("\\u");
                        b.append(hexDigit(h1));
                        b.append(hexDigit(h2));
                        b.append(hexDigit(h3));
                        b.append(hexDigit(h4));
                    }
                    break;
            }
        }
        b.append('"');
        return b.toString();
    }
}
