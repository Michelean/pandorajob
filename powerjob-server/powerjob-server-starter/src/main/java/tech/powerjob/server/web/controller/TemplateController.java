package tech.powerjob.server.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.powerjob.common.response.ResultDTO;
import tech.powerjob.server.common.constants.SwitchableStatus;
import tech.powerjob.server.common.utils.ConvertUtils;
import tech.powerjob.server.core.template.TemplateService;
import tech.powerjob.server.persistence.remote.model.TemplateDO;
import tech.powerjob.server.web.request.SaveTemplateRequest;
import tech.powerjob.server.web.response.ContainerInfoVO;
import tech.powerjob.server.web.response.TemplateVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 传参json模板管理
 * @author: zmx
 * @date 2021/6/3 14:38
 */
@Slf4j
@RestController
@RequestMapping("/template")
public class TemplateController {
    @Autowired
    private TemplateService templateService;

    @PostMapping("/save")
    public ResultDTO<Void> saveTemplate(@RequestBody SaveTemplateRequest request) {
        templateService.save(ConvertUtils.convert(request, TemplateDO::new));
        return ResultDTO.success(null);
    }
    @GetMapping("/delete")
    public ResultDTO<Void> deleteTemplate(Long id) {
        templateService.delete(id);
        return ResultDTO.success(null);
    }

    @GetMapping("/list")
    public ResultDTO<List<TemplateVO>> listTemplates(Long appId) {
        List<TemplateDO> templateDOS = templateService.listByAppId(appId);
        return ResultDTO.success(ConvertUtils.convertList(templateDOS, TemplateVO::new));
    }

}
