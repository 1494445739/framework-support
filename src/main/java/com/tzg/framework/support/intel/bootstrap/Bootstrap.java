package com.tzg.framework.support.intel.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Bootstrap extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger( Bootstrap.class );

    private final static String URL = "http://localhost:8080";

    public void init( ServletConfig config ) throws ServletException {
        super.init( config );
        bootstrap();
    }

    private void bootstrap() {

        try {

            String os = System.getProperty( "os.name" );

            if ( os.startsWith( "Mac OS" ) ) {

                Class< ? > clz    = Class.forName( "com.apple.eio.FileManager" );
                Method     method = clz.getDeclaredMethod( "openURL", String.class );
                method.invoke( null, URL );

            } else if ( os.startsWith( "Windows" ) ) {

                Runtime.getRuntime().exec( "rundll32 url.dll, FileProtocolHandler " + URL );

            } else {

                String[] browsers = {
                        "firefox",
                        "opera",
                        "konqueror",
                        "epiphany",
                        "mozilla",
                        "netscape"
                };
                String browser = null;

                for ( int count = 0; count < browsers.length && browser == null; count++ ) {
                    if ( Runtime.getRuntime().exec( new String[]{
                            "which",
                            browsers[ count ]
                    } ).waitFor() == 0 ) {
                        browser = browsers[ count ];
                    }
                }

                if ( browser == null ) {
                    logger.warn( "======== Could not find web browser ========" );
                } else {
                    Runtime.getRuntime().exec( new String[]{
                            browser,
                            URL
                    } );
                }

            }

        } catch ( ClassNotFoundException e ) {
            logger.error( e.getMessage(), e );
        } catch ( NoSuchMethodException e ) {
            logger.error( e.getMessage(), e );
        } catch ( InvocationTargetException e ) {
            logger.error( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            logger.error( e.getMessage(), e );
        } catch ( IOException e ) {
            logger.error( e.getMessage(), e );
        } catch ( InterruptedException e ) {
            logger.error( e.getMessage(), e );
        } catch ( Exception e ) {
            logger.error( e.getMessage(), e );
        }

    }

}
