package org.jnode.rest.auth;

import org.jnode.rest.di.Named;
import org.jnode.rest.di.Singleton;

@Named("adminResolver")
@Singleton
public class AdminResolver {

    public String getAdminAddress() {
        return adminAddress;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    private String adminAddress;

    public boolean isAdmin(String ftnAddress){
        return ftnAddress != null && ftnAddress.equals(adminAddress);
    }
}
