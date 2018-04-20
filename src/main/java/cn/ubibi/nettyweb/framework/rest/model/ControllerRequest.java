package cn.ubibi.nettyweb.framework.rest.model;

import cn.ubibi.nettyweb.framework.commons.StringWrapper;
import cn.ubibi.nettyweb.framework.rest.utils.UriUtils;
import com.alibaba.fastjson.JSON;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;


import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;


public class ControllerRequest {

    private Map<String, List<String>> queryStringParameters;
    private Map<String, Object> aspectVariable;
    private FullHttpRequest fullHttpRequest;

    private String context;
    private String targetPath;
    private String uri;
    private String pathInfo;
    private Map<String, String> _pathVariable;

    public ControllerRequest(FullHttpRequest fullHttpRequest, String context, String targetPath) {
        this.fullHttpRequest = fullHttpRequest;
        this.context = context;
        this.targetPath = targetPath;
        this.uri = fullHttpRequest.uri();
        this.pathInfo = UriUtils.getPathInfo(uri);
        Charset charset = Config.getInstance().getCharset();
        this.queryStringParameters = new QueryStringDecoder(uri, charset).parameters();
        this.aspectVariable = new HashMap<>();
    }




    public String getHeaderCookie() {
        HttpHeaders headers = fullHttpRequest.headers();
        return headers.get(HttpHeaderNames.COOKIE);
    }


    public String getHeader(String name) {
        HttpHeaders headers = fullHttpRequest.headers();
        return headers.get(name);
    }




    public StringWrapper[] getRequestParams(String paramName) {
        List<String> mm = queryStringParameters.get(paramName);
        List<StringWrapper> ss = new ArrayList<>();

        for (String s : mm) {
            ss.add(new StringWrapper(s));
        }
        return (StringWrapper[]) ss.toArray();
    }

    public StringWrapper getRequestParam(String paramName, String defaultValue) {
        List<String> mm = queryStringParameters.get(paramName);
        if (mm == null || mm.isEmpty()) {
            return new StringWrapper(defaultValue);
        }
        return new StringWrapper(mm.get(0));
    }


    public String getParameter(String paramName, String defaultValue) {
        List<String> mm = queryStringParameters.get(paramName);
        if (mm == null || mm.isEmpty()) {
            return defaultValue;
        }
        return mm.get(0);
    }


    private String[] getParameterValues(String paramName) {
        List<String> mm = queryStringParameters.get(paramName);
        if (mm == null || mm.isEmpty()) {
            return new String[]{};
        }
        return (String[]) mm.toArray();
    }


    public <T> T getRequestParamObject(Class<? extends T> clazz) {

        Map<String, Object> map2 = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            if (fieldType.isArray() || List.class.isAssignableFrom(fieldType)) {
                map2.put(fieldName, getParameterValues(fieldName));
            } else {
                map2.put(fieldName, getParameter(fieldName, null));
            }
        }


        String mapString = JSON.toJSONString(map2);
        T obj = JSON.parseObject(mapString, clazz);

        return obj;
    }


    public Object getRequestBodyObject(Class typeClazz) {
        return null;
    }


    public String getPathVariable(String name) {
        if (this._pathVariable != null) {
            return (this._pathVariable.get(name));
        }

        this._pathVariable = new HashMap<>();


        String[] pathInfoArray = pathInfo.split("/");
        String[] targetPathArray = this.targetPath.split("/");

        for (int i = 0; i < targetPathArray.length; i++) {
            String p1 = targetPathArray[i];
            String p2 = pathInfoArray[i];
            if (p1.startsWith(":")) {
                String k = p1.replaceFirst(":", "");
                this._pathVariable.put(k, p2);
            }
        }
        return this._pathVariable.get(name);
    }


    public FullHttpRequest getFullHttpRequest() {
        return fullHttpRequest;
    }

    public String getContext() {
        return context;
    }


    public String getTargetPath() {
        return targetPath;
    }


    public Object getAspectVariable(String name) {
        if (this.aspectVariable == null) {
            return null;
        }
        return this.aspectVariable.get(name);
    }


    public Object getAspectVariableByClassType(Class clazz) {

        Collection<Object> values = this.aspectVariable.values();
        for (Object obj : values) {
            if (obj.getClass() == clazz || obj.getClass().equals(clazz)) {
                return obj;
            }
        }

        return null;
    }


    public void setAspectVariable(String name, Object aspectVariable) {
        this.aspectVariable.put(name, aspectVariable);
    }

    public String getUri() {
        return uri;
    }

    public String getPathInfo() {
        return pathInfo;
    }
}

