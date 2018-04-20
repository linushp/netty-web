package cn.ubibi.nettyweb.framework.rest.model;

import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class Config {

    private static Config instance = new Config();
    private Config(){}
    public static Config getInstance(){
        return instance;
    }

    private Charset charset = CharsetUtil.UTF_8;

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
