package tech.powerjob.worker.common.utils;

import tech.powerjob.common.utils.CommonUtils;

/**
 * 文件工具类
 *
 * @author tjq
 * @since 2020/5/16
 */
public class OmsWorkerFileUtils {

    private static final String WORKER_DIR = System.getProperty("user.home") + "/pandora-job/worker/";

    public static String getScriptDir() {
        return WORKER_DIR + "script/";
    }

    public static String getContainerDir() {
        return WORKER_DIR + "container/";
    }
    public static String getFilePath(Long containerId, String version) {
        return getContainerDir() + containerId + "/" + version.substring(version.indexOf("-")+1);
    }

    public static String getH2WorkDir() {
        return WORKER_DIR + "h2/" + CommonUtils.genUUID() + "/";
    }
}
