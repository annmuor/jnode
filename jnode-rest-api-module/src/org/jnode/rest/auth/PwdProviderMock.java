package org.jnode.rest.auth;

import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;


@Named("mock-pwdProvider")
@Singleton
public class PwdProviderMock implements PwdProvider{

    @Override
    public RestUser isAuth(String token) {
        return new RestUser();
    }
}
