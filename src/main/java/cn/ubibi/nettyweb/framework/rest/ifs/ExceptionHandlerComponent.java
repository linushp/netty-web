package cn.ubibi.nettyweb.framework.rest.ifs;

import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import io.netty.channel.ChannelHandlerContext;


public interface ExceptionHandlerComponent extends IComponent{
    boolean handle(Exception e, ControllerRequest request, ChannelHandlerContext ctx) throws Exception;
}
