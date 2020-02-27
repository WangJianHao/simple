package com.wjh.netty;

import com.wjh.controller.NettyController;
import com.wjh.dao.UserDaoImpl;
import com.wjh.service.NettyControllerDaoImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //找到每个用户对应的nettyController
        NettyController nettyController = NettyControllerDaoImpl.getNettyControllerByIP(
                ctx.channel().remoteAddress().toString());
        nettyController.channel = ctx.channel();
        //文件接收分了两次，那么可以以是否有}来判断是否为结尾

        if(!msg.toString().endsWith("}")){
            if(nettyController.message.endsWith("}")){
                nettyController.message = "";
            }
            nettyController.message +=  msg.toString();
        }else {
            String message = nettyController.message + msg.toString();
            //打印服务器接收到的信息
            System.out.println(ctx.channel().remoteAddress() + "：" + message);

            //返回结果并回写给客户端
            String result = nettyController.process(message);
            if (result != null) {
//            System.out.println(result);
                ctx.writeAndFlush(result);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"上线了。。。");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UserDaoImpl userDao = new UserDaoImpl();
        String ip_address = ctx.channel().remoteAddress().toString();
        userDao.offline(ip_address);
        NettyControllerDaoImpl.removeIP(ip_address);
        System.out.println(ctx.channel().remoteAddress()+"下线了。。。");
    }
}
