package SecureChatClient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.ssl.SslContext;


public class SecureChatClientInitializerMy extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public SecureChatClientInitializerMy(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(sslCtx.newHandler(ch.alloc(), SecureChatClient.HOST, SecureChatClient.PORT));
        pipeline.addLast("frameDecoder",
                new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2));
        pipeline.addLast("frameEncoder",
                new LengthFieldPrepender(2));
        pipeline.addLast("bytesDecoder",
                new ByteArrayDecoder());
        pipeline.addLast("bytesEncoder",
                new ByteArrayEncoderMy());

        // and then business logic.
        pipeline.addLast(new SecureChatClientHandler());
        pipeline.addLast(new SecureChatClientHandlerOut());
    }
}
