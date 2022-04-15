package tech.powerjob.common.response;

import lombok.Data;

import java.util.List;

/**
 * @author: zmx
 * @date 2022/4/15
 */
@Data
public class TreeNodeDTO {

    private String scadaId;

    private String name;

    private List<TreeNodeDTO> children;

}
