package cn.ubibi.nettyweb.framework.rest;

import cn.ubibi.nettyweb.framework.rest.ifs.WebRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.List;
import java.util.concurrent.Executor;

public class WebServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private List<WebRequestHandler> webRequestHandler;
    private Executor executor;


    public WebServerHandlerInitializer(List<WebRequestHandler> webRequestHandler, Executor executor) {
        this.webRequestHandler = webRequestHandler;
        this.executor = executor;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(new HttpResponseEncoder());

        p.addLast(new ChunkedWriteHandler());
        p.addLast(new WebServerHandler(webRequestHandler,executor));
    }
}
