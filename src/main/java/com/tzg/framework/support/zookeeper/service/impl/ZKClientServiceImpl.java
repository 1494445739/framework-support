package com.tzg.framework.support.zookeeper.service.impl;

import com.tzg.framework.support.zookeeper.service.api.ZKClientService;
import com.tzg.framework.support.zookeeper.support.ZKUtils;
import com.tzg.framework.support.zookeeper.support.ZkGlobalConst;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZKClientServiceImpl implements ZKClientService {

    private ZooKeeper zookeeper;

    private String zkConnectString;

    private String rootPath; //根路径

    private int zkSessionTimeout = 60000;   //超时时间，单位毫秒

    private List< ACL > acls; //访问列表

    private String userName;   //节点权限用户名

    private String password;   //节点权限密码

    private String authScheme = "digest"; //权限模式

    protected static Logger logger = LoggerFactory.getLogger( ZKClientServiceImpl.class );

    public boolean connected() throws Exception {
        return zookeeper != null && zookeeper.getState() == ZooKeeper.States.CONNECTED;
    }

    public synchronized void reConnection() throws Exception {
        if ( this.zookeeper != null ) {
            this.zookeeper.close();
            this.zookeeper = null;
        }
        this.connect();
    }

    public void connect() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch( 1 );
        createZookeeper( connectionLatch );
        connectionLatch.await( 10, TimeUnit.SECONDS );
    }

    private void createZookeeper( final CountDownLatch latch ) throws Exception {
        zookeeper = new ZooKeeper( zkConnectString, zkSessionTimeout, new Watcher() {
            public void process( WatchedEvent event ) {
                sessionEvent( latch, event );
            }
        } );
        if ( !getAcls().isEmpty() ) {
            getAcls().clear();
        }
        if ( StringUtils.isNotBlank( userName ) ) {
            String auth = userName + ":" + password;
            zookeeper.addAuthInfo( authScheme, auth.getBytes() );
            acls.add( new ACL( ZooDefs.Perms.ALL, new Id( authScheme, DigestAuthenticationProvider.generateDigest( auth ) ) ) );
        }
        acls.add( new ACL( ZooDefs.Perms.READ, ZooDefs.Ids.ANYONE_ID_UNSAFE ) );
    }

    public List< ACL > getAcls() {
        if ( null == acls ) {
            acls = new ArrayList<>();
        }
        return acls;
    }

    public ZooKeeper getZooKeeper() throws Exception {
        if ( !connected() ) {
            reConnection();
        }
        return zookeeper;
    }

    @Override
    public List< String > getChildren( String path ) throws Exception {

        List< String > resList = null;
        try {
            resList = getZooKeeper().getChildren( path, false );
        } catch ( Exception e ) {
            logger.error( "查询ZK路径 {} 下的子节点异常:", path, e );
        }
        return resList;

    }

    @Override
    public String[] getTree( String path ) throws Exception {
        return ZKUtils.getTree( zookeeper, getPath( path ) );
    }

    @Override
    public String getData( String path ) throws Exception {
        return getData( path, null );
    }

    @Override
    public String getData( String path, Stat stat ) throws Exception {
        byte[] res = null;
        try {
            path = getPath( path );
            if ( !this.exists( path, false ) ) {
                return null;
            }
            res = zookeeper.getData( path, false, stat );
        } catch ( Exception e ) {
            logger.error( "获取节点:{}数据异常:", path, e );
        }
        return res == null ? null : new String( res, Charsets.UTF_8 );
    }

    @Override
    public Stat setData( String path, byte[] data ) throws Exception {
        return setData( path, data, -1 );
    }

    @Override
    public Stat setData( String path, byte[] data, int version ) throws Exception {
        Stat stat = null;
        try {
            stat = zookeeper.setData( getPath( path ), data, version );
        } catch ( Exception e ) {
            logger.error( "{}:", e.getLocalizedMessage(), e );
        }
        return stat;
    }

    @Override
    public boolean createPersistent( String path, byte[] data ) throws Exception {
        return create( path, CreateMode.PERSISTENT, data );
    }

    @Override
    public boolean createEphemeral( String path, byte[] data ) throws Exception {
        return create( path, CreateMode.EPHEMERAL, data );
    }

    @Override
    public boolean create( String path, CreateMode createMode, byte[] data ) throws Exception {
        return create( path, createMode, data, false, acls );
    }

    @Override
    public boolean create( String path, CreateMode createMode, byte[] data, boolean enforce, List< ACL > acls )
            throws Exception {
        try {
            path = getPath( path );
            if ( exists( path ) ) {
                if ( !enforce ) {
                    logger.warn( "节点路径:{}已经存在,不能创建", path );
                    return false;
                }
                this.deletNode( path );
            }
            acls = ( null == acls ? getAcls() : acls );
            String createdPath = ZKUtils.create( zookeeper, path, data, createMode, acls );
            return StringUtils.equals( path, createdPath );
        } catch ( Exception e ) {
            logger.error( "创建zk节点:{},节点类型:{},发生{}:", path, createMode, e.getClass(), e );
        }
        return false;
    }

    @Override
    public void deleteNode( String path, int version ) throws Exception {
        try {
            zookeeper.delete( getPath( path ), version );
        } catch ( Exception e ) {
            logger.error( "删除节点路径{},版本:{}，发生{}异常:", path, version, e.getClass(), e );
        }
    }

    @Override
    public void deleteTree( String path ) throws Exception {
        ZKUtils.deleteTree( zookeeper, getPath( path ) );
    }

    @Override
    public boolean exists( String path ) throws Exception {
        return exists( path, false );
    }

    public void deletNode( String path ) throws Exception {
        this.deleteNode( path, -1 );
    }

    @Override
    public boolean exists( String path, boolean watch ) throws Exception {
        return null != zookeeper.exists( getPath( path ), watch );
    }

    @Override
    public String getAuth() {
        return StringUtils.isBlank( userName ) ? "" : userName.concat( ":" ).concat( password );
    }

    @Override
    public void auth( String auth ) {
        try {
            reConnection();
        } catch ( Exception e1 ) {
            logger.error( "重连zk:{}异常", zkConnectString );
        }
        auth( auth, auth.getBytes( Charsets.UTF_8 ) );
    }

    @Override
    public String auth( String userName, String pwd ) {
        return auth( getAuthScheme(), userName, pwd );
    }

    @Override
    public List< ACL > addAuthInfo( String userName, String pwd ) {
        return this.addAuthInfo( getAuthScheme(), userName, pwd );
    }

    @Override
    public List< ACL > addAuthInfo( String authScheme, String userName, String pwd ) {
        List< ACL > acls = new ArrayList<>();
        String      auth = auth( authScheme, userName, pwd );
        try {
            acls.add( new ACL( ZooDefs.Perms.ALL, new Id( authScheme, DigestAuthenticationProvider.generateDigest( auth ) ) ) );
        } catch ( NoSuchAlgorithmException e ) {
            logger.error( "添加认证失败:{}", auth );
        }
        return acls;
    }

    public String auth( String authScheme, String userName, String pwd ) {
        try {
            reConnection();
        } catch ( Exception e1 ) {
            logger.error( "重连zk:{}异常", zkConnectString );
        }
        String auth = userName.concat( ":" ) + pwd;
        auth( authScheme, auth.getBytes( Charsets.UTF_8 ) );
        return auth;
    }

    private void auth( String authScheme, byte[] auth ) {
        zookeeper.addAuthInfo( authScheme, auth );
    }

    public String getPath( String path ) {
        if ( !StringUtils.startsWith( path, "/" ) ) {
            path = "/" + path;
        }
        if ( StringUtils.startsWith( path, getRootPath() ) ) {
            return path;
        }
        return getRootPath() + path;
    }

    public String getRootPath() {
        if ( StringUtils.isBlank( rootPath ) ) {
            rootPath = ZkGlobalConst.APP_ROOT_PATH;
        }
        return rootPath;
    }

    private void sessionEvent( CountDownLatch latch, WatchedEvent event ) {
        if ( event.getState() == Watcher.Event.KeeperState.SyncConnected ) {
            logger.info( "连接ZK:{}成功!", zkConnectString );
            latch.countDown();
        } else if ( event.getState() == Watcher.Event.KeeperState.Expired ) {
            logger.error( "ZK会话超时，等待重新建立ZK连接:{}", zkConnectString );
            try {
                reConnection();
            } catch ( Exception e ) {
                logger.error( e.getMessage(), e );
            }
        } else if ( event.getState() == Watcher.Event.KeeperState.Disconnected ) {
            logger.info( "等待重新建立ZK连接:{}", zkConnectString );
            try {
                reConnection();
            } catch ( Exception e ) {
                logger.error( e.getMessage(), e );
            }
        } else {
            logger.info( "ZK会话有其他状态的值，event.state:{},event.value:{}", event.getState(), event.toString() );
            latch.countDown();
        }
    }

    public String getZkConnectString() {
        return zkConnectString;
    }
    public void setZkConnectString( String zkConnectString ) {
        this.zkConnectString = zkConnectString;
    }

    public int getZkSessionTimeout() {
        return zkSessionTimeout;
    }
    public void setZkSessionTimeout( int zkSessionTimeout ) {
        this.zkSessionTimeout = zkSessionTimeout;
    }

    public void setAcls( List< ACL > acls ) {
        this.acls = acls;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword( String password ) {
        this.password = password;
    }

    public String getAuthScheme() {
        return authScheme;
    }
    public void setAuthScheme( String authScheme ) {
        this.authScheme = authScheme;
    }

    public void setRootPath( String rootPath ) {
        this.rootPath = rootPath;
    }

}
