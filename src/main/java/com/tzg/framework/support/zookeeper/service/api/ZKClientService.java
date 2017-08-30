package com.tzg.framework.support.zookeeper.service.api;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * Zookeeper 客户端接口
 */
public interface ZKClientService {

    /**
     * 获取指定路径下的子节点，不递归获取子节点
     *
     * @param path 节点路径
     */
    List< String > getChildren( String path ) throws Exception;

    /**
     * 递归获取子节点
     *
     * @param path 节点路径
     */
    String[] getTree( String path ) throws Exception;

    /**
     * 获取数据
     *
     * @param path 节点路径
     */
    String getData( String path ) throws Exception;

    /**
     * 获取数据
     *
     * @param path
     * @param stat
     */
    String getData( String path, Stat stat ) throws Exception;

    /**
     * 设置数据
     *
     * @param path
     * @param data
     */
    Stat setData( String path, byte[] data ) throws Exception;

    /**
     * 设置数据
     *
     * @param path
     * @param data
     * @param version
     */
    Stat setData( String path, byte[] data, int version ) throws Exception;

    /**
     * 创建永久节点
     *
     * @param path 节点路径
     * @param data 数据
     * @throws Exception
     */
    boolean createPersistent( String path, byte[] data ) throws Exception;

    /**
     * 创建临时节点, 连接断开节点自动删除
     *
     * @param path 节点路径
     * @param data 数据
     * @return
     * @throws Exception
     */
    boolean createEphemeral( String path, byte[] data ) throws Exception;

    /**
     * 创建节点
     *
     * @param path       路径
     * @param createMode 类型
     * @param data       数据
     * @throws Exception
     */
    boolean create( String path, CreateMode createMode, byte[] data ) throws Exception;

    /**
     * 创建节点
     *
     * @param path       路径
     * @param createMode 类型
     * @param data       数据
     * @param acls       权限访问列表
     * @param enforce    是否强制创建,覆盖已有值
     * @param acls       访问控制列表
     * @throws Exception
     */
    boolean create( String path, CreateMode createMode, byte[] data, boolean enforce, List< ACL > acls )
            throws Exception;

    /**
     * 删除指定路径及版本的节点
     *
     * @param path    节点路径
     * @param version 节点版本
     * @throws Exception
     */
    void deleteNode( String path, int version ) throws Exception;

    /**
     * 删除指定路径下的节点及子节点
     *
     * @param path 节点路径
     * @throws Exception
     */
    void deleteTree( String path ) throws Exception;

    /**
     * 判断节点是否存在
     *
     * @param path
     * @return
     * @throws Exception
     */
    boolean exists( String path ) throws Exception;

    /**
     * 判断节点是否存在
     *
     * @param path
     * @param watch
     * @return
     * @throws Exception
     */
    boolean exists( String path, boolean watch ) throws Exception;

    /**
     * 返回配置的认证信息
     *
     * @return
     */
    String getAuth();

    /**
     * 认证，认证需重新连接,所以此处会关闭连接然后重新连接
     *
     * @param auth 认证信息格式：userName:password
     * @return
     */
    void auth( String auth );

    /**
     * 认证，认证需重新连接,所以此处会关闭连接然后重新连接
     *
     * @param userName
     * @param pwd
     * @return
     */
    String auth( String userName, String pwd );

    /**
     * 认证
     *
     * @param userName
     * @param pwd
     */
    List< ACL > addAuthInfo( String userName, String pwd );

    /**
     * 认证
     *
     * @param authScheme
     * @param userName
     * @param pwd
     */
    List< ACL > addAuthInfo( String authScheme, String userName, String pwd );

}
