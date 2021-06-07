package tech.powerjob.server.persistence.remote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tech.powerjob.server.persistence.remote.model.ContainerInfoDO;
import tech.powerjob.server.persistence.remote.model.TemplateDO;

import java.util.List;

public interface TemplateRepository extends JpaRepository<TemplateDO, Long>, JpaSpecificationExecutor<TemplateDO> {

    List<TemplateDO> findByAppId(Long appId);
}
