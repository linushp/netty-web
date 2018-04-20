package cn.ubibi.nettyweb.framework.rest.impl;

import cn.ubibi.nettyweb.framework.rest.ifs.ResponseRender;
import cn.ubibi.nettyweb.framework.rest.model.Config;
import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class JsonRender implements ResponseRender{

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");

    private Object data;

    public JsonRender(Object data) {
        this.data = data;
    }


    @Override
    public void doRender(ControllerRequest request, ChannelHandlerContext ctx) throws Exception {

        Charset charset = Config.getInstance().getCharset();

        String jsonText = JSON.toJSONString(this.data);

        byte[] CONTENT = jsonText.getBytes(charset);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "application/json; charset="+charset.name());
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }
}
