package com.chitu.bigdata.sdp.api.bo;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/12 19:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserSearchBO extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nameOrNumber;
}
