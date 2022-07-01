
package osgi.common.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义类加载器,如果某个包需要隔离其他版本运行可以用自定义加载器加载
 */
/**
 * @author zcc
 * @since 2021年11月29日
 */
public class ExtendableClassLoader extends URLClassLoader {

    private ConcurrentHashMap<String, Class<?>> loadedClassMap = new ConcurrentHashMap<>();

    public ExtendableClassLoader(URL... urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = this.findLoadedClassByThis(name);
            if (c != null) {
                return c;
            }
            try {
                c = this.findClass(name);
            } catch (Exception e1) {
            }
            if (c == null) {
                // If still not found, then invoke findClass in order
                // to find the class.
                try {
                    ClassLoader parent = this.getParent();
                    if (parent != null) {
                        c = parent.loadClass(name);
                    } else {
                    }
                    return c;
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                    throw e;
                }
            }
            loadedClassMap.putIfAbsent(name, c);
            return c;
        }
    }

    public Class<?> findLoadedClassByThis(String name) {
        return loadedClassMap.get(name);
    }

}
