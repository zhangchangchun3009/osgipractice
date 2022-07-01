
package osgi.common.util;

import java.util.Random;

/**
 * The Class RandomUtil.
 * 
 * @author zhangchangchun
 *
 */
public class RandomUtil {

    private static final char numbers[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /**
     * Gets the random num format code.
     *
     * @param number the number
     * @return the random num format code
     */
    public static String getRandomNumFormatCode(int number) {
        char[] res = new char[number];
        Random random = new Random();
        for (int i = 0; i < number; i++) {
            int next = random.nextInt(10000);
            res[i] = numbers[next % 10];
        }
        return new String(res);
    }

    /**
     * Gets the random asc II code.
     *
     * @param number the number
     * @return the random asc II code
     */
    public static String getRandomAscIICode(int number) {
        char[] res = new char[number];
        int[] code = new int[3];
        Random random = new Random();
        for (int i = 0; i < number; i++) {
            int num = random.nextInt(10) + 48;
            int uppercase = random.nextInt(26) + 65;
            int lowercase = random.nextInt(26) + 97;
            code[0] = num;
            code[1] = uppercase;
            code[2] = lowercase;
            res[i] = (char) code[random.nextInt(3)];
        }
        return new String(res);
    }

}
