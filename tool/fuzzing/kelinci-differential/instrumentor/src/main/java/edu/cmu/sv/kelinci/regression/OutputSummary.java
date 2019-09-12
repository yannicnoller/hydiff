package edu.cmu.sv.kelinci.regression;

import java.util.List;

/**
 * Stores and manages the output observed during execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 * 
 */
public class OutputSummary {

    private Object o1;
    private Object o2;

    public OutputSummary(Object o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public int getEncodedValueVersion1() {
        if (o1 == null) {
            return 0;
        } else {
            if (o1 instanceof Throwable) {
                return ((Throwable) o1).toString().hashCode();
            } else {
                return o1.hashCode();
            }
        }
    }

    public int getEncodedValueVersion2() {
        if (o2 == null) {
            return 0;
        } else {
            if (o2 instanceof Throwable) {
                return ((Throwable) o2).toString().hashCode();
            } else {
                return o2.hashCode();
            }
        }
    }

    public boolean isDifferent() {

        /* Easiest comparison. */
        if (o1 == o2 || o1.equals(o2)) {
            return false;
        }
        /* BELOW: objects are not the same */

        /* Check for different null values. */
        if (o1 == null && o2 != null) {
            return true;
        } else if (o1 != null && o2 == null) {
            return true;
        }
        /* BELOW: both are != null */

        /* Throwables: compare the String values of the messages. */
        if (o1 instanceof Throwable && o2 instanceof Throwable) {
            String o1str = ((Throwable) o1).toString();
            String o2str = ((Throwable) o2).toString();

            // remove for "v1_", "v2_", .. prefixes in exception class names
            if (o1str.startsWith("v") && o1str.charAt(2) == '_' && o2str.startsWith("v") && o2str.charAt(2) == '_') {
                o1str = o1str.substring(3);
                o2str = o2str.substring(3);
            }

            return !o1str.equals(o2str);
        }

        /* List: compare sizes and object in lists */
        if (o1 instanceof List && o2 instanceof List) {
            List<?> o1L = (List<?>) o1;
            List<?> o2L = (List<?>) o2;

            if (o1L.size() != o2L.size()) {
                return true;
            }
            /* BELOW: same size */

            if (o1L.isEmpty() && o2L.isEmpty()) {
                return false;
            }
            /* BELOW: both non empty */

            if (!o1L.get(0).getClass().equals(o2L.get(0).getClass())) {
                return true;
            }
            /* BELOW: both same types */

            for (int i = 0; i < o1L.size(); i++) {
                Object o1LO = o1L.get(i);
                Object o2LO = o2L.get(i);

                if (o1LO == o2LO || o1LO.equals(o2LO)) {
                    continue;
                }

                /* Check for different null values. */
                if (o1 == null && o2 != null) {
                    return true;
                } else if (o1 != null && o2 == null) {
                    return true;
                }
                /* BELOW: both are != null */

                /* Check String representations. */
                if (o1LO.toString().equals(o2LO.toString())) {
                    continue;
                }

                return false;
            }
            /* BELOW: both lists are identical */

            return false;
        }

        /* For all other cases just compare the objects. */
        return !(o1.equals(o2));

    }

    public boolean newCrashBehaviorObserved() {
        return !(o1 instanceof Throwable) && (o2 instanceof Throwable);
    }

}