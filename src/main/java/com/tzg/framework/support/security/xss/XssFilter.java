package com.tzg.framework.support.security.xss;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * XSS过滤器。防止XSS攻击。web.xml中配置：
 * <p>
 * <filter>
 * <filter-name>xssFilter</filter-name>
 * <filter-class>com.tzg.framework.support.security.xss.XSSFilter</filter-class>
 * </filter>
 * <p>
 * <filter-mapping>
 * <filter-name>xssFilter</filter-name>
 * <url-pattern>/*</url-pattern>
 * </filter-mapping>
 *
 * @author 曾林 2016/12/15.
 * @link http://192.168.1.177:3000/%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97/%E5%AE%89%E5%85%A8%E8%A7%84%E8%8C%83-%E5%BA%94%E7%94%A8%E7%AF%87
 */
//@WebFilter( filterName = "xssFilter", urlPatterns = "/*" )
public class XssFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger( XssFilter.class );

    public void init( FilterConfig filterConfig ) throws ServletException {}

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

        logger.debug( "XSS filter intercept, request uri is : {} ", ( ( HttpServletRequest ) ( request ) ).getServletPath() );

        chain.doFilter( new XssHttpServletRequestWrapper( ( HttpServletRequest ) request ), response );
    }

    public void destroy() {}
}

class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger( XssHttpServletRequestWrapper.class );

    XssHttpServletRequestWrapper( HttpServletRequest request ) {
        super( request );
    }

    @Override
    public String getHeader( String name ) {
        return StringEscapeUtils.escapeHtml4( super.getHeader( name ) );
    }

    @Override
    public String getQueryString() {
        return StringEscapeUtils.escapeHtml4( super.getQueryString() );
    }

    @Override
    public String getParameter( String name ) {
        return StringEscapeUtils.escapeHtml4( super.getParameter( name ) );
    }

    /**
     * 转义查询字符串的name，此name具有多个value
     */
    @Override
    public String[] getParameterValues( String name ) {

        String[] values = null;

        try {
            values = super.getParameterValues( name );
            int length = values.length;

            if ( length != 0 ) {

                String[] escapeVal = new String[ length ];

                for ( int i = 0; i < length; i++ ) {
                    escapeVal[ i ] = StringEscapeUtils.escapeHtml4( values[ i ] );
                }

                values = escapeVal;
            }
        } catch ( NullPointerException e ) {
            logger.error( "parameter {} is null", name );
        }

        return values;

    }

    /**
     * 转义查询字符串的参数map。将所有name对应的value都进行转义。
     */
    @Override
    public Map< String, String[] > getParameterMap() {

        Map< String, String[] > map = new HashMap< String, String[] >();

        for ( String key : super.getParameterMap().keySet() ) {
            map.put( key, this.getParameterValues( key ) );
        }

        return map;

    }

}
