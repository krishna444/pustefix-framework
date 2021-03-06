package org.pustefixframework.http.internal;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class PustefixServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        PustefixInit.initEnvironmentProperties(ctx);
        ctx.addListener(new SessionStatusListenerAdapter());
    }
    
}
