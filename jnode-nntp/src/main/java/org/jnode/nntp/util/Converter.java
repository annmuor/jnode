package org.jnode.nntp.util;

import org.apache.commons.lang.StringUtils;

public class Converter {

    /**
     * User example: jim.ayson@p300.f2.n750.z6.fidonet.org
     *
     * @param email email.
     * @return ftn address like 6:450/2.300
     */
    public static String convertEmailToFtn(String email) {
        StringBuilder ftnAddress = new StringBuilder();

        String[] ftnParts = StringUtils.split(StringUtils.substring(email, StringUtils.indexOf(email, "@") + 1), ".");
        // -2 because skip fidonet.org
        // -3 because start with 0
        for (int index = ftnParts.length - 3; index >= 0; index--) {
            String part = ftnParts[index];
            if (StringUtils.contains(part, "z")) {
                // ignore
            }
            if (StringUtils.contains(part, "n")) {
                ftnAddress.append(":");
            }
            if (StringUtils.contains(part, "f")) {
                ftnAddress.append("/");
            }
            if (StringUtils.contains(part, "p")) {
                ftnAddress.append(".");
            }

            ftnAddress.append(StringUtils.substring(ftnParts[index], 1));
        }

        return ftnAddress.toString();

    }
}
