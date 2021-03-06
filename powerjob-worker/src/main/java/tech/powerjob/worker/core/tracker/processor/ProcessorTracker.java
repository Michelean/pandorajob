package tech.powerjob.worker.core.tracker.processor;

import akka.actor.ActorSelection;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.util.CollectionUtils;
import tech.powerjob.common.EnvConstant;
import tech.powerjob.common.exception.PowerJobException;
import tech.powerjob.common.RemoteConstant;
import tech.powerjob.common.enums.ExecuteType;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.common.enums.TimeExpressionType;
import tech.powerjob.common.request.ServerDeployContainerRequest;
import tech.powerjob.common.utils.CommonUtils;
import tech.powerjob.common.utils.ZipAndRarTools;
import tech.powerjob.worker.common.WorkerRuntime;
import tech.powerjob.worker.common.constants.DockerExecConstant;
import tech.powerjob.worker.common.constants.TaskStatus;
import tech.powerjob.worker.common.utils.AkkaUtils;
import tech.powerjob.worker.common.utils.IpUtil;
import tech.powerjob.worker.common.utils.OmsWorkerFileUtils;
import tech.powerjob.worker.common.utils.SpringUtils;
import tech.powerjob.worker.container.OmsContainer;
import tech.powerjob.worker.container.OmsContainerFactory;
import tech.powerjob.worker.core.ProcessorBeanFactory;
import tech.powerjob.worker.core.executor.ProcessorRunnable;
import tech.powerjob.worker.core.processor.ExeWindowsProcessor;
import tech.powerjob.worker.core.processor.JarWindowsProcessor;
import tech.powerjob.worker.core.processor.PyWindowsProcessor;
import tech.powerjob.worker.core.processor.ShellProcessor;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;
import tech.powerjob.worker.log.impl.OmsServerLogger;
import tech.powerjob.worker.persistence.TaskDO;
import tech.powerjob.worker.pojo.model.InstanceInfo;
import tech.powerjob.worker.pojo.request.ProcessorReportTaskStatusReq;
import tech.powerjob.worker.pojo.request.ProcessorTrackerStatusReportReq;
import tech.powerjob.worker.pojo.request.TaskTrackerStartTaskReq;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * ???????????? Processor ?????????
 *
 * @author tjq
 * @since 2020/3/20
 */
@Slf4j
public class ProcessorTracker {

    /**
     * ??????????????????
     */
    private long startTime;
    private WorkerRuntime workerRuntime;
    /**
     * ??????????????????
     */
    private InstanceInfo instanceInfo;
    /**
     * ?????? instanceId???????????????
     */
    private Long instanceId;
    /**
     * ???????????????
     */
    private BasicProcessor processor;
    /**
     * ????????????????????????
     */
    private OmsContainer omsContainer;
    /**
     * ????????????
     */
    private OmsLogger omsLogger;
    /**
     * ProcessResult ???????????????????????????
     */
    private Queue<ProcessorReportTaskStatusReq> statusReportRetryQueue;
    /**
     * ?????????????????????????????????????????????
     */
    private long lastIdleTime;
    /**
     * ????????????????????????????????????????????????
     */
    private long lastCompletedTaskCount;

    private String taskTrackerAddress;

    private ActorSelection taskTrackerActorRef;

    private ThreadPoolExecutor threadPool;

    private ScheduledExecutorService timingPool;

    private static final int THREAD_POOL_QUEUE_MAX_SIZE = 128;
    /**
     * ?????????????????? ProcessorTracker ?????????????????????
     */
    private static final long MAX_IDLE_TIME = 120000;
    /**
     * ??? ProcessorTracker ?????????????????????????????? Processor ?????????????????????????????????????????????
     */
    private boolean lethal = false;

    private String lethalReason;

