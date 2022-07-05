
package osgi.common.services;

import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.common.Activator;
import osgi.common.funcinterfaces.IAfterStartUpHandle;
import osgi.common.funcinterfaces.ITimertask;

/**
 * 定时任务启动器
 *
 * @author zhangchangchun
 * @since 2021年4月24日
 */
@Named
public class TimertaskStarter implements IAfterStartUpHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimertaskStarter.class);

    @Inject
    @Named("scheduledThreadPoolExecutor")
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void process() {
        Collection<ITimertask> tasks = Activator.bundleContextManager.getBeansOfType(ITimertask.class);
        for (ITimertask task : tasks) {
            try {
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.execute();
                        } catch (Exception e) {
                            LOGGER.error("timertask " + task.getClass().getName() + " exception:", e);
                        }
                    }
                }, task.getDelay(), task.getPeriod(), task.getTimeUnit());
            } catch (RejectedExecutionException e1) {
                LOGGER.error("task cannot bescheduled for execution");
            } catch (NullPointerException e2) {
                LOGGER.error("command is null");
            } catch (IllegalArgumentException e3) {
                LOGGER.error("period less than or equal to zero");
            }
        }
    }

    @Override
    public int getOrder() {
        return 999;
    }

}
