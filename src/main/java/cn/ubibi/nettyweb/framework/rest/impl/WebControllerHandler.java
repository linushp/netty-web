package cn.ubibi.nettyweb.framework.rest.impl;

import cn.ubibi.nettyweb.framework.ioc.ServiceManager;
import cn.ubibi.nettyweb.framework.rest.annotation.DeleteMapping;
import cn.ubibi.nettyweb.framework.rest.annotation.GetMapping;
import cn.ubibi.nettyweb.framework.rest.annotation.PostMapping;
import cn.ubibi.nettyweb.framework.rest.annotation.PutMapping;
import cn.ubibi.nettyweb.framework.rest.ifs.WebRequestHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebControllerHandler implements WebRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");


    private String context;
    private Object controller;
    private Class controllerClass;
    private List<WebControllerMethodHandler> methodHandlerList;

    public WebControllerHandler(String context, Object controller) {
        this.context = context;
        this.controller = controller;
        this.controllerClass = controller.getClass();
        this.methodHandlerList = toWebControllerMethodHandler(this.controllerClass);
    }



    private List<WebControllerMethodHandler> toWebControllerMethodHandler(Class controllerClass) {


        List<WebControllerMethodHandler> methodList = new ArrayList<>();

        Method[] methods = controllerClass.getMethods();


        if (methods != null) {
            for (Method method : methods) {

                String context = this.context;
                Object controller = this.controller;


                GetMapping methodAnnotation1 = method.getAnnotation(GetMapping.class);
                PostMapping methodAnnotation2 = method.getAnnotation(PostMapping.class);
                PutMapping methodAnnotation3 = method.getAnnotation(PutMapping.class);
                DeleteMapping methodAnnotation4 = method.getAnnotation(DeleteMapping.class);


                WebControllerMethodHandler controllerMethodHandler = null;
                if (methodAnnotation1 != null) {
                    controllerMethodHandler = new WebControllerMethodHandler(controller,context,methodAnnotation1.value(), "get", method);
                } else if (methodAnnotation2 != null) {
                    controllerMethodHandler = new WebControllerMethodHandler(controller,context,methodAnnotation2.value(), "post", method);
                } else if (methodAnnotation3 != null) {
                    controllerMethodHandler = new WebControllerMethodHandler(controller,context,methodAnnotation3.value(), "put", method);
                } else if (methodAnnotation4 != null) {
                    controllerMethodHandler = new WebControllerMethodHandler(controller,context,methodAnnotation4.value(), "delete", method);
                }

                if (controllerMethodHandler != null) {
                    methodList.add(controllerMethodHandler);
                }

            }
        }



        //排序之后路径比较长的优先匹配到
        methodList.sort(new Comparator<WebControllerMethodHandler>() {
            @Override
            public int compare(WebControllerMethodHandler o1, WebControllerMethodHandler o2) {
                return o2.compareTo(o1);
            }
        });



        for (WebControllerMethodHandler methodHandler:methodList){
            LOGGER.info("ControllerMethodHandler : " + methodHandler.toString());
        }


        return methodList;

    }




    @Override
    public Object isHandle(FullHttpRequest req) {
        String uri = req.uri();
        if (!uri.startsWith(this.context) || this.methodHandlerList.isEmpty()){
            return null;
        }


        for (WebControllerMethodHandler webControllerMethodHandler:this.methodHandlerList){
            Object isMethodHandleResult = webControllerMethodHandler.isHandle(req);
            if (Boolean.TRUE.equals(isMethodHandleResult)){
                return webControllerMethodHandler;
            }
        }

        return null;
    }




    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request,Object isHandleResult) throws Exception {
        if (isHandleResult instanceof WebControllerMethodHandler){

            WebControllerMethodHandler webControllerMethodHandler = (WebControllerMethodHandler) isHandleResult;

            webControllerMethodHandler.handle(ctx,request,true);

//            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


    private FullHttpResponse get404Response() {
        byte[] CONTENT = "hello world".getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }


    public String getContext() {
        return context;
    }

}