    /**
     * ?????? ProcessorTracker???????????????????????????????????????????????? T_T???
     */
    @SuppressWarnings("squid:S1181")
    public ProcessorTracker(TaskTrackerStartTaskReq request, WorkerRuntime workerRuntime) {
        try {
            // ??????
            this.startTime = System.currentTimeMillis();
            this.workerRuntime = workerRuntime;
            this.instanceInfo = request.getInstanceInfo();
            this.instanceId = request.getInstanceInfo().getInstanceId();
            this.taskTrackerAddress = request.getTaskTrackerAddress();

            String akkaRemotePath = AkkaUtils.getAkkaWorkerPath(taskTrackerAddress, RemoteConstant.TASK_TRACKER_ACTOR_NAME);
            this.taskTrackerActorRef = workerRuntime.getActorSystem().actorSelection(akkaRemotePath);

            this.omsLogger = new OmsServerLogger(instanceId, workerRuntime.getOmsLogHandler());
            this.statusReportRetryQueue = Queues.newLinkedBlockingQueue();
            this.lastIdleTime = -1L;
            this.lastCompletedTaskCount = 0L;

            // ????????? ????????????TimingPool ???????????????????????? ThreadPool?????????????????????????????????????????????NPE
            initThreadPool();
            // ?????????????????????
            initTimingJob();
            // ????????? Processor
            initProcessor();

            log.info("[ProcessorTracker-{}] ProcessorTracker was successfully created!", instanceId);
        } catch (Throwable t) {
            log.warn("[ProcessorTracker-{}] create ProcessorTracker failed, all tasks submitted here will fail.", instanceId, t);
            lethal = true;
            lethalReason = ExceptionUtils.getMessage(t);
        }
    }

    /**
     * ??????????????????????????????
     * 1.0?????????TaskTracker????????????dispatch????????? ProcessorTracker ??????????????????????????????????????????????????????????????? ProcessorTracker ?????????
     *         ????????????????????????????????????????????????????????????DB????????????????????????????????????????????????????????????????????????????????????
     *         ?????????????????????SPID?????????TaskStatus??????????????????????????????...
     *         last commitId: 341953aceceafec0fbe7c3d9a3e26451656b945e
     * 2.0?????????ProcessorTracker?????????TaskTracker?????????????????????????????????????????????????????????????????????????????????TaskTracker??????ProcessorTracker
     *         ???????????????????????????????????????????????????ProcessorTracker?????????????????????????????????????????? ??????????????? ?????????...???
     * @param newTask ???????????????????????????????????????
     */
    public void submitTask(TaskDO newTask) {

        // ?????? ProcessorTracker ????????????????????????????????????????????????????????????????????????????????????
        // ???????????????TT??????PT???PT??????????????????????????????????????????TT??????????????????PT???????????????PT???????????????????????????????????????????????????PT????????????????????????????????????GG????????? T_T
        if (lethal) {
            ProcessorReportTaskStatusReq report = new ProcessorReportTaskStatusReq()
                    .setInstanceId(instanceId)
                    .setSubInstanceId(newTask.getSubInstanceId())
                    .setTaskId(newTask.getTaskId())
                    .setStatus(TaskStatus.WORKER_PROCESS_FAILED.getValue())
                    .setResult(lethalReason)
                    .setReportTime(System.currentTimeMillis());

            taskTrackerActorRef.tell(report, null);
            return;
        }

        boolean success = false;
        // 1. ????????????????????????
        newTask.setInstanceId(instanceInfo.getInstanceId());
        newTask.setAddress(taskTrackerAddress);

        ClassLoader classLoader = omsContainer == null ? getClass().getClassLoader() : omsContainer.getContainerClassLoader();
        ProcessorRunnable processorRunnable = new ProcessorRunnable(instanceInfo, taskTrackerActorRef, newTask, processor, omsLogger, classLoader, statusReportRetryQueue, workerRuntime);
        try {
            threadPool.submit(processorRunnable);
            success = true;
        } catch (RejectedExecutionException ignore) {
            log.warn("[ProcessorTracker-{}] submit task(taskId={},taskName={}) to ThreadPool failed due to ThreadPool has too much task waiting to process, this task will dispatch to other ProcessorTracker.",
                    instanceId, newTask.getTaskId(), newTask.getTaskName());
        } catch (Exception e) {
            log.error("[ProcessorTracker-{}] submit task(taskId={},taskName={}) to ThreadPool failed.", instanceId, newTask.getTaskId(), newTask.getTaskName(), e);
        }

        // 2. ??????????????????
        if (success) {
            ProcessorReportTaskStatusReq reportReq = new ProcessorReportTaskStatusReq();
            reportReq.setInstanceId(instanceId);
            reportReq.setSubInstanceId(newTask.getSubInstanceId());
            reportReq.setTaskId(newTask.getTaskId());
            reportReq.setStatus(TaskStatus.WORKER_RECEIVED.getValue());
            reportReq.setReportTime(System.currentTimeMillis());

            taskTrackerActorRef.tell(reportReq, null);

            log.debug("[ProcessorTracker-{}] submit task(taskId={}, taskName={}) success, current queue size: {}.",
                    instanceId, newTask.getTaskId(), newTask.getTaskName(), threadPool.getQueue().size());
        }
    }

