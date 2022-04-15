package tech.powerjob.server.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.powerjob.common.response.MonitorPartsDTO;
import tech.powerjob.common.response.TreeNodeDTO;
import tech.powerjob.common.response.WfDTO;
import tech.powerjob.server.common.utils.ConvertUtils;
import tech.powerjob.server.persistence.external.MonitorPartsDO;
import tech.powerjob.server.persistence.external.MonitorPartsRepository;
import tech.powerjob.server.persistence.external.WfDO;
import tech.powerjob.server.persistence.external.WfRepository;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author: zmx
 * @date 2022/4/14
 */
@Service
@Slf4j
public class WindfarmService {

    @Resource
    private WfRepository wfRepository;
    @Resource
    private MonitorPartsRepository monitorPartsRepository;
    @Resource
    private CacheService cacheService;

    public List<WfDTO> listWindfarm() {
        List<WfDTO> wfDTOS = ConvertUtils.convertList(wfRepository.findAll(), WfDTO::new);
        wfDTOS = wfDTOS.stream()
                .collect
                        (Collectors.collectingAndThen(Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(WfDTO::getWfScadaid)))
                                , ArrayList::new));
        return wfDTOS;
    }



    public List<MonitorPartsDTO> getTurbineByWfScadaid(String wfScadaid) {
        return ConvertUtils.convertList(monitorPartsRepository.findByWfScadaid(wfScadaid), MonitorPartsDTO::new);
    }


    public List<TreeNodeDTO> getTurbineTree() {
        List<WfDO> wfDOList = wfRepository.findAll();
        List<TreeNodeDTO> treeList = Collections.synchronizedList(new ArrayList());
        Supplier<TreeNodeDTO> supplier = TreeNodeDTO::new;
        wfDOList.parallelStream().forEach(w->{
            TreeNodeDTO treeNodeDTO = supplier.get();
            List<MonitorPartsDO> monitorPartsDOList = cacheService.getMonitorPartsDOs(w.getWfId());
            treeNodeDTO.setName(w.getWfName());
            treeNodeDTO.setScadaId(w.getWfScadaid());
            List<TreeNodeDTO> collect = monitorPartsDOList.stream().map(m -> {
                TreeNodeDTO node = supplier.get();
                node.setScadaId(m.getBmpScadaid());
                node.setName(m.getBmpName());
                return node;
            }).collect(Collectors.toList());
            treeNodeDTO.setChildren(collect);
            treeList.add(treeNodeDTO);
        });
        return treeList;
    }
}
