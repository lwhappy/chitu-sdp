

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpJar;
import com.chitu.cloud.model.GenericBO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * <pre>
 * jar管理业务实体类
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "JAR入参实体")
public class SdpJarBO extends GenericBO<SdpJar> {
    public SdpJarBO() {
        setVo(new SdpJar());
    }
    public SdpJar getSdpJar() {
        return (SdpJar) getVo();
    }

    public void setSdpJar(SdpJar vo) {
        setVo(vo);
    }

    /**
     * 字段名称：jar名称
     *
     * 数据库字段信息:name VARCHAR(255)
     */
    @Valid
    @NotNull(message = "JAR名称不能为空")
    @ApiModelProperty("JAR名称")
    private String name;

    /**
     * 字段名称：jar版本
     *
     * 数据库字段信息:version VARCHAR(255)
     */
    @ApiModelProperty("JAR版本")
    private String version;

    /**
     * 字段名称：git地址
     *
     * 数据库字段信息:git VARCHAR(255)
     */
    @ApiModelProperty("git地址")
    private String git;

    @ApiModelProperty("jar hdfs url")
    private String url;

    /**
     * 字段名称：jar描述
     *
     * 数据库字段信息:describe VARCHAR(255)
     */
    @ApiModelProperty("JAR描述")
    private String description;

    private Long projectId;

    private String projectCode;
}