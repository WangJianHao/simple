package com.wjh.service;

import com.wjh.controller.NettyController;

import java.util.Hashtable;

public class NettyControllerDaoImpl {
    private static final Hashtable<String,NettyController> hashtable = new Hashtable<>(100);
    /**
     * 通过ip返回对应的NettyController
     * @param ip_address
     * @return
     */
    public static NettyController getNettyControllerByIP(String ip_address) {
        NettyController nettyController = hashtable.get(ip_address);
        if(nettyController == null){//登录
            nettyController = new NettyController();
        }
        hashtable.put(ip_address,nettyController);
        return nettyController;
    }
    public static void removeIP(String ip_address){
        hashtable.remove(ip_address);
    }
}
