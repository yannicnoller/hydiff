import gov.nasa.jpf.symbc.Debug;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class Login {

    /* YN: adjusted so that we can set the real_password from extern. */

    public static boolean login_unsafe(byte[] real_password, byte[] guess, String username) {
        // byte[] real_password = retrieve(username);
        if (real_password == null) {
            return false;
        }
        
        
//        if (execute(NEW)) {
//            int tmp = 0;
////            tmp++;
//            if (tmp > 0) {
//                tmp++;
//            }
//            for (int i=0; i<10; i++) {
//                tmp++;
//            }
//            tmp++;
//        }

        for (int i = 0; i < guess.length; i++) {
            if (i < real_password.length) {
                if (guess[i] != real_password[i]) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public static boolean login_safe(byte[] real_password, byte[] guess, String username) {
        boolean unused;
        boolean matches = true;

        // byte[] real_password = retrieve(username);

        if (real_password == null) {
            return false;
        }

        for (int i = 0; i < guess.length; i++) {
            if (i < real_password.length) {
                if (guess[i] != real_password[i]) {
                    matches = false;
                } else {
                    unused = true;
                }
            } else {
                unused = false;
                unused = true;
                matches = false;
            }
        }

        return matches;
    }

    public static byte[] retrieve(String username) {
        byte[] pw = { 0xa, 0x3, 0xf, 0x1 };
        return pw;
    }
}