    /**
     * ????????????
     */
    public void destroy() {

        // 0. ??????Container??????
        if (omsContainer != null) {
            omsContainer.tryRelease();
        }

        // 1. ???????????????????????????
        CommonUtils.executeIgnoreException(() -> {
            List<Runnable> tasks = threadPool.shutdownNow();
            if (!CollectionUtils.isEmpty(tasks)) {
                log.warn("[ProcessorTracker-{}] shutdown threadPool now and stop {} tasks.", instanceId, tasks.size());
            }
        });

        // 2. ???????????????????????????GC??????
        taskTrackerActorRef = null;
        statusReportRetryQueue.clear();
        ProcessorTrackerPool.removeProcessorTracker(instanceId);

        log.info("[ProcessorTracker-{}] ProcessorTracker destroyed successfully!", instanceId);

        // 3. ?????????????????????
        CommonUtils.executeIgnoreException(() -> timingPool.shutdownNow());
    }


    /**
     * ??????????????????
     */
    private void initThreadPool() {

        int poolSize = calThreadPoolSize();
        // ????????????????????????????????????????????????????????????????????????????????????
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(THREAD_POOL_QUEUE_MAX_SIZE);
        // ????????????????????????????????? (PowerJob Processor Pool -> PPP)
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("PPP-%d").build();
        // ?????????????????????????????????
        RejectedExecutionHandler rejectionHandler = new ThreadPoolExecutor.AbortPolicy();

        threadPool = new ThreadPoolExecutor(poolSize, poolSize, 60L, TimeUnit.SECONDS, queue, threadFactory, rejectionHandler);

        // ???????????????????????????????????????????????????????????????????????????????????????????????????0???
        threadPool.allowCoreThreadTimeOut(true);
    }

    /**
     * ?????????????????????
     */
    private void initTimingJob() {

        // PowerJob Processor TimingPool
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("PPT-%d").build();
        timingPool = Executors.newSingleThreadScheduledExecutor(threadFactory);

        timingPool.scheduleAtFixedRate(new CheckerAndReporter(), 0, 10, TimeUnit.SECONDS);
    }


    /**
     * ????????? TaskTracker ?????????????????????????????????????????????
     */
    private class CheckerAndReporter implements Runnable {

