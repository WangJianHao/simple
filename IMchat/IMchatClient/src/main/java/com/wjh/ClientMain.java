package com.wjh;

import com.wjh.netty.NettyClient;



public class ClientMain {
    public static void main(String[] args) {
         NettyClient.start("127.0.0.1",8888);
    }
}
