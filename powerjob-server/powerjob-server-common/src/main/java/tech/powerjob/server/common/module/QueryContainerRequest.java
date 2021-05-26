package tech.powerjob.server.common.module;

import lombok.Data;
import tech.powerjob.server.common.constants.ContainerSourceType;

@Data
public class QueryContainerRequest {

    // 任务所属应用ID
    private Long appId;
    // 当前页码
    private Integer index;
    // 页大小
    private Integer pageSize;

    // 查询条件
    private ContainerSourceType sourceType;

    private String keyword;
}
