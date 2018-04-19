package cn.ubibi.nettyweb.framework.rest.ifs;

import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;

import java.lang.reflect.Method;

public interface AspectComponent extends IComponent {

    void invokeBefore(Method method, ControllerRequest request) throws Exception;

    void invokeAfter(Method method, ControllerRequest request, Object invokeResult) throws Exception;
}
