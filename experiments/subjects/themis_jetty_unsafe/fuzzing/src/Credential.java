
public class Credential {

    /*
     * YN: we changed String to char[], because some internal things in SPF. When we introduce symbolic variables by
     * adding them to the char values, and later convert it to a String value, then the symbolic values will get lost in
     * an additional back-conversion to char[], because the model for String in SPF does not handle that properly.
     * Therefore, we decided to change that in symbolic analysis. Note: that this is not a big change.
     */
    // public static boolean stringEquals_safe(String s1, String s2) {
    // boolean result = true;
    // int l1 = s1.length();
    // int l2 = s2.length();
    // if (l1 != l2)
    // result = false;
    // int n = (l1 < l2) ? l1 : l2;
    // for (int i = 0; i < n; i++)
    // result &= s1.charAt(i) == s2.charAt(i);
    // return result;
    // }
    public static boolean stringEquals_safe(char[] s1, char[] s2) {
        boolean result = true;
        int l1 = s1.length;
        int l2 = s2.length;
        if (l1 != l2)
            result = false;
        int n = (l1 < l2) ? l1 : l2;
        for (int i = 0; i < n; i++)
            result &= s1[i] == s2[i];
        return result;
    }

    // public static boolean stringEquals_original(String s1, String s2) {
    // if (s1 == s2) {
    // return true;
    // }
    // int n = s1.length();
    // if (n == s2.length()) {
    // char v1[] = s1.toCharArray();
    // char v2[] = s2.toCharArray();
    // int i = 0;
    // while (n-- != 0) {
    // if (v1[i] != v2[i]) {
    // return false;
    // }
    // i++;
    // }
    // return true;
    // }
    // return false;
    // }
    public static boolean stringEquals_original(char[] s1, char[] s2) {
        if (s1 == s2) {
            return true;
        }
        int n = s1.length;
        if (n == s2.length) {
            int i = 0;
            while (n-- != 0) {
                if (s1[i] != s2[i]) {
                    return false;
                }
                i++;
            }
            return true;
        }
        return false;
    }

}
