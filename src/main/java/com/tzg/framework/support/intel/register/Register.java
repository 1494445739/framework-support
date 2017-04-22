package com.tzg.framework.support.intel.register;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 模块智能注册.
 *
 * @author 曾林 2016/12/15.
 */
public class Register {

    private final static Logger logger = LoggerFactory.getLogger( Register.class );

    private DataSource dataSource;

    public void setDataSource( DataSource dataSource ) {
        this.dataSource = dataSource;
    }

    public void init() {

        try {

            String[] impModules = getImpModules();
            String[] allModules = getAllModules();

            impModules( impModules, allModules );

        } catch ( SQLException e ) {
            logger.error( "retrieve database connection error, error is {}", e );
        } catch ( IOException e ) {
            logger.error( "read mysql script error, error is {}", e );
        }

    }

    /**
     * 将还未注册的module进行注册。同时插入到GLOBAL_MODULE表中。
     *
     * @param impModules 已经注册的module
     * @param allModules 所有module
     * @throws IOException  读取init.sql失败报的异常
     * @throws SQLException 从dataSource中获取连接失败报的异常
     */
    private void impModules( String[] impModules, String[] allModules ) throws SQLException, IOException {

        ScriptRunner runner = new ScriptRunner( dataSource.getConnection() );

        for ( String module : allModules ) {

            boolean exist = false;

            for ( String impModule : impModules ) {

                if ( module.trim().equalsIgnoreCase( impModule.trim() ) ) {
                    exist = true;
                    break;
                }

            }

            String sqlPath = "mysql/module/init.sql".replace( "module", module );

            if ( !exist ) {

                logger.info( "start init module {}, the module path is {}", module, sqlPath );

                runner.setStopOnError( Boolean.TRUE );
                runner.setAutoCommit( Boolean.FALSE );
                runner.setLogWriter( null );

                Resources.setCharset( Charset.forName( "utf-8" ) );
                runner.runScript( Resources.getResourceAsReader( sqlPath ) );

                logger.info( "init module {} over...", module );

                logger.info( "insert {} into GLOBAL_MODULE", module );

                Connection conn = dataSource.getConnection();
                Statement  stmt = conn.createStatement();
                stmt.executeUpdate( "insert into GLOBAL_MODULE( name ) values( 'module' )".replace( "module", module ) );
                stmt.close();
                conn.close();

                logger.info( "insert {} into GLOBAL_MODULE over" );

            } else {

                logger.info( "module {} imported, the path is {} ", module, sqlPath );

            }

        }

        runner.closeConnection();

    }

    /**
     * 扫描中枢表CFG_VARIABLES，获取已注册的组件名称列表。如果表不存在，则首先创建中枢表并返回空模块列表。
     * 中枢表只有两个字段ID、NAME。
     * <p>
     * ID : 非业务主键
     * NAME: 组件或者服务名字
     * </p>
     */
    private String[] getImpModules() throws SQLException, IOException {

        List< String > modules = new ArrayList< String >(); // GLOBAL_MODULE的name字段

        ScriptRunner runner = new ScriptRunner( dataSource.getConnection() );

        runner.setStopOnError( Boolean.TRUE );
        runner.setAutoCommit( Boolean.FALSE );
        runner.setLogWriter( null );

        Connection conn = dataSource.getConnection();
        Statement  stmt = conn.createStatement();

        ResultSet rs = conn.getMetaData().getTables( null, null, "GLOBAL_MODULE", null );

        if ( !rs.next() ) { // GLOBAL_MODULE表不存在，就在数据库中建表

            logger.info( "Table GLOBAL_MODULE doesn't exist. Start create ..." );

            Resources.setCharset( Charset.forName( "utf-8" ) );
            runner.runScript( Resources.getResourceAsReader( "mysql/init.sql" ) );

            logger.info( "Table GLOBAL_MODULE create successfully..." );

        } else { // GLOBAL_MODULE表存在，就读出name字段的所有内容

            logger.info( "Table GLOBAL_MODULE exist. exit scan..." );

            rs = stmt.executeQuery( "SELECT name FROM GLOBAL_MODULE" );

            while ( rs.next() ) {
                modules.add( rs.getString( "name" ) );
            }

        }

        runner.closeConnection();

        return modules.toArray( new String[ modules.size() ] );

    }

    /**
     * 获取运行时所有组件字符串数组。即具有mysql/module/init.sql目录结构并被项目pom.xml依赖的组件。
     */
    private String[] getAllModules() throws IOException {

        PathMatchingResourcePatternResolver    resolver  = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources = resolver.getResources( "classpath*:mysql/**/init.sql" );
        List< String >                         modules   = new ArrayList<>();

        for ( org.springframework.core.io.Resource resource : resources ) {
            String url = resource.getURL().getPath();
            if ( url.contains( "framework-support" ) ) continue;
            int    endPos       = url.lastIndexOf( "/" );
            String urlNoInitSql = url.substring( 0, endPos );
            int    startPos     = urlNoInitSql.lastIndexOf( "/" ) + 1;
            String module       = url.substring( startPos, endPos );
            modules.add( module );
        }

        Set< String > set = new HashSet<>( modules );
        modules.clear();
        modules.addAll( set );

        logger.info( "tzg modules are: {}", modules );

        return modules.toArray( new String[ modules.size() ] );

    }

}
