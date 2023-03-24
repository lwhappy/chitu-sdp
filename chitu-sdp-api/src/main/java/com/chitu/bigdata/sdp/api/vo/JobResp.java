package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.model.SdpJob;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author 587694
 * @create 2021-10-25 11:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobResp extends SdpJob implements Serializable {

  private static final long serialVersionUID = 1L;
/*

  */
/**
   * 字段名称：cpu数
   *
   *//*

    private String cpuCore;

    */
/**
     * 字段名称：内存数
     *
     *//*

    private String memoryGb;

    */
/**
     * 字段名称：是否有更新版本
     *
     *//*

    private Integer isNewVersion;

  */
/**
   * 希望状态
   *//*

   private String expectStatus;

  private String flinkUrl;

  private String fileType;

  private String jobStatus;

  */
/**
   * 并行度
   *//*

  private String parallelism;

  */
/**
   * 插槽数
   *//*

  private String slots;

  */
/**
   * 上级目录ID
   *//*

  private Long folderId;

  */
/**
   * 目录全路径
   *//*

  private String fullPath;

  private Integer priority;
*/


}
