package tech.powerjob.common.response;

import lombok.Data;

/**
 * @author: zmx
 * @date 2022/4/14
 */
@Data
public class WfDTO {
    private String wfId;

    private String wfName;
    /**
     * SCADA编号
     */
    private String wfScadaid;
}
