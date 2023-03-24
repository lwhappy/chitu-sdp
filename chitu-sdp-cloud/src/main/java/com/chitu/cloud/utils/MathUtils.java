package com.chitu.cloud.utils;
import java.math.BigDecimal;

public class MathUtils {
    private static final int DEF_DIV_SCALE = 6;

    private MathUtils() {

    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    public static double add(Double v1, Double v2) {
        return add(v1,v2,DEF_DIV_SCALE);
    }

    public static  BigDecimal add(BigDecimal v1,BigDecimal v2){
        return add(v1,v2,DEF_DIV_SCALE);
    }

    public static double add(Double v1, Double v2, int scale ){
        if(v1 == null){
            v1 = 0d;
        }
        if(v2 == null){
            v2 = 0d;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return add(b1,b2,scale).doubleValue();
    }

    public  static  BigDecimal  add(BigDecimal v1,BigDecimal v2, int scale){
        if(v1 == null){
            v1 = new BigDecimal(0d);
        }
        if (v2 == null){
            v2 = new BigDecimal(0d);
        }
        BigDecimal total  = v1.add(v2);
        return round(total,scale);
    }


    /**
     * 提供精确的减法运算。
     *
     * @param v1
     *            被减数
     * @param v2
     *            减数
     * @return 两个参数的差
     */
    public static double sub(Double v1, Double v2, int scale) {
        if(v1 == null){
            v1 = 0d;
        }
        if(v2 == null){
            v2 = 0d;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return sub(b1,b2,scale).doubleValue();
    }

    public static double sub(Double v1, Double v2) {
       return sub(v1,v2,DEF_DIV_SCALE);
    }

    public static  BigDecimal sub(BigDecimal v1,BigDecimal v2,int scale){
        if(v1 == null){
            v1 = new BigDecimal(0d);
        }
        if (v2 == null){
            v2 = new BigDecimal(0d);
        }
        return round(v1.subtract(v2),scale);
    }
    public static  BigDecimal sub(BigDecimal v1,BigDecimal v2){
        return sub(v1,v2,DEF_DIV_SCALE);

    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1
     *            被乘数
     * @param v2
     *            乘数
     * @return 两个参数的积
     */
    public static double mul(Double v1, Double v2, int scale) {
        if(v1 == null){
            v1 = 0d;
        }
        if(v2 == null){
            v2 = 0d;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return  mul(b1,b2,scale).doubleValue();
    }

    public static double mul(Double v1, Double v2) {
        return mul(v1,v2,DEF_DIV_SCALE);
    }

    public  static  BigDecimal mul(BigDecimal v1,BigDecimal v2,int scale){
        if(v1 == null){
            v1 = new BigDecimal(0d);
        }
        if (v2 == null){
            v2 = new BigDecimal(0d);
        }
       return  round(v1.multiply(v2),scale) ;
    }

    public static  BigDecimal mul(BigDecimal v1,BigDecimal v2){
        return mul(v1,v2,DEF_DIV_SCALE);

    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1
     *            被除数
     * @param v2
     *            除数
     * @return 两个参数的商
     */
    public static double div(Double v1, Double v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
     *
     * @param v1
     *            被除数
     * @param v2
     *            除数
     * @param scale
     *            表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */

    public static double div(Double v1, Double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return div(b1,b2,scale).doubleValue();
    }

    public static  BigDecimal div(BigDecimal v1,BigDecimal v2,int scale){
        if(v1 == null){
            v1 = new BigDecimal(0d);
        }
        if (v2 == null){
            v2 = new BigDecimal(1d);
        }
        return v1.divide(v2,scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v
     *            需要四舍五入的数字
     * @param scale
     *            小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static BigDecimal round(BigDecimal v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal one = new BigDecimal("1");
        return v.divide(one, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static void main(String arg[]){
        System.out.println(MathUtils.add(1.00002d,1.00013d,4));
    }

}
