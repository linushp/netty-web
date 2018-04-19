package cn.ubibi.nettyweb.framework.rest.utils;

import cn.ubibi.nettyweb.framework.rest.ifs.AspectComponent;
import cn.ubibi.nettyweb.framework.rest.ifs.ExceptionHandlerComponent;
import cn.ubibi.nettyweb.framework.rest.ifs.IComponent;
import cn.ubibi.nettyweb.framework.rest.ifs.MethodArgumentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ComponentManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentManager.class);

    private static final ComponentManager instance = new ComponentManager();

    public static ComponentManager getInstance() {
        return instance;
    }

    private ComponentManager() {
    }


    private List<AspectComponent> aspectComponentList = new ArrayList<>();
    private List<ExceptionHandlerComponent> exceptionComponentList = new ArrayList<>();
    private List<MethodArgumentComponent> methodArgumentComponentList = new ArrayList<>();
    private List<IComponent> components = new ArrayList<>();


    public void addComponent(IComponent object) {
        if (object == null) {
            return;
        }
        if (object instanceof AspectComponent) {
            aspectComponentList.add((AspectComponent) object);
        }

        if (object instanceof ExceptionHandlerComponent) {
            exceptionComponentList.add((ExceptionHandlerComponent) object);
        }

        if (object instanceof MethodArgumentComponent) {
            methodArgumentComponentList.add((MethodArgumentComponent) object);
        }

        components.add(object);
        LOGGER.info("addComponent:" + object.getClass().getName());
    }



    public List<AspectComponent> getAspectComponentList() {
        return aspectComponentList;
    }

    public List<ExceptionHandlerComponent> getExceptionComponentList() {
        return exceptionComponentList;
    }

    public List<MethodArgumentComponent> getMethodArgumentComponentList() {
        return methodArgumentComponentList;
    }

    public List<IComponent> getComponents() {
        return components;
    }
}
