package SecureChatServer;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class SecureChatServerInitializerMy extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;


    public SecureChatServerInitializerMy(SslContext sslCtx) {
        this.sslCtx = sslCtx;

    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.
        pipeline.addLast(sslCtx.newHandler(ch.alloc()));

        // On top of the SSL handler, add the text line codec.
        pipeline.addLast("frameDecoder",
                new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2));
        pipeline.addLast("frameEncoder",
                new LengthFieldPrepender(2));
        pipeline.addLast("bytesDecoder",
                new ByteArrayDecoderMy());
        pipeline.addLast("bytesEncoder", new ByteArrayEncoder());


        // and then business logic.
        pipeline.addLast(new SecureChatServerHandler());
        pipeline.addLast(new SecureChatServerHandlerOut());
    }
}
