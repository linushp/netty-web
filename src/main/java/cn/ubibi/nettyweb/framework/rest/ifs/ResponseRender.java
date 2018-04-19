package cn.ubibi.nettyweb.framework.rest.ifs;


import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import io.netty.channel.ChannelHandlerContext;


public interface ResponseRender {
    void doRender(ControllerRequest request,ChannelHandlerContext ctx) throws Exception;
}
