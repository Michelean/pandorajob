package tech.powerjob.server.persistence.remote.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author: zmx
 * @date 2021/6/3 14:47
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {@Index(columnList = "appId")})
public class TemplateDO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    /**
     * 所属的应用ID
     */
    private Long appId;

    private String name;

    private String code;

    private String json;

    private Date gmtCreate;

    private Date gmtModified;
}
