package tech.powerjob.server.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tech.powerjob.common.OmsConstant;
import tech.powerjob.common.response.ResultDTO;
import tech.powerjob.server.common.module.QueryContainerRequest;
import tech.powerjob.server.persistence.PageResult;
import tech.powerjob.server.remote.transport.starter.AkkaStarter;
import tech.powerjob.server.common.constants.ContainerSourceType;
import tech.powerjob.server.common.constants.SwitchableStatus;
import tech.powerjob.server.core.container.ContainerTemplateGenerator;
import tech.powerjob.server.common.utils.OmsFileUtils;
import tech.powerjob.server.persistence.remote.model.AppInfoDO;
import tech.powerjob.server.persistence.remote.model.ContainerInfoDO;
import tech.powerjob.server.persistence.remote.repository.AppInfoRepository;
import tech.powerjob.server.persistence.remote.repository.ContainerInfoRepository;
import tech.powerjob.server.core.container.ContainerService;
import tech.powerjob.server.web.request.GenerateContainerTemplateRequest;
import tech.powerjob.server.web.request.SaveContainerInfoRequest;
import tech.powerjob.server.web.response.ContainerInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 容器信息控制层
 *
 * @author tjq
 * @since 2020/5/15
 */
@Slf4j
@RestController
@RequestMapping("/container")
public class ContainerController {

    @Value("${server.port}")
    private int port;

    @Resource
    private ContainerService containerService;
    @Resource
    private AppInfoRepository appInfoRepository;
    @Resource
    private ContainerInfoRepository containerInfoRepository;

    @GetMapping("/downloadJar")
    public void downloadJar(String version, Integer type, HttpServletResponse response) throws IOException {
        File file = containerService.fetchContainerJarFile(URLDecoder.decode(version), type);
        if (file.exists()) {
            OmsFileUtils.fileDownLoad(file, response);
        }
    }

    @PostMapping("/downloadContainerTemplate")
    public void downloadContainerTemplate(@RequestBody GenerateContainerTemplateRequest req, HttpServletResponse response) throws IOException {
        File zipFile = ContainerTemplateGenerator.generate(req.getGroup(), req.getArtifact(), req.getName(), req.getPackageName(), req.getJavaVersion());
        OmsFileUtils.file2HttpResponse(zipFile, response);
    }

