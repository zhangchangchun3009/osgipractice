package osgi.common.services.test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import osgi.common.Activator;
import osgi.common.dao.ISequenceDao;
import osgi.common.funcinterfaces.IAfterStartUpHandle;
import osgi.common.util.context.BundleContextManager;
import osgi.common.util.context.PackageScanner;
import osgi.database.util.MysqlDB;

public class TestContext {

    public static void main(String[] args) {
        try {
            PackageScanner packageScanner = new PackageScanner(Activator.class.getClassLoader(), Activator.PACKAGE_DAO,
                    true, null, null);
            Set<Class<?>> mappers = packageScanner.doScanAllClasses();
            MysqlDB.registMappers(mappers);
            ISequenceDao sequenceDao = MysqlDB.getMapper(ISequenceDao.class);
            System.out.println(sequenceDao.getNextValue("s_common"));
            ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(8,
                    new ThreadPoolExecutor.AbortPolicy());
            Map<String, Object> requiredOuterBeans = new HashMap<>();
            requiredOuterBeans.put("scheduledThreadPoolExecutor", scheduledExecutorService);
            mappers.forEach(mapperInterface -> {
                requiredOuterBeans.put(mapperInterface.getName(), MysqlDB.getMapper(mapperInterface));
            });
            BundleContextManager bundleContextManager = new BundleContextManager(Activator.BUNDLE_NAME,
                    requiredOuterBeans);
            BundleContextManager.registContext(Activator.BUNDLE_NAME, bundleContextManager);
            Collection<IAfterStartUpHandle> beans = bundleContextManager.getBeansOfType(IAfterStartUpHandle.class);
            beans.forEach(bean -> {
                System.out.println(bean.getClass().getName());
            });
            CA ca = bundleContextManager.getBeanOfId("CA");
            CB cb = bundleContextManager.getBeanOfId("cB");
            CC cc = bundleContextManager.getBeanOfId("cC");
            CD cd = bundleContextManager.getBeanOfId("cD");
            ca.print();
            cb.print();
            cc.print();
            cd.print();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
