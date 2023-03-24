

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-4-18
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.model.SdpSavepoint;
import com.chitu.bigdata.sdp.mapper.SdpSavepointMapper;
import com.chitu.bigdata.sdp.utils.HdfsUtils;
import com.chitu.cloud.service.GenericService;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 保存点记录信息业务类
 * </pre>
 */
@Service("sdpSavepointService")
public class SdpSavepointService extends GenericService<SdpSavepoint, Long> {
    public SdpSavepointService(@Autowired SdpSavepointMapper sdpSavepointMapper) {
        super(sdpSavepointMapper);
    }

    @Autowired
    JobService jobService;

    
    public SdpSavepointMapper getMapper() {
        return (SdpSavepointMapper) super.genericMapper;
    }

    public List<SdpSavepoint> queryByJobId( Long jobId, String operateStatus, Integer size){
        List<SdpSavepoint> sdpSavepoints = getMapper().queryByJobId(jobId, operateStatus, size);
        //序号赋值
        if(!CollectionUtils.isEmpty(sdpSavepoints)){
            for (int i = 0,len = sdpSavepoints.size(); i < len; i++) {
                sdpSavepoints.get(i).setOrderNum((i+1));
            }
        }
        String hadoopConfDir = jobService.getHadoopConfDir(jobId);
        return filertSavePointPath(hadoopConfDir,sdpSavepoints);
    }


    private List<SdpSavepoint> filertSavePointPath(String hadoopConfDir, List<SdpSavepoint> savepointList) {
        if(CollectionUtil.isEmpty(savepointList)){
            return savepointList;
        }
        List<SdpSavepoint> filertSavepointList = new ArrayList<>();
        FileSystem fileSystem = null;
        try {
            fileSystem = HdfsUtils.getFileSystem(hadoopConfDir);
            for(SdpSavepoint item:savepointList){
                // 兼容检查点为空场景
                if(StrUtil.isEmpty(item.getFilePath())){
                    filertSavepointList.add(item);
                    continue;
                }
                if (fileSystem.exists(new Path(item.getFilePath()))) {
                    filertSavepointList.add(item);
                }
            }
        } catch (IOException e) {
            logger.error("hdfs操作异常", e);
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    logger.error("hdfs关闭连接异常", e);
                }
            }
        }

        return filertSavepointList;
    }

}