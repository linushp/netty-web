package cn.ubibi.nettyweb.framework.rest.ifs;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface WebRequestHandler {

    Object isHandle(FullHttpRequest req);

    void handle(ChannelHandlerContext ctx, FullHttpRequest msg,Object isHandleResult) throws Exception;

}
