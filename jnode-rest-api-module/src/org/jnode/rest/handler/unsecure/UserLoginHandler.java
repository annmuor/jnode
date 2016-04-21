package org.jnode.rest.handler.unsecure;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import jnode.dto.Link;
import org.jnode.rest.auth.AdminResolver;
import org.jnode.rest.core.CryptoUtils;
import org.jnode.rest.core.RPCError;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.Inject;
import org.jnode.rest.di.Named;
import org.jnode.rest.fido.LinkProxy;
import org.jnode.rest.fido.RestUserProxy;
import org.jnode.rest.handler.AbstractHandler;

public class UserLoginHandler extends AbstractHandler {

    @Inject
    @Named("restUserProxy")
    private RestUserProxy restUserProxy;

    @Inject
    @Named("linkProxy")
    private LinkProxy linkProxy;

    @Inject
    @Named("adminResolver")
    private AdminResolver adminResolver;

    @Override
    protected JSONRPC2Response createJsonrpc2Response(Object reqID, NamedParamsRetriever np) throws JSONRPC2Error {

        final String login = np.getString("login", false);

        if (login == null || login.isEmpty()){
            return new JSONRPC2Response(RPCError.EMPTY_LOGIN, reqID);
        }

        Link link = linkProxy.getByFtnAddress(login);

        if (link == null){
            return new JSONRPC2Response(RPCError.BAD_CREDENTIALS, reqID);
        }

        if (link.getPaketPassword() == null || !link.getPaketPassword().equals(np.getString("password", false))){
            return new JSONRPC2Response(RPCError.BAD_CREDENTIALS, reqID);
        }


        RestUser restUser = restUserProxy.findByLinkId(link.getId());

        final String pwd = CryptoUtils.randomToken();

        if (restUser != null){
            restUser.setToken(CryptoUtils.makeToken(pwd));
            restUserProxy.update(restUser);
        } else {
            restUser = new RestUser();
            restUser.setLink(link);
            restUser.setToken(CryptoUtils.makeToken(pwd));
            restUser.setType(adminResolver.isAdmin(login) ? RestUser.Type.ADMIN : RestUser.Type.USER);
            restUserProxy.save(restUser);
        }

        return new JSONRPC2Response(pwd, reqID);

    }

    @Override
    protected RestUser.Type[] secured() {
        return null;
    }

    @Override
    public String[] handledRequests() {
        return new String[]{"user.login"};
    }

    public void setRestUserProxy(RestUserProxy restUserProxy) {
        this.restUserProxy = restUserProxy;
    }

    public void setLinkProxy(LinkProxy linkProxy) {
        this.linkProxy = linkProxy;
    }

    public void setAdminResolver(AdminResolver adminResolver) {
        this.adminResolver = adminResolver;
    }

}
