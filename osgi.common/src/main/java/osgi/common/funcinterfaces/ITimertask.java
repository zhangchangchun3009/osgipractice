
package osgi.common.funcinterfaces;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务接口，应该配置执行延迟和周期
 *
 * @author zhangchangchun
 * @since 2021年4月24日
 */
public interface ITimertask {

    /**
     * 任务内容
     */
    void execute();

    /**
     * @return 首次运行延迟
     */
    long getDelay();

    /**
     * @return 运行周期
     */
    long getPeriod();

    /**
     * @return 时间单位
     */
    TimeUnit getTimeUnit();

}
