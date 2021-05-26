package tech.powerjob.worker.core.processor;

import java.io.File;

/**
 * @author: zmx
 * @date 2021/4/16 15:59
 */
public class JarWindowsProcessor extends WindowsProcessor {


    public JarWindowsProcessor(Long instanceId, String processInfo, long timeout) {
        super(instanceId, processInfo, timeout);
    }

    @Override
    protected String fetchRunCommand() {
        return "java -jar ";
    }

    @Override
    protected File fetchParentFile() {
        File file = new File(processInfo.substring(0, processInfo.indexOf(".jar")+4));
        return file.getParentFile();
    }
}
