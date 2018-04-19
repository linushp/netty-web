package cn.ubibi.nettyweb.framework.rest.impl;

import cn.ubibi.nettyweb.framework.rest.ifs.ResponseRender;
import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class TextRender implements ResponseRender{

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");

    private String text;

    public TextRender(String text) {
        if (text == null){
            this.text = "null";
        }else {
            this.text = text;
        }
    }



    @Override
    public void doRender(ControllerRequest request, ChannelHandlerContext ctx) throws Exception {

        byte[] CONTENT = this.text.getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());


        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);


    }
}
