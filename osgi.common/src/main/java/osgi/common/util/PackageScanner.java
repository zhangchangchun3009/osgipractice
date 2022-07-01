package osgi.common.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 双鬼带单
 * @printBy zcc
 * @Date unknown
 * @see <a href="https://blog.csdn.net/zyndev/article/details/107259570">blog</a>
 */
public class PackageScanner {

    private final String basePackage;

    private final boolean recursive;

    private final Predicate<String> packagePredicate;

    private final Predicate<Class<?>> classPredicate;

    private final ClassLoader classLoader;

    public PackageScanner(ClassLoader classLoader, String basePackage, boolean recursive,
            Predicate<String> packagePredicate, Predicate<Class<?>> classPredicate) {
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        this.basePackage = basePackage;
        this.recursive = recursive;
        this.packagePredicate = packagePredicate;
        this.classPredicate = classPredicate;
    }

    public Set<Class<?>> doScanAllClasses() throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageName = basePackage;
        if (packageName.endsWith(".")) {
            packageName.substring(0, packageName.lastIndexOf('.'));
        }
        String basePackageFilePath = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(basePackageFilePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                doScanPackageClassesByFile(classes, packageName, filePath);
            } else if ("jar".equals(protocol)) {
                doScanPackageClassesByJar(classes, packageName, resource);
            }
        }
        return classes;
    }

    private void doScanPackageClassesByFile(Set<Class<?>> classes, String packageName, String packagePath)
            throws ClassNotFoundException {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    if (!recursive) {
                        return false;
                    }
                    if (packagePredicate != null) {
                        return packagePredicate.test(packageName + "." + fileName);
                    }
                    return true;
                }
                return fileName.endsWith(".class");
            }
        });
        if (dirFiles == null) {
            return;
        }
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes, packageName + "." + file.getName(), file.getAbsolutePath());
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                Class<?> loadClass = classLoader.loadClass(packageName + "." + className);
                if (classPredicate == null || classPredicate.test(loadClass)) {
                    classes.add(loadClass);
                }
            }
        }
    }

    private void doScanPackageClassesByJar(Set<Class<?>> classes, String basePackage, URL resourceUrl)
            throws IOException, ClassNotFoundException {
        String packageName = basePackage;
        String basePackageFilePath = packageName.replace('.', '/');
        JarFile jarFile = ((JarURLConnection) resourceUrl.openConnection()).getJarFile();
        Enumeration<JarEntry> jarEntryEnum = jarFile.entries();
        while (jarEntryEnum.hasMoreElements()) {
            JarEntry entry = jarEntryEnum.nextElement();
            String name = entry.getName();
            if (!name.startsWith(basePackageFilePath) || entry.isDirectory()) {
                continue;
            }
            if (!recursive && name.lastIndexOf('/') != basePackageFilePath.length()) {
                continue;
            }
            if (packagePredicate != null) {
                String jarPackageName = name.substring(0, name.lastIndexOf('/')).replace('/', '.');
                if (!packagePredicate.test(jarPackageName)) {
                    continue;
                }
            }
            String className = name.replace('/', '.');
            className = className.substring(0, className.length() - 6);
            Class<?> loadClass = classLoader.loadClass(className);
            if (classPredicate == null || classPredicate.test(loadClass)) {
                classes.add(loadClass);
            }
        }
    }

    public static void main(String[] args) {
        PackageScanner ps = new PackageScanner(null, "osgi.common.util", true, null, null);
        try {
            Set<Class<?>> set = ps.doScanAllClasses();
            for (Iterator<Class<?>> iterator = set.iterator(); iterator.hasNext();) {
                Class<?> class1 = iterator.next();
                System.out.println(class1.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
