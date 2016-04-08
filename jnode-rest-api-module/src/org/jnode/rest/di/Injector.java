package org.jnode.rest.di;


import jnode.logger.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Performs the injection of dependencies on a class instance.<br>
 * 
 * Usage:
 * 
 * <pre>
 *   BeanTest bean = new BeanTest();
 *   Injector.inject(bean);
 * </pre>
 * 
 *  Note: before use make sure, that your subjects to be injected are scanned by the TinyDI framework.
 *  Either use the following code snippet, or <code>TinyDependencyInjectionServletContextListener</code>
 *  
 *  <pre>
 *    ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
 *    scanner.scan("com.mycompany.myapp");
 *  </pre>  
 *  
 * @author Richard Pal
 */
public class Injector {
  private static final Logger LOGGER = Logger.getLogger(Injector.class);

  private static DependencyRepository repository = DependencyRepository.getInstance();
  
  /**
   * Performs dependency injection on the passed object.
   *  
   * @param object the object which's dependencies should be injected.
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InstantiationException */
  public static <T> T inject(T object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
    long time = System.nanoTime();
    Class<? extends Object> clazz = object.getClass();
    LOGGER.l4(MessageFormat.format("starting dependency injection of class {0}", clazz.getCanonicalName()));
    
    for (Field field : getInjectedFields(clazz)) {

      String fieldName = field.getName();
      LOGGER.l4(MessageFormat.format("{0}.{1} field is annotated with @Inject",
          clazz.getCanonicalName(), fieldName));
      
      String setterMethodName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
      Method setterMethod = null;
      try {
        setterMethod = clazz.getMethod(setterMethodName, field.getType());
      } catch (Exception e) {
        throw new IllegalAccessException(MessageFormat.format("Cannot find/invoke (public) setter {0}.{1}({2})",
            clazz.getCanonicalName(), setterMethodName, field.getType().getCanonicalName()));
      }
      
      // if @Named annotation is present at the field, then we find it via bean name
      // else via the referenced class name:
      Named named = field.getAnnotation(Named.class);
      Object injectedValue = (named != null) ? repository.getBean(named.value())
                                             : repository.getBean(field.getType());

      try {
        setterMethod.invoke(object, injectedValue);
      } catch (Exception e) {
        throw new InstantiationException(MessageFormat.format(
          "Unable to call setter function [{0}] of object {1} with argument type {2}",
          setterMethod.toString(),
          object.getClass(),
          injectedValue.getClass(), e));
      }
      LOGGER.l3(MessageFormat.format("Injected {0}.{1}() successfully",
          clazz.getCanonicalName(), setterMethodName));
    }
    time = System.nanoTime() - time;
    LOGGER.l4(MessageFormat.format("finished dependency injection of class {0}. Took {1} nanosec",
        clazz.getCanonicalName(), time));
    return object;
  }
  
  @SuppressWarnings("unchecked")
  /** Returns all @Inject annotated fields of the class and its parent classes. */
  private static Collection<Field> getInjectedFields(Class clazz) {
    List<Field> fieldList = new ArrayList<Field>();
    
    for (Field field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(Inject.class)) {
        fieldList.add(field);
      }
    }
    
    Class superClass = clazz.getSuperclass();
    if (superClass != null) {
      Collection<Field> fieldsOfParent = getInjectedFields(superClass);
      fieldList.addAll(fieldsOfParent);
    }
    return fieldList;
  }
}
