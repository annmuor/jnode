package org.jnode.rest.auth;

import jnode.ftn.FtnTools;
import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;


@Named("mock-pwdProvider")
@Singleton
public class PwdProviderMock implements PwdProvider{
    @Override
    public char[] getPwd(String submittedUsername) {
        return FtnTools.md5("111111").toCharArray();
    }
}
