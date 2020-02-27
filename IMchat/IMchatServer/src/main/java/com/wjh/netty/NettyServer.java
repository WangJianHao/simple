package com.wjh.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    /**
     * 按照端口启动服务器
     * @param port
     */
    public static void start(int port){
        //先创建两个事件循环组
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(10);

        //实例化启动辅助类
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.
                //绑定事件循环组
                group(boss,worker)
                //给定channel类型
                .channel(NioServerSocketChannel.class)
                //自定义channelHandler
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();

                        //添加编码
                        pipeline.addLast(new StringDecoder());//String类型的解码
                        pipeline.addLast(new StringEncoder());//String类型的编码

                        //添加自定义的ChannelHandler
                        pipeline.addLast(new ServerHandler());
                    }
                });
        try {
            //同步启动
            ChannelFuture sync = serverBootstrap.bind(port).sync();
            System.out.println("聊天服务器已启动。。。");
            //同步关闭
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
