package com.chitu.bigdata.sdp.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.util.StrUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author 587694
 *
 */
public class StrUtils {
    public static final String SPACE = " ";
    public static final String TAB = "	";
    public static final String DOT = ".";
    public static final String DOUBLE_DOT = "..";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String EMPTY = "";
    public static final String CR = "\r";
    public static final String LF = "\n";
    public static final String DLF = ",\n";
    public static final String CRLF = "\r\n";
    public static final String UNDERLINE = "_";
    public static final String COMMA = ",";
    public static final String DELIM_START = "{";
    public static final String DELIM_END = "}";
    public static final String BRACKET_START = "[";
    public static final String BRACKET_END = "]";
    public static final String COLON = ":";
    public static final String SEMICOLON = ";";

    public static final String HTML_NBSP = "&nbsp;";
    public static final String HTML_AMP = "&amp";
    public static final String HTML_QUOTE = "&quot;";
    public static final String HTML_LT = "&lt;";
    public static final String HTML_GT = "&gt;";

    public static final String EMPTY_JSON = "{}";
    //apostrophe
    public static final String APOSTROPHE = "'";
    //backticks 反单引号
    public static final String BACKTICKS  = "`";
    //equal sign
    public static final String EQUAL_SIGN = "=";
    //Parentheses
    public static final String LP = "(";
    public static final String RP = ")";
    //semicolon
    public static final String RPS = ");";
   //Placeholder  :sql占位符
   public static final String SQL_PLACEHOLDER = "\\$\\{(.*)\\}";

   public static final String SQL_REPLACE = "\\\\_";


   //使用进行mysql搜索是替换通配符"_"
    public static String changeWildcard(String str){
        String strRep;
        if (Objects.nonNull(str) && str.contains(UNDERLINE)) {
            strRep=str.replaceAll(UNDERLINE,SQL_REPLACE);
        }else{
            strRep = str;
        }
        return strRep;
    }

    /**
     * 给字符串带上反单引号
     * @param str
     * @return
     */
    public static String strWithBackticks(String str){
        if(StrUtil.isBlank(str)){
            return str;
        }
        return  BACKTICKS + str + BACKTICKS;
    }

    /**
     * 去掉字符串反单引号
     * @param str
     * @return
     */
    public static String strWithoutBackticks(String str){
        if(StrUtil.isBlank(str)){
            return str;
        }
        return  str.replace(BACKTICKS,"");
    }

    /**
     * a=1&b=2... 格式参数转换成Map
     * @param params
     * @return
     */
    public static Map<String,String> getParamMap(String params){
        Map<String,String>  paramMap = Maps.newHashMap();
        if(cn.hutool.core.util.StrUtil.isBlank(params)){
            return paramMap;
        }
        List<String> keyVals = Splitter.on("&").omitEmptyStrings().trimResults().splitToList(params);
        if(CollectionUtil.isNotEmpty(keyVals)){
            for (String keyVal : keyVals) {
                List<String> kv = Splitter.on("=").trimResults().splitToList(keyVal);
                if(CollectionUtil.isNotEmpty(kv) && 2 ==  kv.size()){
                    paramMap.put(kv.get(0),kv.get(1));
                }
            }
        }
        return paramMap;
    }


    private static final Pattern BASE64_PATTERN = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");

    /**
     * {@link  com.chitu.bigdata.sdp.service.FileService} # isBase64(String)
     * 从上面的方法中抽取base64字符串公共判断方法
     * @param str
     * @return
     */
    public static boolean isBase64(String str) {
        if(Objects.isNull(str)){
            return false;
        }
        return BASE64_PATTERN.matcher(str).matches();
    }

    public static String numToW(Long num){
        if (Objects.isNull(num)) {
            return "0";
        }
        long w = num / 10000;
        long y = num % 10000;

        return w == 0 && y == 0 ? "0" : (w == 0 ? "" : w + "万" ) + ( y == 0 ? "" : y);
    }

    /**
     * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
     * @param openToken
     * @param closeToken
     * @param text
     * @param args
     * @return
     */
    public static String parse(String openToken, String closeToken, String text, Object... args) {
        if (args == null || args.length <= 0) {
            return text;
        }
        int argsIndex = 0;
        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);
        if (start == -1) {
            return text;
        }
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    ///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////
                    String value = (argsIndex <= args.length - 1) ?
                            (args[argsIndex] == null ? "" : args[argsIndex].toString()) : expression.toString();
                    builder.append(value);
                    offset = end + closeToken.length();
                    argsIndex++;
                    ////////////////////////////////////////////////////////////////////////////////////////////////
                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
    public static String parse0(String text, Object... args) {
        return parse("${", "}", text, args);
    }
    public static String parse1(String text, Object... args) {
        return parse("{", "}", text, args);
    }



}
