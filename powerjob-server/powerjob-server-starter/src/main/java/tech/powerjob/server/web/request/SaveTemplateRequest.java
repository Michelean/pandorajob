package tech.powerjob.server.web.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: zmx
 * @date 2021/6/3 14:40
 */
@Data
@Accessors(chain = true)
@ApiModel
public class SaveTemplateRequest {
    private Long id;
    private String name;
    private String code;
    private Long appId;
    private String json;
}
