package tech.powerjob.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: zmx
 * @date 2022/5/12
 */
public class ProcessUtils {
    public static int getPid(Process process){
        List<String> matches=getMatches("pid=\\d*",process.toString());
        String pid="";
        if (matches!=null||matches.size()>0){
            pid=matches.get(0);
        }
        List<String> pidList=getMatches("\\d+",pid);
        if (pidList!=null||pidList.size()>0){
            pid=pidList.get(0);
        }else {
            pid="-1";
        }
        return Integer.valueOf(pid);
    }

    /**
     * 获取正则匹配的部分
     *
     * @param regex 正则表达式
     * @param input 要匹配的字符串
     * @return 正则匹配的部分
     */
    public static List<String> getMatches(String regex, CharSequence input) {
        if (input == null) {
            return null;
        }
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

}
