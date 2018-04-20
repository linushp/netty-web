package cn.ubibi.nettyweb.framework.rest;

import cn.ubibi.nettyweb.framework.rest.ifs.WebRequestHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.List;
import java.util.concurrent.Executor;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebServerHandler extends SimpleChannelInboundHandler<Object> {

    private List<WebRequestHandler> webRequestHandlerList;
    private Executor executor;


    public WebServerHandler(List<WebRequestHandler> webRequestHandlerList, Executor executor) {
        this.webRequestHandlerList = webRequestHandlerList;
        this.executor = executor;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {


            FullHttpRequest req = (FullHttpRequest) msg;
            for (WebRequestHandler webRequestHandler : webRequestHandlerList) {
                Object isHandleRequest = webRequestHandler.isHandle(req);
                if (isHandleRequest!=null || Boolean.TRUE.equals(isHandleRequest)) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                webRequestHandler.handle(ctx, req,isHandleRequest);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return;
                }
            }


            ctx.writeAndFlush(get404Response()).addListener(ChannelFutureListener.CLOSE);


        } else {
            System.out.println(msg.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }



    private FullHttpResponse get404Response() {
        byte[] CONTENT = "404 Page Not Found".getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

}
