package org.jnode.rest.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Repository of the Dependency Injection framework.
 * 
 * <p>Stores the classes of the managed beans, and the singleton bean instances.
 * Creates instances of the managed objects.</p>
 * 
 * <p>Note: this class is not thread safe.<br> 
 * (designed to be used with singleton patterns in servlet environments)</p>
 * 
 * @author Richard Pal
 */
public class DependencyRepository {

  protected Logger logger = LoggerFactory.getLogger(getClass());
  
  private static DependencyRepository repositoryInstace = new DependencyRepository();
  
  /** Binds the Bean name (either short class name or custom name defined with
   * @Named to the related Class. */
  @SuppressWarnings("unchecked")
  private Map<String, Class> namedBeans = new HashMap<String, Class>();
  
  /** Binds the Class to its singleton instance */
  @SuppressWarnings("unchecked")
  private Map<Class, Object> singletons = new HashMap<Class, Object>();
  
  /** Binds the interface to its implementor */
  @SuppressWarnings("unchecked")
  private Map<Class, Class> interfaces = new HashMap<Class, Class>();
  
  @SuppressWarnings("unchecked")
  private Set<Class> objectsCurrentlyInitializing = new HashSet<Class>();
  
  public static DependencyRepository getInstance() {
    return repositoryInstace;
  }

  @SuppressWarnings("unchecked")
  Map<String, Class> getNamedBeans() {
    return namedBeans;
  }

  @SuppressWarnings("unchecked")
  Map<Class, Object> getSingletons() {
    return singletons;
  }

  @SuppressWarnings("unchecked")
  public Object getBean(String beanName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Class clazz = namedBeans.get(beanName);
    if (clazz == null) throw new InstantiationException(
        MessageFormat.format("The bean >{0}< is an unknown alias. Not a class name or defined in @Named", beanName));
    return getBean(clazz);
  }
  
    
  @SuppressWarnings("unchecked")
  public <T> T getBean(Class clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    
    T bean = null;
    
    if (singletons.containsKey(clazz)) {
      Object singletonObject = singletons.get(clazz);
      if (singletonObject == null) {
        singletonObject = instantiateManagedObject(clazz);
        singletons.put(clazz, singletonObject);
      }
      bean = (T) singletonObject;
    } else {
        bean = (T) instantiateManagedObject(clazz);
    }
    
    return bean;
  }
  
  @SuppressWarnings("unchecked")
  private <T> T instantiateManagedObject(Class<T> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    
    logger.info(MessageFormat.format("Instantating class {0}", clazz.getCanonicalName()));
    
    // check if the class is an interface (or abstract.)
    // look for an appropriate implementation class then
    // note: Interfaces with generics are blended together :(
    T newInstance = null;
    if (clazz.isInterface()) {
      Class clazzImpl = interfaces.get(clazz);
      if (clazzImpl == null) {
        throw new InstantiationException(MessageFormat.format("No bean is known to implement the {0} interface",
            clazz.getCanonicalName()));
      }
      logger.debug(MessageFormat.format("Injecting class {0} in place of the {1} interface",
          clazzImpl.getCanonicalName(), clazz.getCanonicalName()));
      clazz = clazzImpl;
    } 
    newInstance = clazz.newInstance();
       
    if (objectsCurrentlyInitializing.contains(clazz)) {
      throw new InstantiationException(
        MessageFormat.format("Circular reference has been detected during the instantiation of class {0}",
        clazz.getCanonicalName()));
    }
    try {
      objectsCurrentlyInitializing.add(clazz);
      logger.debug("starting a recursive Dep.Injection");
      Injector.inject(newInstance);
    } finally {
      objectsCurrentlyInitializing.remove(clazz);
    }
    return newInstance;
  }
  
  /** Releases all references to beans. */
  public void release() {
    namedBeans.clear();
    singletons.clear();
    interfaces.clear();
  }

  @SuppressWarnings("unchecked")
  public Map<Class, Class> getInterfaces() {
    return interfaces;
  }
}
