package com.tzg.framework.support.config.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonMappingExceptionResolver extends SimpleMappingExceptionResolver {

    private final static Logger logger = LoggerFactory.getLogger( JsonMappingExceptionResolver.class );

    @Override
    protected ModelAndView doResolveException( HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex ) {

        logger.warn( "Exception was caught. The reason is: {}", ex.getLocalizedMessage() );

        ex.printStackTrace();

        HandlerMethod method = ( HandlerMethod ) handler;
        ResponseBody  body   = method.getMethodAnnotation( ResponseBody.class );
        if ( body == null ) {
            return super.doResolveException( request, response, handler, ex );
        }

        ModelAndView mav = new ModelAndView();
        //        response.setStatus( HttpStatus.INTERNAL_SERVER_ERROR.value() );
        response.setContentType( MediaType.APPLICATION_JSON_VALUE );
        response.setCharacterEncoding( "UTF-8" );
        response.setHeader( "Cache-Control", "no-cache, must-revalidate" );

        try {
            String errJson = "{\"status\":\"error\", \"data\": \"" + ex.getLocalizedMessage() + "\"}";
            response.getWriter().write( errJson );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return mav;

    }

}
