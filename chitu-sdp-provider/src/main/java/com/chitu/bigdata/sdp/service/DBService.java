package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.DBManageBO;
import com.chitu.bigdata.sdp.api.vo.DBManageVO;
import com.chitu.bigdata.sdp.mapper.DBMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/10 21:35
 */
@Slf4j
@Service
public class DBService {
    @Autowired
    private DBMapper dbMapper;

    public DBManageVO execute(DBManageBO dbManageBO) {
        DBManageVO managerVO = new DBManageVO();
        try{
            int fullDataSize = 0;
            Map fullData = null;
            List<Map> result = dbMapper.execute(dbManageBO);
            if(result != null && result.size() > 0){
                for(Map map : result){
                    if(map.size() > fullDataSize){
                        fullDataSize = map.size();
                        fullData = map;
                    }
                }
                managerVO.setColumnList(fullData.keySet());
                List rows = new ArrayList();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for(Map map : result){
                    List values = new ArrayList();
                    for(String column : managerVO.getColumnList()){
                        Object value = map.get(column);
                        if(value != null){
                            if(value instanceof Timestamp){
                                values.add(sdf.format((Timestamp)value));
                            }else{
                                values.add(value);
                            }
                        }else{
                            values.add("");
                        }
                    }
                    rows.add(values);
                }
                managerVO.setRows(rows);
                managerVO.setEffectRow(result.size());
            }
        }catch (Exception e){
            log.error("提交执行失败==={}",e);
        }
        return managerVO;
    }
}
