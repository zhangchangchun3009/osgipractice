package osgi.common.util;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

public class EhCacheUtil {
    private static final CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().build();

    static {
        init();
    }

    public static void init() {
        manager.init();
    }

    public static CacheManager getManager() {
        return manager;
    }

}