        @Override
        @SuppressWarnings({"squid:S1066","squid:S3776"})
        public void run() {

            // ?????????????????????????????????????????? TaskTracker
            long interval = System.currentTimeMillis() - startTime;
            // ???????????????ProcessorTracker???????????????
            if (!TimeExpressionType.frequentTypes.contains(instanceInfo.getTimeExpressionType())) {
                if (interval > instanceInfo.getInstanceTimeoutMS()) {
                    log.warn("[ProcessorTracker-{}] detected instance timeout, maybe TaskTracker's destroy request missed, so try to kill self now.", instanceId);
                    destroy();
                    return;
                }
            }

            // ?????????????????????????????????????????????????????? TaskTracker ????????????
            if (threadPool.getActiveCount() > 0 || threadPool.getCompletedTaskCount() > lastCompletedTaskCount) {
                lastIdleTime = -1;
                lastCompletedTaskCount = threadPool.getCompletedTaskCount();
            } else {
                if (lastIdleTime == -1) {
                    lastIdleTime = System.currentTimeMillis();
                } else {
                    long idleTime = System.currentTimeMillis() - lastIdleTime;
                    if (idleTime > MAX_IDLE_TIME) {
                        log.warn("[ProcessorTracker-{}] ProcessorTracker have been idle for {}ms, it's time to tell TaskTracker and then destroy self.", instanceId, idleTime);

                        // ????????????????????????????????????????????????????????????????????????????????? ProcessorTracker??????????????????
                        ProcessorTrackerStatusReportReq statusReportReq = ProcessorTrackerStatusReportReq.buildIdleReport(instanceId);
                        statusReportReq.setAddress(workerRuntime.getWorkerAddress());
                        taskTrackerActorRef.tell(statusReportReq, null);
                        destroy();
                        return;
                    }
                }
            }

            // ?????????????????????????????????????????????????????????????????????????????????????????????????????? PT ????????? TT ?????????????????????????????????
            while (!statusReportRetryQueue.isEmpty()) {
                ProcessorReportTaskStatusReq req = statusReportRetryQueue.poll();
                if (req != null) {
                    req.setReportTime(System.currentTimeMillis());
                    if (!AkkaUtils.reliableTransmit(taskTrackerActorRef, req)) {
                        statusReportRetryQueue.add(req);
                        log.warn("[ProcessorRunnable-{}] retry report finished task status failed: {}", instanceId, req);
                        return;
                    }
                }
            }

            // ???????????? ProcessorTracker ??????
            long waitingNum = threadPool.getQueue().size();
            ProcessorTrackerStatusReportReq statusReportReq = ProcessorTrackerStatusReportReq.buildLoadReport(instanceId, waitingNum);
            statusReportReq.setAddress(workerRuntime.getWorkerAddress());
            taskTrackerActorRef.tell(statusReportReq, null);
            log.debug("[ProcessorTracker-{}] send heartbeat to TaskTracker, current waiting task num is {}.", instanceId, waitingNum);
        }

    }

