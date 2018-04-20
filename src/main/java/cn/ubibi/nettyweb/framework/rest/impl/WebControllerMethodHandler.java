package cn.ubibi.nettyweb.framework.rest.impl;

import cn.ubibi.nettyweb.framework.commons.CastTypeUtils;
import cn.ubibi.nettyweb.framework.commons.CollectionUtils;
import cn.ubibi.nettyweb.framework.commons.StringUtils;
import cn.ubibi.nettyweb.framework.commons.StringWrapper;
import cn.ubibi.nettyweb.framework.rest.annotation.*;
import cn.ubibi.nettyweb.framework.rest.ifs.AspectComponent;
import cn.ubibi.nettyweb.framework.rest.ifs.MethodArgumentComponent;
import cn.ubibi.nettyweb.framework.rest.ifs.ResponseRender;
import cn.ubibi.nettyweb.framework.rest.ifs.WebRequestHandler;
import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import cn.ubibi.nettyweb.framework.rest.model.MethodArgument;
import cn.ubibi.nettyweb.framework.rest.utils.ComponentManager;
import cn.ubibi.nettyweb.framework.rest.utils.UriUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebControllerMethodHandler implements WebRequestHandler{

    private String context;
    private String methodPath;
    private String requestMethod;
    private Method method;
    private String targetPath;
    private Object controller;

    public WebControllerMethodHandler(Object controller,String context, String methodPath, String requestMethod, Method controllerMethod) {
        this.controller = controller;
        this.context = context;
        this.methodPath = methodPath;
        this.requestMethod = requestMethod;
        this.method = controllerMethod;
        this.targetPath = pathJoin(context,methodPath);
    }


    @Override
    public Object isHandle(FullHttpRequest request) {
        if (this.requestMethod.equalsIgnoreCase(request.method().name())) {


            String uri = request.uri();

            String reqPath = UriUtils.getPathInfo(uri);


            if (targetPath.equals(reqPath)) {
                return true;
            }

            //   /user/abc
            //   /user/:id
            //   /user/23332
            List<String> path1Array = CollectionUtils.removeEmpty(targetPath.split("/"));
            List<String> path2Array = CollectionUtils.removeEmpty(reqPath.split("/"));
            if (path1Array.size() != path2Array.size()) {
                return false;
            }

            for (int i = 0; i < path1Array.size(); i++) {
                String pp1 = path1Array.get(i);
                String pp2 = path2Array.get(i);
                if (!isPathEquals(pp1, pp2)) {
                    return false;
                }
            }

            return true;

        }
        return false;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request, Object isHandleResult) throws Exception {

        ControllerRequest controllerRequest  = new ControllerRequest(request,context,targetPath);

        Object invokeResult;
        try {

            List<AspectComponent> methodWrappers = ComponentManager.getInstance().getAspectComponentList();

            //Aspect前置
            for (AspectComponent methodWrapper : methodWrappers) {
                methodWrapper.invokeBefore(method, controllerRequest);
            }

            //准备参数
            Object[] paramsObjects = getMethodParamsObjects(method, controllerRequest);
            //方法调用
            invokeResult = method.invoke(controller, paramsObjects);


            //Aspect后置
            for (AspectComponent methodWrapper : methodWrappers) {
                methodWrapper.invokeAfter(method, controllerRequest, invokeResult);
            }


        } catch (Exception e) {
            throw e;
        }


        //2.执行Render
        if (invokeResult instanceof ResponseRender) {
            ((ResponseRender) invokeResult).doRender(controllerRequest, ctx);
        } else if (invokeResult instanceof String) {
            new TextRender(invokeResult.toString()).doRender(controllerRequest, ctx);
        } else {
            new JsonRender(invokeResult).doRender(controllerRequest, ctx);
        }
    }




    private Object[] getMethodParamsObjects(Method method, ControllerRequest controllerRequest) {
        int paramsCount = method.getParameterCount();
        Type[] paramsTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();


        if (paramsCount == 0) {
            return new Object[]{};
        }


        List<Object> objects = new ArrayList<>();

        for (int i = 0; i < paramsCount; i++) {

            Type type = paramsTypes[i];
            Annotation[] annotations = paramAnnotations[i];

            MethodArgument methodArgument = new MethodArgument(method, type, annotations);

            Object object;
            MethodArgumentComponent methodArgumentResolver = findMethodArgumentResolver(methodArgument);
            if (methodArgumentResolver != null) {
                object = methodArgumentResolver.resolveArgument(methodArgument, controllerRequest);
            } else {
                object = getMethodParamsObject(methodArgument, controllerRequest);
            }

            objects.add(object);
        }

        return objects.toArray();
    }



    private Object getMethodParamsObject(MethodArgument methodArgument, ControllerRequest controllerRequest) {

        Class typeClazz = (Class) methodArgument.getType();

        Annotation[] annotations = methodArgument.getAnnotations();

        Annotation annotation = null;
        if (annotations != null && annotations.length > 0) {
            annotation = annotations[0];
        }


        Object object = null;

        //1.通过注解注入
        if (annotation != null) {

            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == RequestParam.class) {

                RequestParam requestParam = (RequestParam) annotation;
                String paramName = requestParam.value();
                Class elementType = requestParam.elementType();
                if (typeClazz.isArray()) {
                    elementType = typeClazz.getComponentType();
                    object = getArrayParamValue(controllerRequest, paramName, elementType);
                } else if (List.class.isAssignableFrom(typeClazz)) {
                    object = getListParamValue(controllerRequest, paramName, elementType);
                } else if (Set.class.isAssignableFrom(typeClazz)) {
                    object = getSetParamValue(controllerRequest, paramName, elementType);
                } else {
                    StringWrapper sw = controllerRequest.getRequestParam(paramName, requestParam.defaultValue());
                    object = CastTypeUtils.castValueType(sw, typeClazz);
                }

            } else if (annotationType == RequestParams.class) {
                object = controllerRequest.getRequestParamObject(typeClazz);
            } else if (annotationType == RequestBody.class) {
                object = controllerRequest.getRequestBodyObject(typeClazz);
            } else if (annotationType == PathVariable.class) {
                PathVariable requestPath = (PathVariable) annotation;
                String sw = controllerRequest.getPathVariable(requestPath.value());
                object = CastTypeUtils.castValueType(sw, typeClazz);
            } else if (annotationType == AspectVariable.class) {
                AspectVariable aspectVariable = (AspectVariable) annotation;
                String aspectVariableName = aspectVariable.value();
                if (!aspectVariableName.isEmpty()) {
                    object =  controllerRequest.getAspectVariable(aspectVariableName);
                } else {
                    object = controllerRequest.getAspectVariableByClassType(typeClazz);
                }
            }
        }


        //2.通过类型注入
        if (object == null) {
            if (typeClazz.equals(ControllerRequest.class)) {
                object = controllerRequest;
            }
        }

        return object;
    }



    private List getListParamValue(ControllerRequest controllerRequest, String paramName, Class elementType) {
        StringWrapper[] swArray = controllerRequest.getRequestParams(paramName);
        if (swArray == null || swArray.length == 0) {
            return new ArrayList();
        }


        List<Object> objectList = new ArrayList<>();

        for (int i = 0; i < swArray.length; i++) {

            StringWrapper sw = swArray[i];

            Object value = CastTypeUtils.castValueType(sw, elementType);

            objectList.add(value);
        }


        return objectList;
    }

    private Object[] getArrayParamValue(ControllerRequest jettyBootReqParams, String paramName, Class elementType) {
        List list = getListParamValue(jettyBootReqParams, paramName, elementType);
        return list.toArray();
    }

    private Set getSetParamValue(ControllerRequest jettyBootReqParams, String paramName, Class elementType) {
        List list = getListParamValue(jettyBootReqParams, paramName, elementType);
        return new HashSet(list);
    }




    private MethodArgumentComponent findMethodArgumentResolver(MethodArgument methodArgument) {
        List<MethodArgumentComponent> methodArgumentResolvers = ComponentManager.getInstance().getMethodArgumentComponentList();
        for (MethodArgumentComponent resolver : methodArgumentResolvers) {
            if (resolver.isSupport(methodArgument)) {
                return resolver;
            }
        }
        return null;
    }


    public int compareTo(WebControllerMethodHandler o) {
        return this.targetPath.compareTo(o.targetPath);
    }

    private boolean isPathEquals(String configPath, String pp2) {
        if (configPath.equals(pp2)) {
            return true;
        }

        if (configPath.startsWith(":")) {
            return true;
        }

        return false;
    }

    private String pathJoin(String path1, String path2) {

        List<String> path1Arr = CollectionUtils.removeEmpty(path1.split("/"));
        List<String> path2Arr = CollectionUtils.removeEmpty(path2.split("/"));


        List<String> pathList = new ArrayList<>();

        pathList.addAll(path1Arr);
        pathList.addAll(path2Arr);

        String result = "/" + StringUtils.join(pathList, "/");
        return result;
    }
}
