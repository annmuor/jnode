package jnode.jscript;

import jnode.logger.Logger;

public class CoreHelper extends IJscriptHelper{

    private final Logger logger = Logger.getLogger(getClass());

    public Class<?> getClassByName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            logger.l4(String.format("get bad class %s from jscript", name));
            return null;
        }
    }


    @Override
    public Version getVersion() {
        return new Version() {
            @Override
            public int getMajor() {
                return 1;
            }

            @Override
            public int getMinor() {
                return 0;
            }
        };
    }
}
