package com.wjh.netty;

import com.wjh.service.SendService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class NettyClient {
    /**
     * 启动客户端的方法
     * @param ip
     * @param port
     */
    public static void start(String ip,int port) {
        //创建事件循环组
        NioEventLoopGroup work = new NioEventLoopGroup();

        //创建启动辅助类
        Bootstrap bootstrap = new Bootstrap();

        bootstrap
                //绑定事件循环组
                .group(work)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new ClientHandler());
                    }
                });
        try {
            Channel channel = bootstrap.connect(ip, port).sync().channel();

//            channel.writeAndFlush("Hello World!");
            SendService sendService = new SendService(channel);
            sendService.send();

            //同步关闭
            channel.closeFuture().sync();

              //直接关闭
//            channel.close().syncUninterruptibly();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            work.shutdownGracefully();
        }
    }
}
