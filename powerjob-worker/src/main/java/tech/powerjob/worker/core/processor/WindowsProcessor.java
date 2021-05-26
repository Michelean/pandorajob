package tech.powerjob.worker.core.processor;

import lombok.extern.slf4j.Slf4j;
import tech.powerjob.worker.core.ProcessFactory;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.io.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * @author: zmx
 * @date 2021/3/22 9:50
 */
@Slf4j
public abstract class WindowsProcessor implements BasicProcessor {

    private static final ForkJoinPool pool = new ForkJoinPool(4 * Runtime.getRuntime().availableProcessors());

    protected String processInfo;
    private final Long instanceId;
    private final long timeout;

    public WindowsProcessor(Long instanceId, String processInfo, long timeout) {
        this.instanceId = instanceId;
        this.processInfo = processInfo;
        this.timeout = timeout;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("D:/Documents/Downloads/calc_speed.exe", null, new File("D:/Documents/Downloads"));
        StringBuilder inputSB = new StringBuilder();
        StringBuilder errorSB = new StringBuilder();

        try (InputStream is = process.getInputStream(); InputStream es = process.getErrorStream()) {
            copy(is, inputSB);
            copy(es, errorSB);
        }

        process.waitFor();


    }

    private static void copy(InputStream is, StringBuilder sb){
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                System.out.println(line);
                // 同步到在线日志
            }
        }catch (Exception e) {
            log.warn("[ScriptProcessor] copyStream failed.", e);

            sb.append("Exception: ").append(e);
            System.out.println(e);
        }
    }

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("脚本执行");


        Process process = Runtime.getRuntime().exec(fetchRunCommand() + processInfo, null, fetchParentFile());
        StringBuilder inputSB = new StringBuilder();
        StringBuilder errorSB = new StringBuilder();

        String result;
        boolean s;

        try (InputStream is = process.getInputStream(); InputStream es = process.getErrorStream()) {

//            pool.execute(() -> copyStream(is, inputSB, omsLogger));
//            pool.execute(() -> copyStream(es, errorSB, omsLogger));
            new Thread(() -> copyStream(is, inputSB, omsLogger)).start();
            new Thread(() -> copyStream(es, errorSB, omsLogger)).start();

            ProcessFactory.recordProcess(context.getInstanceId(), process);
            s = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            if (!s) {
                process.destroy();
                omsLogger.info("SYSTEM===> process timeout");
                return new ProcessResult(false, "TIMEOUT");
            }
            ProcessFactory.deleteRecord(context.getInstanceId());
            // 0 代表正常退出
            int exitValue = process.exitValue();
            s = exitValue == 0?true:false;
        } catch (Exception e) {
            s = false;
            process.destroy();
            omsLogger.info("调用脚本出错："+ e.getMessage());
        } finally {
            result = String.format("[INPUT]: %s;[ERROR]: %s", inputSB.toString(), errorSB.toString());
        }
        // 返回结果，该结果会被持久化到数据库，在前端页面直接查看，极为方便
        return new ProcessResult(s, result);
    }

    private static void copyStream(InputStream is, StringBuilder sb, OmsLogger omsLogger) {
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                // 同步到在线日志
                omsLogger.info(line);
            }
        } catch (Exception e) {
            log.warn("[WindowsProcessor] copyStream failed.", e);
            omsLogger.warn("[WindowsProcessor] copyStream failed.", e);

            sb.append("Exception: ").append(e);
        }
    }
    protected abstract String fetchRunCommand();

    protected abstract File fetchParentFile();
}
