package org.jnode.rest.di;


import jnode.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans the classpath (both the filesystem and the jars) for classes inside a certain java package.
 *
 * @author Richard Pal
 * @date 10/23/11
 */
public class ClasspathScanner {

  private static final Logger LOGGER = Logger.getLogger(ClasspathScanner.class);

  public List<String> getClassNamesFromPackage(String packagePrefix) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    ArrayList<String> classNameList = new ArrayList<String>();

    // replace "." with "/"
    String packageDir = packagePrefix.replace(".", "/") + "/";
    //URL packageURL = classLoader.getResource(packageDir);

    // fetch all JARs and filesystem urls which contain the package prefix:
    Enumeration<URL> resources = classLoader.getResources(packageDir);

    while(resources.hasMoreElements()) {
      URL url = resources.nextElement();
      LOGGER.l4(String.format("url:%s", url));

      if(url.getProtocol().equals("jar")) {
        // extract from JAR
        getClassNamesFromJar(url, classNameList, packageDir);
      } else {
        // extract from filesystem:
        File folder = new File(url.getFile());
        getClassNamesFromFileSystem(classLoader, folder, classNameList, packagePrefix);
      }
    }
    return classNameList;
  }

  private void getClassNamesFromJar(URL packageUrl, List<String> classList, String packagePrefix) throws IOException {
    // extract jar file name
    String jarFileName = packageUrl.getFile();
    jarFileName = jarFileName.substring(5,jarFileName.indexOf("!"));
    LOGGER.l4(String.format("JAR file name: %s", jarFileName));

    JarFile jf = new JarFile(jarFileName);
    Enumeration<JarEntry> jarEntries = jf.entries();

    while (jarEntries.hasMoreElements()){
      String entryName = jarEntries.nextElement().getName();
      // logger.debug("jar entry: {}", entryName);

      if (entryName.startsWith(packagePrefix)
        && entryName.length()> packagePrefix.length()+5
        && entryName.endsWith(".class")) {
        //entryName = entryName.substring(packagePrefix.length(),entryName.lastIndexOf('.'));
        entryName = entryName.substring(0,entryName.lastIndexOf('.')).replace("/", ".");
        classList.add(entryName);
        LOGGER.l4(String.format("jar entry: %s added to list", entryName));
      }
    }
  }

  private void getClassNamesFromFileSystem(ClassLoader classLoader,
                                           File folder,
                                           List<String> classList,
                                           String packageName) {

    File[] folderContent = folder.listFiles();

    for (File actual: folderContent){
      String entryName = actual.getName();

      // is this a package directory?
      if (actual.isDirectory() && entryName.indexOf('.') == -1) {
        String childPackage = packageName + "." + entryName;
        LOGGER.l4(String.format("drilling down to childpackage: %s", childPackage));

        getClassNamesFromFileSystem(classLoader, actual, classList, childPackage);

      } else if (entryName.endsWith(".class")) {
        // add the simple class:
        entryName = packageName + "." + entryName.substring(0, entryName.lastIndexOf('.'));
        LOGGER.l4(String.format("adding class to list: %s", entryName));
        classList.add(entryName);
      }
    }
  }
}
