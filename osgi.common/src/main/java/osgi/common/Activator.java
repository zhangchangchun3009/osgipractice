package osgi.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import osgi.common.funcinterfaces.IAfterStartUpHandle;
import osgi.common.funcinterfaces.IBeforeShutDownHandle;
import osgi.common.util.context.BundleContextManager;
import osgi.common.util.context.PackageScanner;
import osgi.database.util.MysqlDB;

public class Activator implements BundleActivator {

    public static final String PACKAGE_DAO = "osgi.common.dao";

    public static final String BUNDLE_NAME = "osgi.common";

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    public static BundleContextManager bundleContextManager;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        PackageScanner packageScanner = new PackageScanner(this.getClass().getClassLoader(), PACKAGE_DAO, true, null,
                null);
        Set<Class<?>> mappers = packageScanner.doScanAllClasses();
        MysqlDB.registMappers(mappers);
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(8,
                new ThreadPoolExecutor.AbortPolicy());
        Map<String, Object> requiredOuterBeans = new HashMap<>();
        requiredOuterBeans.put("scheduledThreadPoolExecutor", scheduledExecutorService);
        mappers.forEach(mapperInterface -> {
            requiredOuterBeans.put(mapperInterface.getName(), MysqlDB.getMapper(mapperInterface));
        });
        bundleContextManager = new BundleContextManager(BUNDLE_NAME, requiredOuterBeans);
        BundleContextManager.registContext(BUNDLE_NAME, bundleContextManager);
        Collection<IAfterStartUpHandle> beans = bundleContextManager.getBeansOfType(IAfterStartUpHandle.class);
        beans.stream().sorted((o1, o2) -> {
            return o1.getOrder() - o2.getOrder();
        }).forEach(item -> {
            item.process();
        });
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Collection<IBeforeShutDownHandle> beans = bundleContextManager.getBeansOfType(IBeforeShutDownHandle.class);
        beans.stream().sorted((o1, o2) -> {
            return o1.getOrder() - o2.getOrder();
        }).forEach(item -> {
            item.process();
        });
        Activator.context = null;
    }

}