    @PostMapping("/scriptUpload")
    public ResultDTO<String> scriptUpload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResultDTO.failed("empty file");
        }
        return ResultDTO.success(containerService.uploadContainerScriptFile(file));
    }

    @PostMapping("/jarUpload")
    public ResultDTO<String> fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResultDTO.failed("empty file");
        }
        return ResultDTO.success(containerService.uploadContainerJarFile(file));
    }

    @PostMapping("/save")
    public ResultDTO<Void> saveContainer(@RequestBody SaveContainerInfoRequest request) {
        request.valid();

        ContainerInfoDO container = new ContainerInfoDO();
        BeanUtils.copyProperties(request, container);
        container.setSourceType(request.getSourceType().getV());
        container.setStatus(request.getStatus().getV());

        containerService.save(container);
        return ResultDTO.success(null);
    }

    @GetMapping("/delete")
    public ResultDTO<Void> deleteContainer(Long appId, Long containerId) {
        containerService.delete(appId, containerId);
        return ResultDTO.success(null);
    }

    @GetMapping("/list")
    public ResultDTO<List<ContainerInfoVO>> listContainers(Long appId) {
        List<ContainerInfoVO> res = containerInfoRepository.findByAppIdAndStatusNot(appId, SwitchableStatus.DELETED.getV())
                .stream().map(ContainerController::convert).collect(Collectors.toList());
        return ResultDTO.success(res);
    }

    @PostMapping("/listPage")
    public ResultDTO<PageResult<ContainerInfoVO>> listPage(@RequestBody QueryContainerRequest request) {
        ContainerSourceType sourceType = request.getSourceType();
        String keyword = request.getKeyword();
        Sort sort = Sort.by(Sort.Direction.DESC, "gmtModified");
        Pageable pageable = PageRequest.of(request.getIndex(), request.getPageSize(), sort);
        Specification<ContainerInfoDO> specification = (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(cb.notEqual(root.get("status"), SwitchableStatus.DELETED.getV()));
            if(sourceType != null) {
                list.add(cb.equal(root.get("sourceType"), sourceType.getV()));
            }
            Predicate and = cb.and(list.toArray(new Predicate[list.size()]));

            if(StringUtils.isNotBlank(keyword)){
                List<Predicate> listPermission = new ArrayList<>();
                String condition = "%" + request.getKeyword() + "%";
                listPermission.add(cb.like(root.get("containerName"), condition));
                listPermission.add(cb.like(root.get("containerDesc"), condition));
                return query.where(and, cb.or(listPermission.toArray(new Predicate[listPermission.size()]))).getRestriction();
            }
            return and;
        };

        Page<ContainerInfoDO> containerInfoPage = containerInfoRepository.findAll(specification, pageable);

//        Page<ContainerInfoDO> containerInfoPage = null;
//        if(sourceType == null && StringUtils.isBlank(keyword)){
//            containerInfoPage = containerInfoRepository.findByAppIdAndStatusNot(request.getAppId(), SwitchableStatus.DELETED.getV(), pageRequest);
//
////            List<ContainerInfoVO> res = containerInfoRepository.findByAppIdAndStatusNot(request.getAppId(), SwitchableStatus.DELETED.getV())
////                    .stream().map(ContainerController::convert).collect(Collectors.toList());
//        }else if(sourceType != null && StringUtils.isBlank(keyword)){
//            containerInfoPage = containerInfoRepository.findByAppIdAndSourceTypeAndStatusNot(request.getAppId(), sourceType.getV(), SwitchableStatus.DELETED.getV(), pageRequest);
//        }else if(StringUtils.isNotBlank(keyword)){
//            String condition = "%" + request.getKeyword() + "%";
//            if(sourceType == null){
//                containerInfoPage = containerInfoRepository.findByAppIdAndContainerNameLikeOrContainerDescLikeAndStatusNot(request.getAppId(), condition, condition, SwitchableStatus.DELETED.getV(), pageRequest);
//            }else{
//                containerInfoPage = containerInfoRepository.findByAppIdAndContainerNameLikeOrContainerDescLikeAndSourceTypeAndStatusNot(request.getAppId(), condition, condition, sourceType.getV(), SwitchableStatus.DELETED.getV(), pageRequest);
//            }
//        }


        return ResultDTO.success(convertPage(containerInfoPage));
    }


    @GetMapping("/listDeployedWorker")
    public ResultDTO<String> listDeployedWorker(Long appId, Long containerId, HttpServletResponse response) {
        AppInfoDO appInfoDO = appInfoRepository.findById(appId).orElseThrow(() -> new IllegalArgumentException("can't find app by id:" + appId));
        String targetServer = appInfoDO.getCurrentServer();

        if (StringUtils.isEmpty(targetServer)) {
            return ResultDTO.failed("No workers have even registered！");
        }

        // 转发 HTTP 请求
        if (!AkkaStarter.getActorSystemAddress().equals(targetServer)) {
            String targetIp = targetServer.split(":")[0];
            String url = String.format("http://%s:%d/container/listDeployedWorker?appId=%d&containerId=%d", targetIp, port, appId, containerId);
            try {
                response.sendRedirect(url);
                return ResultDTO.success(null);
            }catch (Exception e) {
                return ResultDTO.failed(e);
            }
        }
        return ResultDTO.success(containerService.fetchDeployedInfo(appId, containerId));
    }

    private static ContainerInfoVO convert(ContainerInfoDO containerInfoDO) {
        ContainerInfoVO vo = new ContainerInfoVO();
        BeanUtils.copyProperties(containerInfoDO, vo);
        if (containerInfoDO.getLastDeployTime() == null) {
            vo.setLastDeployTime("N/A");
        }else {
            vo.setLastDeployTime(DateFormatUtils.format(containerInfoDO.getLastDeployTime(), OmsConstant.TIME_PATTERN));
        }
        SwitchableStatus status = SwitchableStatus.of(containerInfoDO.getStatus());
        vo.setStatus(status.name());
        ContainerSourceType sourceType = ContainerSourceType.of(containerInfoDO.getSourceType());
        vo.setSourceType(sourceType.name());
        return vo;
    }

    private PageResult<ContainerInfoVO> convertPage(Page<ContainerInfoDO> containerInfoPage) {
        List<ContainerInfoVO> containerInfoVOList = containerInfoPage.getContent().stream().map(ContainerController::convert).collect(Collectors.toList());
        PageResult<ContainerInfoVO> pageResult = new PageResult<>(containerInfoPage);
        pageResult.setData(containerInfoVOList);
        return pageResult;
    }

}
