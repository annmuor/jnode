package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import org.jnode.rest.di.Injector;
import org.jnode.rest.handler.UserLoginHandler;

import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;

public class LoginServlet extends BaseServlet {

    private final Dispatcher dispatcher = new Dispatcher();

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            dispatcher.register(Injector.inject(new UserLoginHandler()));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ServletException(e);
        }

    }

    @Override
    protected Dispatcher getDispatcher() {
        return dispatcher;
    }

}
