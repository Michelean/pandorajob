package tech.powerjob.worker.core;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * @author: zmx
 * @date 2021/4/13 9:37
 */
@Slf4j
public class ProcessFactory {
    private static final Map<Long, Process> CARGO = Maps.newConcurrentMap();

    public static void recordProcess(Long instanceId, Process process){
        CARGO.put(instanceId, process);
    }

    public static void destroyProcess(Long instanceId){
        Optional.ofNullable(CARGO.get(instanceId)).ifPresent(process -> {
            log.warn("[ProcessFactory-instanceId:{}] - destroy process", instanceId);
            process.destroy();
            deleteRecord(instanceId);
        });
    }

    public static void deleteRecord(Long instanceId){
        CARGO.remove(instanceId);
    }
}
