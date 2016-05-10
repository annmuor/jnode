package org.jnode.rest.route;

import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import org.jnode.rest.di.Injector;
import org.jnode.rest.handler.secure.EchomailGetHandler;
import org.jnode.rest.handler.secure.EchomailPostHandler;
import org.jnode.rest.handler.secure.LinkCreateHandler;
import org.jnode.rest.handler.secure.LinkListHandler;

import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;

public class SecureServlet extends BaseServlet {

    private final Dispatcher dispatcher = new Dispatcher();

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            dispatcher.register(Injector.inject(new EchomailPostHandler()));
            dispatcher.register(Injector.inject(new EchomailGetHandler()));
            dispatcher.register(Injector.inject(new LinkListHandler()));
            dispatcher.register(Injector.inject(new LinkCreateHandler()));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ServletException(e);
        }

    }

    @Override
    protected Dispatcher getDispatcher() {
        return dispatcher;
    }

}
