package cn.ubibi.nettyweb.framework.rest;

import cn.ubibi.nettyweb.framework.commons.scan.ClasspathPackageScanner;
import cn.ubibi.nettyweb.framework.commons.scan.PackageScanner;
import cn.ubibi.nettyweb.framework.rest.ifs.WebRequestHandler;
import cn.ubibi.nettyweb.framework.rest.impl.WebControllerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class WebServer {

    private int nBossGroup = 1;
    private int nWorkerGroup = 0;
    private InetSocketAddress inetSocketAddress;
    private List<WebRequestHandler> webRequestHandler;
    private Executor executor;



    public WebServer(int inetPort){
        this.inetSocketAddress = new InetSocketAddress(inetPort);
        this.webRequestHandler = new ArrayList<>();
        this.executor = new ThreadPoolExecutor(8, 200, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }


    public void start(){

        List<WebRequestHandler> webRequestHandler = sortWebRequestHandler(this.webRequestHandler);

        EventLoopGroup bossGroup = new NioEventLoopGroup(nBossGroup);
        EventLoopGroup workerGroup = new NioEventLoopGroup(nWorkerGroup);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new WebServerHandlerInitializer(webRequestHandler,executor));

            Channel ch = b.bind(inetSocketAddress).sync().channel();

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }





    public List<WebRequestHandler> sortWebRequestHandler(List<WebRequestHandler> webRequestHandlers) {

        List<WebControllerHandler> controllerHandlers = new ArrayList<>();
        List<WebRequestHandler> otherHandlers = new ArrayList<>();

        for (WebRequestHandler webRequestHandler : webRequestHandlers){
            if (webRequestHandler instanceof WebControllerHandler){
                controllerHandlers.add((WebControllerHandler) webRequestHandler);
            }else {
                otherHandlers.add(webRequestHandler);
            }
        }



        controllerHandlers.sort(new Comparator<WebControllerHandler>() {
            @Override
            public int compare(WebControllerHandler o1, WebControllerHandler o2) {
                String c1 = o1.getContext();
                String c2 = o2.getContext();
                return c2.compareTo(c1);
            }
        });





        List<WebRequestHandler> result = new ArrayList<>();
        result.addAll(controllerHandlers);
        result.addAll(otherHandlers);

        return result;
    }


    public void setnBossGroup(int nBossGroup) {
        this.nBossGroup = nBossGroup;
    }

    public void setnWorkerGroup(int nWorkerGroup) {
        this.nWorkerGroup = nWorkerGroup;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public void addWebRequestHandler(WebRequestHandler webRequestHandler) {
        this.webRequestHandler.add(webRequestHandler);
    }

    public void addController(String context, Object controller){
        this.webRequestHandler.add(new WebControllerHandler(context,controller));
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }


    public void autoConfigByScanPackage(Class mainServerClass) throws Exception {
        String packageName = mainServerClass.getPackage().getName();
        new PackageAutoConfigScanner(packageName).doConfig(this);
    }

}