    /**
     * ?????????????????? Processor
     */
    private void initProcessor() throws Exception {

        ProcessorType processorType = ProcessorType.valueOf(instanceInfo.getProcessorType());
        String processorInfo = instanceInfo.getProcessorInfo();

        switch (processorType) {
            case BUILT_IN:
                // ????????? Spring ??????
                if (SpringUtils.supportSpringBean()) {
                    try {
                        processor = SpringUtils.getBean(processorInfo);
                    } catch (Exception e) {
                        log.warn("[ProcessorTracker-{}] no spring bean of processor(className={}), reason is {}.", instanceId, processorInfo, ExceptionUtils.getMessage(e));
                    }
                }
                // ????????????
                if (processor == null) {
                    processor = ProcessorBeanFactory.getInstance().getLocalProcessor(processorInfo);
                }
                break;
            case EXTERNAL:
                String[] split = processorInfo.split("#");
                log.info("[ProcessorTracker-{}] try to load processor({}) in container({})", instanceId, split[1], split[0]);

                String serverPath = AkkaUtils.getServerActorPath(workerRuntime.getServerDiscoveryService().getCurrentServerAddress());
                ActorSelection actorSelection = workerRuntime.getActorSystem().actorSelection(serverPath);
                omsContainer = OmsContainerFactory.fetchContainer(Long.valueOf(split[0]), actorSelection);
                if (omsContainer != null) {
                    processor = omsContainer.getProcessor(split[1]);
                } else {
                    log.warn("[ProcessorTracker-{}] load container failed.", instanceId);
                }
                break;
            case SHELL:
                processor = new ShellProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case CONTAINER_SCRIPT:
                findMatchingProcessor(instanceInfo);
//                processor = new ContainerProcessor(instanceInfo);
                break;
            case JDK7:
                processorInfo = EnvConstant.getJdk7Command() + processorInfo;
                processor = new ShellProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case JDK8:
                processorInfo = EnvConstant.getJdk8Command() + processorInfo;
                processor = new ShellProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case PYTHON2:
                processorInfo = EnvConstant.getPython2Command() + processorInfo;
                processor = new ShellProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case PYTHON3:
                processorInfo = EnvConstant.getPython3Command() + processorInfo;
                processor = new ShellProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case JAVA_WINDOWS:
                processor = new JarWindowsProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case PYTHON_WINDOWS:
                processor = new PyWindowsProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            case EXE:
                processor = new ExeWindowsProcessor(instanceId, processorInfo, instanceInfo.getInstanceTimeoutMS());
                break;
            default:
                log.warn("[ProcessorTracker-{}] unknown processor type: {}.", instanceId, processorType);
                throw new PowerJobException("unknown processor type of " + processorType);
        }

        if (processor == null) {
            log.warn("[ProcessorTracker-{}] fetch Processor(type={},info={}) failed.", instanceId, processorType, processorInfo);
            throw new PowerJobException("fetch Processor failed, please check your processorType and processorInfo config");
        }
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        String jobParams = "-BDwfID=100024,100020";
        StringBuffer sb = new StringBuffer();
        if(StringUtils.isNotBlank(jobParams)){
            if(jobParams.contains("-BD")){
                jobParams += " \"-BDrunDate="+DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss")+"\" -BDenterpriseID=1 -BDapplicationUserID=iphm -BDapplicationUserPassword=iphm -BDjobRecordID="+"222"+" -BDtaskID="+"1"+" -BDresultURL="+ IpUtil.getLocalIp4Address().get().toString().replaceAll("/","")+" -BDcommonParams={\"env\":2}";
            }else{
                //????????????
                jobParams = jobParams.replaceAll("(\\r\\n|\\n|\\n\\r)", "").replaceAll("\"", "'");
                //?????????????????????
                //            jobParams = jobParams.replaceAll("(\\r\\n|\\n|\\n\\r|\t|\\s)", "").replaceAll("\"", "'");
                jobParams = "\"" + jobParams + "\"";

            }
            sb.append(" ").append(jobParams);
        }
        System.out.println(sb.toString());
    }


