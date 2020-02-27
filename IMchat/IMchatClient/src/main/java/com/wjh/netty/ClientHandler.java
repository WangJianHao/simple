package com.wjh.netty;

import com.wjh.controller.ClientController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler {
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ClientController.channel = ctx.channel();
        //{"msgType":-6,"friendList":"wjh(不在线),wk(不在线),gy(不在线),test2(不在线),"}{"msgType":-5,"sender":"wjh","receiver":"test","msg":"test"}
        //全部一起发回来了
//        System.out.println(msg);

        if(!msg.toString().endsWith("}")){
            if(ClientController.message.endsWith("}")){
                ClientController.message = "";
            }
            ClientController.message +=  msg.toString();
        }else {
            String message = ClientController.message + msg.toString();
            ClientController.process(message);
        }
    }
}
