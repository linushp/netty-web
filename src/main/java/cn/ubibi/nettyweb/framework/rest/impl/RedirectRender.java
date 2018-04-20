package cn.ubibi.nettyweb.framework.rest.impl;

import cn.ubibi.nettyweb.framework.rest.ifs.ResponseRender;
import cn.ubibi.nettyweb.framework.rest.model.Config;
import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.TEMPORARY_REDIRECT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 重定向
 */
public class RedirectRender implements ResponseRender {

    private String location;


    public RedirectRender(String location) {
        this.location = location;
    }


    @Override
    public void doRender(ControllerRequest request, ChannelHandlerContext ctx) throws Exception {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, TEMPORARY_REDIRECT);

        response.headers().set(HttpHeaderNames.LOCATION, this.location);

        ChannelFuture future = ctx.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);

    }
}
