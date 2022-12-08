package net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import model.AbstractMessage;
import model.CommandMessageWithInfo;

public class NettyNet {
    private SocketChannel channel;
    private OnMessageReceived onMessageReceived;

    public NettyNet(OnMessageReceived onmr){
        this.onMessageReceived = onmr;
        new Thread(()->{
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture future =
                        bootstrap.channel(NioSocketChannel.class).group(group)
                                .handler(new ChannelInitializer<SocketChannel>() {

                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        channel = socketChannel;
                                        socketChannel.pipeline().addLast(
                                                new ObjectEncoder(),
                                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                                new ClientMsgHandler(onMessageReceived)
                                        );
                                    }
                                }).connect("localhost", 8081).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();
    }

    public void sendMessage(AbstractMessage msg){
        channel.writeAndFlush(msg);
    }


}
