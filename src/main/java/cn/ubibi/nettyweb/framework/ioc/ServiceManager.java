package cn.ubibi.nettyweb.framework.ioc;

import cn.ubibi.nettyweb.framework.rest.ifs.IComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ServiceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);

    private static final ServiceManager instance = new ServiceManager();

    public static ServiceManager getInstance() {
        return instance;
    }

    private ServiceManager() {
    }


    private List<Object> serviceList = new ArrayList<>();



    public void addService(Object object) {
        serviceList.add(object);
        LOGGER.info("addService:" + object.getClass().getName());
    }



    //执行注入依赖的过程
    public void injectDependency(Object controller) throws Exception {
        Field[] fields = controller.getClass().getDeclaredFields();

        if (fields != null && fields.length > 0) {
            for (Field field : fields) {

                //有注解
                Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
                if (autowired != null) {
                    field.setAccessible(true);
                    Object filedValue = field.get(controller);
                    if (filedValue == null) {
                        Object service = findServiceByField(field);
                        if (service != null) {

                            field.set(controller, service);

                            //放在set后面，允许循环依赖，只要调用不循环就行。
                            injectDependency(service);

                        } else {
                            throw new Exception("ServiceNotFound :" + field.getType().getName());
                        }
                    }
                }
            }
        }

    }


    private Object findServiceByField(Field field) throws Exception {
        Class<?> fieldType = field.getType();
        for (Object service : this.serviceList) {
            if (fieldType.isAssignableFrom(service.getClass())) {
                return service;
            }
        }
        return null;
    }

//
//
//
//    public List<IComponent> findComponentByType(Class<? extends IComponent> classType){
//        List<IComponent> result = new ArrayList<>();
//        for (IComponent obj : this.componentList){
//            if (classType.isAssignableFrom(obj.getClass())){
//                return obj;
//            }
//        }
//        return null;
//    }

}
