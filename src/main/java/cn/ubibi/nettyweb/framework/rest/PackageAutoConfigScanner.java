package cn.ubibi.nettyweb.framework.rest;

import cn.ubibi.nettyweb.framework.commons.scan.ClasspathPackageScanner;
import cn.ubibi.nettyweb.framework.ioc.ServiceManager;
import cn.ubibi.nettyweb.framework.rest.annotation.*;
import cn.ubibi.nettyweb.framework.rest.ifs.IComponent;
import cn.ubibi.nettyweb.framework.rest.utils.ComponentManager;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class PackageAutoConfigScanner extends ClasspathPackageScanner{

    public PackageAutoConfigScanner(String packageName) {
        super(packageName);
    }


    public void doConfig(WebServer webServer) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        List<String> classNameList = this.getFullyQualifiedClassNameList();
        ClassLoader classLoader = webServer.getClass().getClassLoader();


        for (String className : classNameList) {

            Class<?> clazz = Class.forName(className, true, classLoader);
            Annotation[] annotations = clazz.getAnnotations();
            if (annotations != null && annotations.length > 0) {

                for (Annotation annotation : annotations) {


                    //1
                    if (annotation.annotationType() == Controller.class) {
                        Controller controllerAnnotation = (Controller) annotation;
                        String controllerPath = controllerAnnotation.value();
                        Object controllerObject = clazz.newInstance();
                        webServer.addController(controllerPath,controllerObject);
                    }


                    //2
                    else if (annotation.annotationType() == Service.class) {
                        Object serviceObject = clazz.newInstance();
                        ServiceManager.getInstance().addService(serviceObject);
                    }

                    else if (annotation.annotationType() == ServiceFactory.class){
                        addByServiceFactory(clazz);
                    }

                    //3
                    else if (annotation.annotationType() == Component.class) {
                        Object componentObject = clazz.newInstance();
                        if (componentObject instanceof IComponent){
                            ComponentManager.getInstance().addComponent((IComponent) componentObject);
                        }
                    }

                    //4
                    else if (annotation.annotationType() == ComponentFactory.class) {
                        addByComponentFactory(clazz);
                    }
                }
            }

        }// for end

    }



    private void addByComponentFactory(Class<?> factoryClazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object serviceFactory = factoryClazz.newInstance();
        Method[] methods = factoryClazz.getDeclaredMethods();

        if (methods != null) {
            for (Method method : methods) {
                Component annotation = method.getAnnotation(Component.class);
                if (annotation != null) {
                    method.setAccessible(true);
                    Object object = method.invoke(serviceFactory);
                    if (object instanceof IComponent) {
                        ComponentManager.getInstance().addComponent((IComponent) object);
                    }
                }
            }
        }
    }




    private void addByServiceFactory(Class<?> factoryClazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object serviceFactory = factoryClazz.newInstance();
        Method[] methods = factoryClazz.getDeclaredMethods();

        if (methods != null) {
            for (Method method : methods) {
                Service annotation = method.getAnnotation(Service.class);
                if (annotation != null) {
                    method.setAccessible(true);
                    Object object = method.invoke(serviceFactory);
                    if (object != null) {
                        ServiceManager.getInstance().addService(object);
                    }
                }
            }
        }
    }
}
