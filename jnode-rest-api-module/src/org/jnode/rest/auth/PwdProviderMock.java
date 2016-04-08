package org.jnode.rest.auth;

import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;


@Named("mock-pwdProvider")
@Singleton
public class PwdProviderMock implements PwdProvider{

    @Override
    public boolean isAuth(String token) {
        return true;
    }
}
