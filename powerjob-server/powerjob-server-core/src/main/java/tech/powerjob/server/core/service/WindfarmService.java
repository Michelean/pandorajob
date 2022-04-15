package tech.powerjob.server.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.powerjob.common.response.MonitorPartsDTO;
import tech.powerjob.common.response.WfDTO;
import tech.powerjob.server.common.utils.ConvertUtils;
import tech.powerjob.server.persistence.external.MonitorPartsRepository;
import tech.powerjob.server.persistence.external.WfRepository;

import javax.annotation.Resource;
import java.util.List;

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

    public List<WfDTO> listWindfarm() {
        return ConvertUtils.convertList(wfRepository.findAll(), WfDTO::new);
    }



    public List<MonitorPartsDTO> getTurbineByWfScadaid(String wfScadaid) {
        return ConvertUtils.convertList(monitorPartsRepository.findByWfScadaid(wfScadaid), MonitorPartsDTO::new);
    }


}
