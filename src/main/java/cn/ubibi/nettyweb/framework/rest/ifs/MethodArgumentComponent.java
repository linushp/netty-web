package cn.ubibi.nettyweb.framework.rest.ifs;

import cn.ubibi.nettyweb.framework.rest.model.ControllerRequest;
import cn.ubibi.nettyweb.framework.rest.model.MethodArgument;

public interface MethodArgumentComponent extends IComponent{

    boolean isSupport(MethodArgument methodArgument);

    Object resolveArgument(MethodArgument methodArgument, ControllerRequest request);

}