package org.jnode.rest.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Scans the java packages for annotated, injectable classes, and registers those
 * in the repository.
 * <p>
 * <p>Note: the underlying repository is not thread safe.</p>
 *
 * @author Richard Pal
 */
public class ClassfileDependencyScanner {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private DependencyRepository repository = DependencyRepository.getInstance();
    private ClasspathScanner classpathScanner = new ClasspathScanner();

    @SuppressWarnings("unchecked")
    public void scan(String packageRoot, String prefix) {
        long time = System.currentTimeMillis();
        logger.info(MessageFormat.format(
                "TinyDi is going to be initialised ... parsing java package: {0}", packageRoot));

        try {
            Iterable<Class> classes = getClasses(packageRoot);

            collectNamedEntities(classes,
                    repository.getNamedBeans(),
                    repository.getSingletons(), prefix);

        } catch (Exception e) {
            throw new RuntimeException("failed to carry out DI", e);
        }
        time = System.currentTimeMillis() - time;
        logger.info(MessageFormat.format("TinyDi finished java package parsing. Found {0} managed objects (of which {1} singletons). Took {2} ms.",
                repository.getNamedBeans().keySet().size(),
                repository.getSingletons().keySet().size(),
                time));
    }

    /**
     * Collects classes which can be injected with DI
     */
    @SuppressWarnings("unchecked")
    private void collectNamedEntities(
            Iterable<Class> classes,
            Map<String, Class> namedEntities,
            Map<Class, Object> singletons, String prefix) {

        for (Class clazz : classes) {
            if (clazz.isAnnotationPresent(Named.class) || clazz.isAnnotationPresent(Singleton.class)) {
                Named named = (Named) clazz.getAnnotation(Named.class);
                String name = queryName(named, prefix);
                if (name == null || name.length() == 0) {
                    continue;
                }
                if (namedEntities.get(name) != null) {
                    throw new RuntimeException(
                            MessageFormat
                                    .format(
                                            "Named entity {0} is double defined. Found at classes {1} and {2}",
                                            name, namedEntities.get(name).getCanonicalName(), clazz
                                                    .getCanonicalName()));
                }
                namedEntities.put(name, clazz);
                logger.info(MessageFormat.format(
                        "{0} class is registered with alias >{1}<", clazz.getCanonicalName(), name));

                // register singletons:
                if (clazz.isAnnotationPresent(Singleton.class)) {
                    singletons.put(clazz, null);
                    logger.info(MessageFormat.format(
                            "{0} class (alias >{1}<) is singleton", clazz.getCanonicalName(), name));
                }

                // register the class' interfaces:
                Class[] interfaces = clazz.getInterfaces();
                Map<Class, Class> interfaceMappings = repository.getInterfaces();
                for (Class iface : interfaces) {
                    Class alreadyBound = interfaceMappings.get(iface);
                    if (alreadyBound == null) {
                        interfaceMappings.put(iface, clazz);
                    } else {
                        logger.debug(MessageFormat.format("Interface >{0}< is already bound to class >{1}<, so it won't be additionally bound to class >{2}<",
                                iface.getCanonicalName(), alreadyBound.getCanonicalName(), clazz.getCanonicalName()));
                    }
                }

            }
        }
    }

    private String queryName(Named named, String prefix) {
        if (named != null) {

            boolean hasPrefix = named.value().contains("-");

            if (named.value().startsWith(prefix)) { // с нашим префиксом пускаем
                return named.value().substring(prefix.length());
            } else if (hasPrefix) { // со всеми другим посылаем
                return null;
            }

            return named.value(); // если префикса нет - то пущаем
        }
        return null;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to
     * the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     * @see http://stackoverflow.com/questions/862106/how-to-find-annotated-methods-in-a-given-package
     */
    @SuppressWarnings("unchecked")
    private Iterable<Class> getClasses(String packageName)
            throws ClassNotFoundException, IOException {

        List<String> classNames = classpathScanner.getClassNamesFromPackage(packageName);

        List<Class> classes = new ArrayList<Class>();
        Set<String> classNameSet = new HashSet<String>();

        for (String cl : classNames) {
            // filter duplicate class+package names:
            if (!classNameSet.contains(cl)) {
                classes.add(Class.forName(cl));
                classNameSet.add(cl);
            }
        }

        return classes;
    }


}