    private void findMatchingProcessor(InstanceInfo instanceInfo) throws Exception {
        StringBuffer sb = new StringBuffer();
        ServerDeployContainerRequest containerScript = instanceInfo.getContainerScript();
        ServerDeployContainerRequest containerConfig = instanceInfo.getContainerConfig();
        Optional.ofNullable(containerConfig).ifPresent(config -> {
            String filePath = OmsWorkerFileUtils.getFilePath(config.getContainerId(), config.getVersion());
            String containerExecPath = config.getContainerExecPath();
            //????????????????????????????????? ??????????????????????????????????????????????????????
            if(StringUtils.isNotBlank(containerExecPath)){
                filePath = filePath.substring(0, filePath.lastIndexOf("/")) + containerExecPath;
            }

            sb.append(" ").append(filePath);
        });
        String jobParams = instanceInfo.getJobParams();
        if(StringUtils.isNotBlank(jobParams)){
            if(jobParams.contains("-BD")){
                String serverIp = workerRuntime.getWorkerConfig().getServerAddress().get(0).split(":")[0]+":7707";
                jobParams += " \"-BDrunDate="+DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss")+"\" -BDenterpriseID=1 -BDapplicationUserID=iphm -BDapplicationUserPassword=iphm -BDjobRecordID="+instanceInfo.getInstanceId()+" -BDtaskID="+instanceInfo.getJobId()+" -BDresultURL="+ serverIp +" -BDcommonParams={\"env\":2}";
            }else{
                //????????????
                jobParams = jobParams.replaceAll("(\\r\\n|\\n|\\n\\r)", "").replaceAll("\"", "'");
                //?????????????????????
                //            jobParams = jobParams.replaceAll("(\\r\\n|\\n|\\n\\r|\t|\\s)", "").replaceAll("\"", "'");
                jobParams = "\"" + jobParams + "\"";
            }
            sb.append(" ").append(jobParams);
        }
        boolean isLinux = ZipAndRarTools.isLinux();
        String scriptFilePath = OmsWorkerFileUtils.getFilePath(containerScript.getContainerId(), containerScript.getVersion());
        String containerExecPath = containerScript.getContainerExecPath();
        //????????????????????????????????? ??????????????????????????????????????????????????????
        if(StringUtils.isNotBlank(containerExecPath)){
            scriptFilePath = scriptFilePath.substring(0, scriptFilePath.lastIndexOf("/")) + containerExecPath;
        }

        switch (scriptFilePath.substring(scriptFilePath.lastIndexOf(".")+1).toLowerCase()){
            case "jar":
                if(isLinux){
                    sb.insert(0, "java -jar " + scriptFilePath);
                    log.warn("command:{}", sb.toString());
                    processor = new ShellProcessor(instanceId, sb.toString(), instanceInfo.getInstanceTimeoutMS());
                }else{
                    sb.insert(0, scriptFilePath);
                    log.warn("command:{}", sb.toString());
                    processor = new JarWindowsProcessor(instanceId, sb.toString(), instanceInfo.getInstanceTimeoutMS());
                }
                break;
            case "py":
            case "pyc":
                if(isLinux){
                    String command = String.format(DockerExecConstant.PYTHON,scriptFilePath.substring(0, scriptFilePath.lastIndexOf("/")),scriptFilePath);


//                    sb.insert(0, "python3 " + scriptFilePath);
                    sb.insert(0, command);
                    log.warn("command:{}", sb.toString());
                    processor = new ShellProcessor(instanceId, sb.toString(), instanceInfo.getInstanceTimeoutMS());
                }else{
                    sb.insert(0, scriptFilePath);
                    log.warn("command:{}", sb.toString());
                    processor = new PyWindowsProcessor(instanceId, sb.toString(), instanceInfo.getInstanceTimeoutMS());
                }
                break;
            case "sh":
                if(isLinux){
                    sb.insert(0, "sh " + scriptFilePath);
                    log.warn("command:{}", sb.toString());
                    processor = new ShellProcessor(instanceId, sb.toString(), instanceInfo.getInstanceTimeoutMS());
                }
                break;
            case "rar":
            case "zip":
            case "gz":
                break;
            default:
                throw new PowerJobException("????????????????????????");

        }
    }

    /**
     * ?????????????????????
     */
    private int calThreadPoolSize() {
        ExecuteType executeType = ExecuteType.valueOf(instanceInfo.getExecuteType());
        ProcessorType processorType = ProcessorType.valueOf(instanceInfo.getProcessorType());

        // ????????????????????????????????????????????????????????????????????????????????????????????????
        if (processorType == ProcessorType.PYTHON || processorType == ProcessorType.SHELL) {
            return 1;
        }

        if (executeType == ExecuteType.MAP_REDUCE || executeType == ExecuteType.MAP) {
            return instanceInfo.getThreadConcurrency();
        }
        if (TimeExpressionType.frequentTypes.contains(instanceInfo.getTimeExpressionType())) {
            return instanceInfo.getThreadConcurrency();
        }
        return 2;
    }

}
