package com.chitu.bigdata.sdp.api.enums;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/12/10 15:19
 */
public enum ApproveStatus {
    /**
     * 审核状态:同意
     */
    AGREE("同意"),
    /**
     * 审核状态:不同意
     */
    DISAGREE("不同意"),
    /**
     * 审核状态:待审批
     */
    PENDING("待审批"),
    /**
     * 审核状态:审批中
     */
    APPROVING("审批中"),
    /**
     * 审核状态:已撤销
     */
    CANCEL("已撤销");

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    ApproveStatus(String status) {
        this.status = status;
    }
    public static ApproveStatus of(String status){
        ApproveStatus approveStatus = null;
        for (ApproveStatus ap : values()){
            if (ap.name().equalsIgnoreCase(status)){
                approveStatus = ap;
            }
        }
        return approveStatus;
    }
}
