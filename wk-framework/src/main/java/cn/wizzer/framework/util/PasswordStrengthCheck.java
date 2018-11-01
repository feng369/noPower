package cn.wizzer.framework.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class PasswordStrengthCheck {

    public static void main(String[] args) {
//        String pwd = "12345678";
        String pwd = "24680";
        String []dic = new String[]{"12345678","kdp12345678"};
        int level = showPassstrength(dic,pwd,8,20);
        System.out.println(level);
    }

    public static int showPassstrength(String[]dictionary,String pwd,int minLen, int maxLen){
        pwd = pwd.toLowerCase();
        int level = 1;
        /** ****************加分因子***************** */
        if (containsAToZ(pwd)) {
            level++;
        }
        if (containsNumber(pwd)) {
            level++;
        }
        if (contains_(pwd)) {
            level++;
        }
        if ((containsAToZ(pwd) && containsNumber(pwd))
                || (containsAToZ(pwd) && contains_(pwd))
                || (containsNumber(pwd) && contains_(pwd))) {
            level++;
        }
        if (containsAToZ(pwd) && containsNumber(pwd) && contains_(pwd)) {
            level++;
        }
        /** ****************减分因子***************** */
        // 纯数字的密码不能是一个等差数列数列
//        if (/^[0-9]+$/.test(pwd)
        if(pwd.matches("^[0-9]+$")
                && (isANumberSequence(pwd) || isANumberSequence(reverse(pwd)))) {
            level--;
        }
        // 不能由连续的字母组成，例如：abcdefg
        if ("abcdefghijklmnopqrstuvwxyz".indexOf(pwd) > 0||"abcdefghijklmnopqrstuvwxyz".indexOf(reverse(pwd)) > 0) {
            level--;
        }
        // 纯字母组成的密码不能是键盘上的相邻键位字母组合，例如：qwertyu
        if ("qwertyuiop".indexOf(pwd) > 0 || "asdfghjkl".indexOf(pwd) > 0
                || "zxcvbnm".indexOf(pwd) > 0) {
            level--;
        }
        // 不能是2段短字符的重复，例如：567567
        if (pwd.length() % 2 == 0) {
            String part1 = pwd.substring(0, pwd.length() / 2);
            String part2 = pwd.substring(pwd.length() / 2);
            if (part1.equals(part2))
                level--;
        }
        // 不能是3段短字符的重复，例如：121212
        if (pwd.length() % 3 == 0) {
            String part1 = pwd.substring(0, pwd.length() / 3);
            String part2 = pwd.substring(pwd.length() / 3, pwd.length() / 3 * 2);
            String part3 = pwd.substring(pwd.length() / 3 * 2);
            if (part1.equals(part2)&& part2.equals(part3))
                level--;
        }
        // 不能是一个日期，例如：19870723
//        if (/^[0-9]+$/.test(pwd)) {
        if(pwd.matches("^[0-9]+$")){
            if (pwd.length() == 8) {
                try {
                    int year = Integer.parseInt(pwd.substring(0, 4));
                    int month = Integer.parseInt(pwd.substring(4, 6));
                    int day = Integer.parseInt(pwd.substring(5, 7));
                    if (year >= 1000 && year < 2100 && month >= 1 && month <= 12
                            && day >= 1 && day <= 31) {
                        level--;
                    }
                } catch (Exception e) {

                }
            }
        }
        //        if (pwd.length() < (minLen + maxLen) / 2 && pwd.length() >= minLen) {
//        level--;
        if(pwd.length()<minLen){
            level=0;
        }
        String[] pwdArray = pwd.split("");
        // 全部由同一个字符组成的直接判为弱
        boolean allEquals = true;
        String element = pwdArray[0];
        for ( int i = 1; i < pwdArray.length; i++) {
            if (!pwdArray[i].equals(element)) {
                allEquals = false;
                break;
            }
            element = pwdArray[i];
        }
        if (allEquals)
            level = 0;
        // 不能位于内置字典内
        for ( int i = 0; i < dictionary.length; i++) {
            if (pwd.equals(dictionary[i]) || dictionary[i].indexOf(pwd) >= 0) {
//                level--;
                level = 0;
                break;
            }
        }

        if (level < 0)
            level = 0;
        if (level > 5)
            level = 5;
//        var levelNames = [ "很弱", "较弱", "一般", "较好", "良好", "优秀" ];
        return level;
    }

    private static boolean containsAToZ(String str){
        String[] aToZ = new String[]{ "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k","l", "m", "n"
                , "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        boolean r = false;
        for ( int i = 0; i < aToZ.length; i++) {
            if (str.indexOf(aToZ[i]) >= 0) {
                r = true;
                 break;
             }
        }
        return r;
    }

    private static boolean containsNumber (String str) {
        String[] numbers = new String[]{ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        boolean r = false;
        for ( int i = 0; i < numbers.length; i++) {
            if (str.indexOf(numbers[i]) >= 0) {
                r = true;
                break;
            }
        }
        return r;
    }

    private static boolean contains_(String str) {
        return str.indexOf("_") >= 0 ? true : false;
    }

    private static boolean  isANumberSequence(String str) {
        try {
            String[] array = str.split("");
            boolean eq = true;
            int num = Integer.parseInt(array[1]);
            int diff = num - Integer.parseInt(array[0]);
            for (int i = 2; i < array.length; i++) {
                if (Integer.parseInt(array[i]) != num + diff) {
                    eq = false;
                    break;
                }
                num = Integer.parseInt(array[i]);
            }
            return eq;
        }catch (Exception e){
            return false;
        }
    }

    private static String reverse(String str) {
        String[] array = str.split("");
        ArrayUtils.reverse(array);
        String str1= StringUtils.join(array, "");
//        String str2 = String.format("%s,%s,%s", array);
        return str1;
    }
}
