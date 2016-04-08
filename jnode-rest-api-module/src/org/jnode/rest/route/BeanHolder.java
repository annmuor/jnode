package org.jnode.rest.route;

import org.jnode.rest.auth.PwdProvider;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;

public class BeanHolder {
    @Inject
    @Named("pwdProvider")
    private PwdProvider pwdProvider;

    public PwdProvider getPwdProvider() {
        return pwdProvider;
    }

    public void setPwdProvider(PwdProvider pwdProvider) {
        this.pwdProvider = pwdProvider;
    }
}
