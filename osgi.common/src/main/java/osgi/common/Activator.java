package osgi.common;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import osgi.common.dao.ISequenceDao;
import osgi.common.funcinterfaces.IAfterStartUpHandle;
import osgi.common.funcinterfaces.IBeforeShutDownHandle;
import osgi.common.services.CA;
import osgi.common.services.CB;
import osgi.common.services.CC;
import osgi.common.services.CD;
import osgi.common.util.context.BundleContextManager;
import osgi.common.util.context.PackageScanner;
import osgi.database.util.MysqlDB;

public class Activator implements BundleActivator {

    private static final String PACKAGE_DAO = "osgi.common.dao";

    private static BundleContext context;

    public static final String BUNDLE_NAME = "osgi.common";

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
        bundleContextManager = new BundleContextManager(BUNDLE_NAME);
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

    public static void main(String[] args) {
        try {
            PackageScanner packageScanner = new PackageScanner(Activator.class.getClassLoader(), PACKAGE_DAO, true,
                    null, null);
            Set<Class<?>> mappers = packageScanner.doScanAllClasses();
            MysqlDB.registMappers(mappers);
            ISequenceDao sequenceDao = MysqlDB.getMapper(ISequenceDao.class);
            System.out.println(sequenceDao.getNextValue("s_common"));
            bundleContextManager = new BundleContextManager(BUNDLE_NAME);
            BundleContextManager.registContext(BUNDLE_NAME, bundleContextManager);
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
