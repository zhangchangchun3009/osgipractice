package dictionary;

import java.util.Hashtable;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import dictionary.impl.DictionaryImpl;
import dictionary.impl.DictionaryServiceImpl;
import dictionary.impl.ServiceComponent;
import dictionary.services.Dictionary;
import dictionary.services.DictionaryService;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        DictionaryService dictionaryService = new DictionaryServiceImpl();
        Dictionary dictionary = new DictionaryImpl();
        dictionaryService.registerDictionary(dictionary);
        context.registerService(DictionaryService.class.getName(), dictionaryService, new Hashtable<String, Object>());
        ServiceComponent serviceComponent = new ServiceComponent();
        serviceComponent.setDictionary(dictionaryService);
        context.registerService(CommandProvider.class.getName(), serviceComponent, new Hashtable<String, Object>());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
