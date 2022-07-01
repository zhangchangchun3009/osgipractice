package osgi.common.util.eventstream.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Sleeper {

    private static final Random RANDOM = new Random();

    /**
     * 随机睡眠一段时间，睡眠时间遵循平均值为mean，方差为stdDev的正态分布
     * @param mean
     * @param stdDev
     * @throws InterruptedException 
     */
    public static void randomSleep(double mean, double stdDev) throws InterruptedException {
        final double micros = 1000 * (mean + RANDOM.nextGaussian() * stdDev);
        try {
            TimeUnit.MICROSECONDS.sleep((long) micros);
        } catch (InterruptedException e) {
            throw e;
        }
    }

}
