package com.tzg.framework.support.zookeeper.support;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZKUtils {

    /**
     * 创建节点路径
     *
     * @param zk
     * @param path
     * @param data
     * @param createMode
     * @param acl
     * @return
     * @throws Exception
     */
    public static String create( ZooKeeper zk, String path, byte[] data, CreateMode createMode, List< ACL > acl )
            throws Exception {
        return create( zk, path, data, createMode, acl, false );
    }

    /**
     * 创建节点路径
     *
     * @param zk
     * @param path
     * @param createMode
     * @param acls
     * @return
     * @throws Exception
     */
    public static String create( ZooKeeper zk, String path, byte[] data, CreateMode createMode, List< ACL > acls, boolean checkParent )
            throws Exception {
        path = getPath( path );
        if ( acls == null || acls.isEmpty() ) {
            acls = ( acls == null ? new ArrayList<>() : acls );
            acls.add( new ACL( ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE ) );
        }
        if ( !checkParent ) {
            return zk.create( path, data, acls, createMode );
        }
        String[] list   = path.split( "/" );
        String   zkPath = "";
        for ( String str : list ) {
            if ( str.equals( "" ) ) {
                continue;
            }
            zkPath = zkPath + "/" + str;
            if ( zk.exists( zkPath, false ) != null ) {
                continue;
            }
            zk.create( zkPath, data, acls, createMode );
        }
        return zkPath;
    }

    public static void deleteTree( ZooKeeper zk, String path ) throws Exception {
        path = getPath( path );
        String[] list = getTree( zk, path );
        for ( int i = list.length - 1; i >= 0; i-- ) {
            zk.delete( list[ i ], -1 );
        }
    }

    public static String[] getTree( ZooKeeper zk, String path ) throws Exception {
        path = getPath( path );
        if ( zk.exists( path, false ) == null ) {
            return new String[ 0 ];
        }

        List< String > dealList = new ArrayList< String >();
        dealList.add( path );
        int index = 0;
        while ( index < dealList.size() ) {
            String         tempPath = dealList.get( index );
            List< String > children = zk.getChildren( tempPath, false );
            if ( tempPath.equalsIgnoreCase( "/" ) == false ) {
                tempPath = tempPath + "/";
            }
            Collections.sort( children );
            for ( int i = children.size() - 1; i >= 0; i-- ) {
                dealList.add( index + 1, tempPath + children.get( i ) );
            }
            index++;
        }
        return dealList.toArray( new String[ 0 ] );
    }

    public static String getPath( String path ) {
        if ( !StringUtils.startsWith( path, "/" ) ) {
            path = "/" + path;
        }
        if ( StringUtils.endsWith( path, "/" ) ) {
            path = StringUtils.substringBeforeLast( path, "/" );
        }
        return path;
    }

}
