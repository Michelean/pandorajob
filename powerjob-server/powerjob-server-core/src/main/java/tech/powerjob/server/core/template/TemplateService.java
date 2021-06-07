package tech.powerjob.server.core.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.powerjob.server.persistence.remote.model.TemplateDO;
import tech.powerjob.server.persistence.remote.repository.TemplateRepository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author: zmx
 * @date 2021/6/3 14:45
 */
@Service
@Slf4j
public class TemplateService {
    @Resource
    private TemplateRepository templateRepository;

    /**
     * 保存模板
     * @param templateDO
     */
    public void save(TemplateDO templateDO) {

        Long originId = templateDO.getId();
        if (originId != null) {
            // just validate
            templateRepository.findById(originId).orElseThrow(() -> new IllegalArgumentException("can't find template by id: " + originId));
        } else {
            templateDO.setGmtCreate(new Date());
        }
        templateDO.setGmtModified(new Date());

        templateRepository.saveAndFlush(templateDO);
    }

    public void delete(Long id) {
        templateRepository.deleteById(id);
    }

    public List<TemplateDO> listByAppId(Long appId) {
        List<TemplateDO> templateDOList = templateRepository.findByAppId(appId);
        return templateDOList;
    }
}
