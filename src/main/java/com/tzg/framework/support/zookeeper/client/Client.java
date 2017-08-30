package com.tzg.framework.support.zookeeper.client;

import com.tzg.framework.support.zookeeper.service.impl.ZKClientServiceImpl;
import com.tzg.framework.support.zookeeper.support.ZKUtils;

import java.util.Arrays;
import java.util.List;

public class Client {

    public static void main( String[] args ) throws Exception {

        String              zkConnStr       = "192.168.1.43:2181";
        ZKClientServiceImpl zkClientService = new ZKClientServiceImpl();
        zkClientService.setZkConnectString( zkConnStr );

        List< String > list = zkClientService.getChildren( "/tzg" );
        list.stream().forEach( System.out::println );

        String[] nodeNames = ZKUtils.getTree( zkClientService.getZooKeeper(), zkClientService.getPath( "/dubbo" ) );
        Arrays.stream( nodeNames ).forEach( System.out::println );

        String data = zkClientService.getData( "/dubbo" );
        System.out.println( "data value is: " + data );

        zkClientService.setData( "/dubbo", "dubbo configuration".getBytes() );
        System.out.println( "new data value is: " + zkClientService.getData( "/dubbo" ) );

        //        zkClientService.createPersistent( "/foo", "bar".getBytes() );
        //        zkClientService.deletNode( "/foo" );
        //        zkClientService.createEphemeral( "/foo", "bar".getBytes() );

        //        zkClientService.deleteTree( "/foo" );

        System.out.println( "Node 'dubbo' exists: " + zkClientService.exists( "/dubbo" ) );

    }

}
