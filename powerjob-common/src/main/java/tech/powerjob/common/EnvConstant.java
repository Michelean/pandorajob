package tech.powerjob.common;

public class EnvConstant {

    public static final String FILTER_STR = "{filterStr}";

    private static final String HOME = "/home";

    private static final String ROOT = HOME + "/pandoraJob/env";

    private static final String JDK7_PATH = ROOT + "/jdk1.7";

    private static final String JDK8_PATH = ROOT + "/jdk1.8";

    /*private static final String killCommand = "#!/bin/sh \n" +
            " if [ `ps ax |grep -v grep |grep '%s' |awk '{print $1}'`\"a\" = \"a\" ] ;" +
            "then  echo \"no pid to kill\"; " +
            "else kill -9 `ps ax |grep -v grep |grep '%s' |awk '{print $1}'`; fi";*/

    private static final String killCommand = "#!/bin/sh \n" +
            "name=" + FILTER_STR +"\n"+
            "if [ `ps ax |grep -v grep |grep $name |awk '{print $1}'`\"a\" = \"a\" ] ;then  echo \"no pid to kill\"; else kill -9 `ps ax |grep -v grep |grep $name |awk '{print $1}'`; fi";



    public static String getJdk7Command(){
        return JDK7_PATH + "/bin/java -jar ";
    }

    public static String getJdk8Command(){
        return JDK8_PATH + "/bin/java -jar ";
    }

    public static String getPython2Command(){
        return "python ";
    }

    public static String getPython3Command(){
        return "python3 ";
    }

    public static String getKillCommand(){
        return killCommand;
    }

}
