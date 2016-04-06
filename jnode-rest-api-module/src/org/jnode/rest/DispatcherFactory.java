package org.jnode.rest;

import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jnode.module.JnodeModuleException;
import org.jnode.rest.di.Injector;
import org.jnode.rest.handler.EchomailGetHandler;
import org.jnode.rest.handler.EchomailPostHandler;

import java.lang.reflect.InvocationTargetException;

public class DispatcherFactory {

    public Dispatcher create() throws JnodeModuleException {
        Dispatcher dispatcher = new Dispatcher();

        try {
            dispatcher.register(Injector.inject(new EchomailPostHandler()));
            dispatcher.register(Injector.inject(new EchomailGetHandler()));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new JnodeModuleException(e);
        }

        return dispatcher;
    }

}
