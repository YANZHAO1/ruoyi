package com.ruoyi.kuzi.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
    public static void main(String[] args) throws BadHanyuPinyinOutputFormatCombination {

        String str = PinyinUtil.getPinYin("全国车主76万2020年");
        System.out.println(str);

        //System.out.println(getPinYin(null));


//        String[] strs = PinyinUtil.getPinYin('空');
//        for (String str : strs) {
//            System.out.println(str);
//        }

    }

    /**
     * 传入中文获取首字母 （小写）
     * 如：小超人 -> xcr
     *
     * @param str 需要转化的中文字符串
     * @return
     */
    public static String getPinYinHeadChar(String str) {
        String convert = "";
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0].charAt(0);
            } else {
                convert += word;
            }
        }
        return convert;
    }


    /**
     * 获取中文字的拼音（多音字，拼音后的数字代表第几声）
     * 如：空 -> kong1 kong4
     *
     * @param word
     * @return
     */
    public static String[] getPinYin(char word) {
        return PinyinHelper.toHanyuPinyinStringArray(word);
    }

    /**
     * 获取中文字的拼音（多音字，拼音上的符号代表第几声）
     * 如：空 -> kōng kòng
     *
     * @param word
     * @return
     */
    public static String[] getPinYinWithToneMark(char word) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        return PinyinHelper.toHanyuPinyinStringArray(word, format);
    }

    public static String getPinYin(String str){
        // 正则表达式：匹配非汉字、字母和数字的字符
        String regex = "[^\\u4E00-\\u9FA5a-zA-Z0-9]";
        // 去除特殊字符但保留汉字、字母和数字
        str = str.replaceAll(regex, "");

        String rtn = "";
        for (char c : str.toCharArray()) {
            if(Character.isDigit(c) || isEnglishLetterButNotChinese(c)){
                rtn +=c;
                continue;
            }
            if(PinyinHelper.toHanyuPinyinStringArray(c)==null){
                continue;
            }
            String py = PinyinHelper.toHanyuPinyinStringArray(c)[0];
            if(py==null){
                continue;
            }

            rtn +=py.substring(0,py.length()-1)+"_";
        }
        return removeTrailingUnderscore(rtn);
    }

    /**
     * 判断字符是否是英文字母且不是汉字
     *
     * @param c 待判断的字符
     * @return 如果是英文字母且不是汉字，返回true；否则返回false
     */
    public static boolean isEnglishLetterButNotChinese(char c) {
        // 检查是否是英文字母
        boolean isEnglishLetter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');

        // 检查是否是汉字
        boolean isChinese = c >= '\u4E00' && c <= '\u9FA5';

        // 返回是英文字母但不是汉字的结果
        return isEnglishLetter && !isChinese;
    }

    /**
     * 如果字符串的最后一个字符是'_'，则删除它
     *
     * @param str 待处理的字符串
     * @return 处理后的字符串
     */
    public static String removeTrailingUnderscore(String str) {
        // 检查字符串是否为空或长度为0
        if (str == null || str.length() == 0) {
            return str;
        }

        // 检查最后一个字符是否为'_'
        if (str.charAt(str.length() - 1) == '_') {
            // 返回去掉最后一个字符的新字符串
            return str.substring(0, str.length() - 1);
        }

        // 返回原字符串
        return str;
    }

}
