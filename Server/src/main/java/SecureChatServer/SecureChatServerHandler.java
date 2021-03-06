package SecureChatServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles a server-side channel.
 */
public class SecureChatServerHandler extends SimpleChannelInboundHandler<byte[]> {

private List<List<String>> listCommandIn = new LinkedList<>();
private boolean doAutorization = false;

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        ctx.writeAndFlush((byte[])
                                new String("Welcome to secure chat service!\n").getBytes(StandardCharsets.UTF_8));
                        ctx.writeAndFlush(
                                "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite.\n");


                    }
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        doListCommand(msg);

        autorization(ctx);




        // Close the connection if the client has sent 'bye'.
        if ("exit".equals(msg.toString().toLowerCase())) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void doListCommand(byte[] msg) {

       /* for (byte b : msg) {
            System.out.println(b);

        }*/


        if (msg[0] == (byte) 49) {
            List<String> list = new ArrayList<>();

            String str = new String(msg, 1, msg.length - 1);
            for (int i = 1; i < str.split("\n").length - 1; i++) {
                list.add(str.split("\n")[i]);
            }
            listCommandIn.add(list);
        }
    }
    private void autorization(ChannelHandlerContext ctx){

        if (!doAutorization & listCommandIn.size()>0){
            List<String> list = new ArrayList<>();
            for (int i = 0; i < listCommandIn.size(); i++) {
                list.addAll(listCommandIn.get(0));


                if (list.get(0).indexOf("newUser")!=-1 | list.get(0).indexOf("user")!=-1){
                    doAutorization=true;


                    String cmd = (list.get(0).indexOf("newUser")!=-1) ? "\"Instruction\": \"newUser\"":"\"Instruction\": \"user\"";
                    System.out.println("DEBUG data _________");
                    System.out.println(cmd);
                    String str = String.format("{%n" +
                                    "%s,%n" +
                                    "%s" +
                                    "%n}",
                            cmd,
                            "\"rezl\": \"successful\"");
                    System.out.println("DEBUG data" + str);
                    ctx.writeAndFlush(str.getBytes(StandardCharsets.UTF_8));
                    System.out.println("DEBUG data" + str);
                    listCommandIn.remove(0);
                    break;
                } else {
                    listCommandIn.remove(0);
                }
            }


        }
    }


}