package tech.powerjob.worker.core.processor;

import java.io.File;

public class ExeWindowsProcessor extends WindowsProcessor {


    public ExeWindowsProcessor(Long instanceId, String processInfo, long timeout) {
        super(instanceId, processInfo, timeout);
    }

    @Override
    protected String fetchRunCommand() {
        return "";
    }

    @Override
    protected File fetchParentFile() {
        File file = new File(processInfo.substring(0, processInfo.indexOf(".exe")+4));
        return file.getParentFile();
    }

}
