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
@Table(name = "basic_wf",indexes = {@Index(columnList = "wfId")})
public class WfDO implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private String wfId;

    /**
     * 电场编号
     */
    private String wfNumber;

    /**
     * 电场名称
     */
    private String wfName;

    /**
     * 所属公司
     */
    private String wfInvestname;

    /**
     * 风机数量
     */
    private Integer wfCount;

    /**
     * 描述
     */
    private String wfDalias;

    /**
     * 装机容量
     */
    private Double wfCapability;

    /**
     * 地址
     */
    private String wfAddress;

    /**
     * 电场负责人
     */
    private String wfCommander;

    /**
     * 电场联系人
     */
    private String wfControlperson;

    /**
     * 电话
     */
    private String wfTel;

    /**
     * 邮箱
     */
    private String wfEmail;

    /**
     * 图片
     */
    private byte[] wfPic;

    /**
     * 纬度
     */
    private Double wfXcoordinate;

    /**
     * 经度
     */
    private Double wfYcoordinate;

    /**
     * 并网时间
     */
    private LocalDateTime wfTime;

    /**
     * 排序
     */
    private Integer wfIndex;

    /**
     * SCADA编号
     */
    private String wfScadaid;

    /**
     * 类型 0 风电  1 光伏  2 风光储
     */
    private Integer wfType;

    /**
     * CMS 出具报告类型 1：季度   2：月度
     */
    private Integer wfReporttype;

    /**
     * 项目概述
     */
    private String wfSummary;

    /**
     * 最终修改时间
     */
    private LocalDateTime wfLastmodifydate;

    /**
     * 传输方式（在线，离线，隔离）
     */
    private Integer wfSendmode;

    /**
     * 电场展示名称
     */
    private String wfShowName;

    /**
     * 密钥（解密波形使用）
     */
    private String wfKeyid;

    /**
     * 新增时间
     */
    private LocalDateTime wfCreateTime;


}
