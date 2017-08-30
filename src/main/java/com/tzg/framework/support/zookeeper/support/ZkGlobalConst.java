package com.tzg.framework.support.zookeeper.support;

import org.springframework.beans.factory.annotation.Value;

public final class ZkGlobalConst {

    public static String DEFAULT_ROOT = "/tzg/dolphin";

    public static String DEFAULT_VERSION = "1.0.0";

    public static String APP_ROOT_PATH = DEFAULT_ROOT + "/" + DEFAULT_VERSION;

    /** 锁根节点路径 */
    public final static String LOCK_ROOT = DEFAULT_ROOT + "/" + DEFAULT_VERSION + "/locks/";

    @Value( "#{props['rootNode']}" )
    public void setDefaultRoot( String defaultRoot ) {
        DEFAULT_ROOT = defaultRoot;
    }

    @Value( "#{props['version']}" )
    public void setDefaultVersion( String defaultVersion ) {
        DEFAULT_VERSION = defaultVersion;
    }

}
