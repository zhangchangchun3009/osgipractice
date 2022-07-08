package osgi.web;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import dictionary.services.DictionaryService;

public class Activator implements BundleActivator {

    private static BundleContext context;

    public static Map<String, Object> bundleServices = new HashMap<>();

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        ServiceReference<?> dictionaryServiceRef = bundleContext.getServiceReference(DictionaryService.class.getName());
        DictionaryService dictionaryService = (DictionaryService) bundleContext.getService(dictionaryServiceRef);
        bundleServices.put(DictionaryService.class.getName(), dictionaryService);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
