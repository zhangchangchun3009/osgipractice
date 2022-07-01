package osgi.database.activator;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import osgi.database.dao.ISequenceDao;
import osgi.database.util.MysqlDB;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        List<Class<?>> mappers = new ArrayList<>();
        mappers.add(ISequenceDao.class);
        MysqlDB.registMappers(mappers);
        ISequenceDao dao = MysqlDB.getMapper(ISequenceDao.class);
        System.out.println(dao.getNextValue("s_common"));
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
