package com.tzg.framework.support.config.logback;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter( filterName = "logbackMDCFilter", urlPatterns = "/*" )
public class LogbackMDCFilter implements Filter {

    private final String CLIENT_ID = "clientID";

    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {}

    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException {

        MDC.put( CLIENT_ID, ( ( HttpServletRequest ) request ).getSession().getId() );

        try {
            chain.doFilter( request, response );
        } finally {
            MDC.remove( CLIENT_ID );
        }

    }

    @Override
    public void destroy() {}

}
