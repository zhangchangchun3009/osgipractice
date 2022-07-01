
package osgi.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class ByteUtil {
    public static byte[] getBytes(short data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        return bytes;
    }

    public static byte[] getBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    public static byte[] getBytes(long data) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data >> 8) & 0xff);
        bytes[2] = (byte) ((data >> 16) & 0xff);
        bytes[3] = (byte) ((data >> 24) & 0xff);
        bytes[4] = (byte) ((data >> 32) & 0xff);
        bytes[5] = (byte) ((data >> 40) & 0xff);
        bytes[6] = (byte) ((data >> 48) & 0xff);
        bytes[7] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(String data, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        return data.getBytes(charset);
    }

    public static byte[] getBytes(String data) {
        return getBytes(data, "UTF-8");
    }

    public static short getShort(byte[] bytes) {
        return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static char getChar(byte[] bytes) {
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static int getInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16))
                | (0xff000000 & (bytes[3] << 24));
    }

    public static long getLong(byte[] bytes) {
        return (0xffL & bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16))
                | (0xff000000L & ((long) bytes[3] << 24)) | (0xff00000000L & ((long) bytes[4] << 32))
                | (0xff0000000000L & ((long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48))
                | (0xff00000000000000L & ((long) bytes[7] << 56));
    }

    public static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

    public static double getDouble(byte[] bytes) {

        long l = getLong(bytes);
        return Double.longBitsToDouble(l);
    }

    public static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.forName(charsetName));
    }

    public static String getString(byte[] bytes) {
        return getString(bytes, "UTF-8");
    }

    /**
     * short转成十六位二进制并高位补零
     * 
     * @param num unsigned short
     * @return
     */
    public static String toFullBinaryString(int num) {
        String binary = Integer.toBinaryString(num);
        char[] cha = new char[Short.SIZE];
        for (int i = 0; i < cha.length; i++) {
            if (i < binary.length()) {
                cha[Short.SIZE - 1 - i] = binary.charAt(binary.length() - 1 - i);
            } else {
                cha[Short.SIZE - 1 - i] = '0';
            }
        }
        return new String(cha);
    }

    /**
     * 获取字符在指定的中文双字节编码的字符集下字节的十进制无符号int值表示.
     *
     * @param cha 字符
     * @param charset 字符集 典型如GBK,GB2312,GB18030
     * @return the int val of char by charset
     * @throws UnsupportedEncodingException 
     */
    public static int getIntValOfCharByCharset(char cha, String charset) throws UnsupportedEncodingException {
        byte[] byteA = String.valueOf(cha).getBytes(charset);
        if (byteA.length == 1) {
            return byteA[0];
        }
        return ((byteA[0] & 0x0FF) << 8) | (byteA[1] & 0x0FF);
    }

    /**
     * 将两个单字节编码的字符转换成低位放第一个字符、高位放第二个字符的16位bit数组的整型表示
     *
     * @param lowChar the low char
     * @param highChar the high char
     * @param charset the charset
     * @return the int val of 2 chars by charset
     */
    public static int getIntValOf2CharsByCharset(char lowChar, char highChar, String charset)
            throws UnsupportedEncodingException {
        int intLow8 = getIntValOfCharByCharset(lowChar, charset);
        int intHigh8 = getIntValOfCharByCharset(highChar, charset);
        return (intLow8 << 8) | intHigh8;
    }

    public static String normalizeWord(int unsignShort) {
        int normal = ((unsignShort & 0xFF) << 8) | (unsignShort >>> 8);
        return toFullBinaryString(normal);
    }

    public static byte[] shortArr2ByteArr(short[] in) {
        byte[] out = new byte[in.length * 2];
        for (int i = 0; i < in.length; i++) {
            out[2 * i] = (byte) ((in[i] & 0x0ff00) >>> 8);
            out[2 * i + 1] = (byte) (in[i] & (short) 0x00ff);
        }
        return out;
    }

    public static short[] byteArr2ShortArr(byte[] in) {
        short[] out = new short[in.length / 2];
        for (int i = 0; i < in.length / 2; i++) {
            out[i] = (short) (((in[2 * i] & 0xff) << 8) | (in[2 * i + 1] & 0xff));
        }
        return out;
    }

}
