package methods;

import java.security.SecureRandom;

public final class Seq {

    public static long next(){
        return new SecureRandom().nextLong();
    }
}
