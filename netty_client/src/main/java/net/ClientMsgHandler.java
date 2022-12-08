package net;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.AbstractMessage;


public class ClientMsgHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final OnMessageReceived onMessageReceived;

    public ClientMsgHandler(OnMessageReceived onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        onMessageReceived.onReceive(abstractMessage);
    }
}
