package com.tzg.framework.support.config.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import ch.qos.logback.ext.spring.web.WebLogbackConfigurer;
import com.tzg.tool.support.properties.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

/**
 * 重置logback.xml配置文件的路径。从默认路径resources目录下移至resources/logback下
 *
 * @author 曾林 2016/11/28
 */
@WebListener
public class LogbackListener implements ServletContextListener {

    private final static Logger logger = LoggerFactory.getLogger( LogbackListener.class );

    /** logback 配置文件的新路径 */
    private String LOG_CONFIG_LOCATION = "/logback/logback.xml";

    /** logback 第三方组件日志级别环境变量 */
    private String LOG_ROOT_LEVEL = "log.root.level";

    public void contextInitialized( ServletContextEvent evt ) {

        try {

            String logLevel = PropUtil.get( LOG_ROOT_LEVEL );
            System.setProperty( "log.root.level", StringUtils.isEmpty( logLevel ) ? "DEBUG" : logLevel );

            LoggerContext loggerContext = ( LoggerContext ) StaticLoggerBinder.getSingleton().getLoggerFactory();
            loggerContext.reset();

            new ContextInitializer( loggerContext ).configureByResource( getClass().getResource( LOG_CONFIG_LOCATION ) );

            StatusPrinter.print( loggerContext ); // 控制台打印logback.xml内部配置信息，比如生成多少个日志文件等

            logger.debug( "loaded slf4j config file from {}", evt.getServletContext().getRealPath( LOG_CONFIG_LOCATION ) );

        } catch ( JoranException e ) {
            logger.error( "loaded slf4j config file error, error message is {}", e.getLocalizedMessage() );
        } catch ( IOException e ) {
            logger.error( "read default.properties file error, error message is {}", e.getLocalizedMessage() );
        }
    }

    public void contextDestroyed( ServletContextEvent evt ) {
        WebLogbackConfigurer.shutdownLogging( evt.getServletContext() );
        logger.debug( "shutdown slf4j logging" );
    }

}
