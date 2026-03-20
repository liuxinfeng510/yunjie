package com.yf.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具类 - 基于 pinyin4j
 */
public class PinyinUtil {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 获取全拼: "阿莫西林胶囊" -> "amoxilinjiaonang"
     */
    public static String toPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            try {
                String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if (arr != null && arr.length > 0) {
                    sb.append(arr[0]);
                } else if (Character.isLetterOrDigit(c)) {
                    sb.append(Character.toLowerCase(c));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                if (Character.isLetterOrDigit(c)) {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取拼音首字母: "阿莫西林胶囊" -> "amxljn"
     */
    public static String toPinyinShort(String chinese) {
        if (chinese == null || chinese.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            try {
                String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if (arr != null && arr.length > 0) {
                    sb.append(arr[0].charAt(0));
                } else if (Character.isLetterOrDigit(c)) {
                    sb.append(Character.toLowerCase(c));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                if (Character.isLetterOrDigit(c)) {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }
}
