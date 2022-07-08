package osgi.common;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import osgi.common.funcinterfaces.IAfterStartUpHandle;
import osgi.common.funcinterfaces.IBeforeShutDownHandle;
import osgi.common.services.interfaces.IAsyncTaskEventResultService;
import osgi.common.services.interfaces.ICommonService;
import osgi.common.services.interfaces.IExcelErrorService;
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
        Set<Class<?>> mappers = registDaoMapper();
        Map<String, Object> requiredOuterBeans = registOuterBean(mappers);
        buildBundleContext(requiredOuterBeans);
        publishService(bundleContext);
        startupCustomizedProcessesAtLast();
    }

    private void startupCustomizedProcessesAtLast() {
        Collection<IAfterStartUpHandle> beans = bundleContextManager.getBeansOfType(IAfterStartUpHandle.class);
        beans.stream().sorted((o1, o2) -> {
            return o1.getOrder() - o2.getOrder();
        }).forEach(item -> {
            item.process();
        });
    }

    private void buildBundleContext(Map<String, Object> requiredOuterBeans) {
        bundleContextManager = new BundleContextManager(BUNDLE_NAME, requiredOuterBeans);
        BundleContextManager.registContext(BUNDLE_NAME, bundleContextManager);
    }

    /**
     * define any object that you want it to be a bean here.
     * dao instances need to be registered by default.
     * @param mappers
     * @return
     */
    private Map<String, Object> registOuterBean(Set<Class<?>> mappers) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(8,
                new ThreadPoolExecutor.AbortPolicy());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 300, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(3000), new ThreadPoolExecutor.AbortPolicy());
        Map<String, Object> requiredOuterBeans = new HashMap<>();
        requiredOuterBeans.put("scheduledThreadPoolExecutor", scheduledExecutorService);
        requiredOuterBeans.put("threadPoolExecutor", threadPoolExecutor);
        mappers.forEach(mapperInterface -> {
            requiredOuterBeans.put(mapperInterface.getName(), MysqlDB.getMapper(mapperInterface));
        });
        return requiredOuterBeans;
    }

    /**
     * choose the package name of dao mapper interfaces and register them to mybatis. 
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Set<Class<?>> registDaoMapper() throws IOException, ClassNotFoundException {
        PackageScanner packageScanner = new PackageScanner(this.getClass().getClassLoader(), PACKAGE_DAO, true, null,
                null);
        Set<Class<?>> mappers = packageScanner.doScanAllClasses();
        MysqlDB.registMappers(mappers);
        return mappers;
    }

    // maybe i should use package scanner and publish services automatically,but sometimes programmers want to decide this by self.
    private void publishService(BundleContext bundleContext) {
        IAsyncTaskEventResultService asyncTaskEventResultService = bundleContextManager
                .getBeanOfId("asyncTaskEventResultService");
        ICommonService commonService = bundleContextManager.getBeanOfId("commonService");
        IExcelErrorService excelErrorService = bundleContextManager.getBeanOfId("excelErrorService");
        bundleContext.registerService(IAsyncTaskEventResultService.class, asyncTaskEventResultService, null);
        bundleContext.registerService(ICommonService.class, commonService, null);
        bundleContext.registerService(IExcelErrorService.class, excelErrorService, null);
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
