
package osgi.common;

/**
 * after bundle start up
 *
 * @author zhangchangchun
 * @since 2021年4月21日
 */
public interface IAfterStartUpHandle {

    /**
     * 执行动作
     */
    void process();

    /**
     * 执行顺序，0最先执行
     * 
     * @return int
     */
    int getOrder();

}
