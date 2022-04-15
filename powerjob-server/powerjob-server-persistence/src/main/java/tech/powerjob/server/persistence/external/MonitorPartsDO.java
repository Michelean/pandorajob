package tech.powerjob.server.persistence.external;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2022-04-14
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "basic_monitor_parts",indexes = {@Index(columnList = "bmpId")})
public class MonitorPartsDO implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private String bmpId;

    /**
     * 风场ID
     */
    private String wfId;

    /**
     * 编号
     */
    private String bmpNumber;

    /**
     * scadaid
     */
    private String bmpScadaid;

    /**
     * 名称
     */
    private String bmpName;

    /**
     * 排序编号
     */
    private Integer bmpIndex;

    /**
     * 父节点
     */
    private String bmpParentid;

    /**
     * 是否启用
     */
    private Integer bmpIsuse;

    /**
     * 关联参数或参数组id
     */
    private String bmpPartparamsid;

    /**
     * 扩展参数 (参见 extended表)
     */
    private String bmpExtended;

    /**
     * 关联基础信息	关联基础信息，根据类型不同，关联不同部件基础表id（馈线，风机，消防，塔筒，叶片，主轴，齿轮箱，发电机，风机基础信息-关系表，设备-箱变关联box_position.position_uuid）
     */
    private String bipId;

    /**
     * 最终修改时间
     */
    private LocalDateTime bmpLastmodifydate;

    /**
     * 监测对象类型ID
     */
    private String bsdId;


}
