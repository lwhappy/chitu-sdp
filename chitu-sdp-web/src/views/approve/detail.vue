<template>
  <div class="detail-container"
       v-loading="isLoading">
    <div class="detail-top">
      <h3 class="justify-start">
        <span><i class="chitutree-h5 chitutreefanhui "
             @click="back"></i>作业申请详情</span>
        <em>提示：项目管理员负责作业代码和配置审批, 系统管理员负责查询和写入数据源权限审批。请审批人用心审批！</em>
      </h3>

    </div>
    <div v-if="detail"
         class="detail-content">
      <div class="steps">
        <!-- 项目管理员提交的审批一级自动审批，不显示一级审批节点 -->
        <div v-if="detail.approveFlow.updatedBy === 'system'"
             class="two-step justify-center">
          <div class="item finish">
            <div class="index">1</div>
            <div class="first">审批发起</div>
            <p class="second">
              申请人：{{detail.approveFlow.createdBy}}
            </p>
            <p class="third">{{$common.timeToDate(detail.approveFlow.creationDate)}}</p>
          </div>
          <div class="item"
               :class="{'finish':currentStatus > 1, 'active' : currentStatus === 1}">
            <div class="line"></div>
            <template>
              <img style="width:24px"
                   src="@/assets/icons/setp_success.png"
                   alt=""
                   v-if="detail.approveFlow.status2 === 'AGREE'">
              <img src="@/assets/icons/step_failure.png"
                   alt=""
                   v-else-if="detail.approveFlow.status2 === 'DISAGREE'">
              <div v-else
                   class="index">2</div>
            </template>

            <p class="first">
              <span v-if="detail.approveFlow.status2 === 'PENDING'">待审批</span>
              <!--PENDING表示的是待审批，只是流程到当前节点时文案用‘审批中’-->
              <span v-else-if="detail.approveFlow.status2 === 'APPROVING'">待审批</span>
              <span class="success"
                    v-else-if="detail.approveFlow.status2 === 'AGREE'">审批同意</span>
              <span class="fail"
                    v-else-if="detail.approveFlow.status2 === 'DISAGREE'">审批驳回</span>
            </p>
            <div class="second">2级多人审批：

              <a-popover v-if="detail.approveFlow.updatedBy2"
                         placement="topLeft">
                <template slot="content">
                  <div v-html="detail.approveFlow.updatedBy2"
                       style="word-break: break-all;">
                  </div>
                </template>
                <span>{{detail.approveFlow.updatedBy2}}</span>
              </a-popover>
              <a-popover v-else
                         placement="topLeft">
                <template slot="content">
                  <div v-html="detail.approveFlow.approver2"
                       style="word-break: break-all;">
                  </div>
                </template>
                <span>{{detail.approveFlow.approver2}}</span>
              </a-popover>
            </div>

            <p class="third">
              <span v-if="detail.approveFlow.status2 === 'AGREE'">
                {{$common.timeToDate(detail.approveFlow.updationDate2)}} {{detail.approveFlow.opinion2}}</span>
              <span v-else-if="(detail.approveFlow.status2 === 'PENDING' || detail.approveFlow.status2 === 'APPROVING') && currentStatus === 1">滞留时长：{{detail.approveFlow.awaitTime2}}</span>
            </p>
          </div>
        </div>
        <!-- 不是项目管理员提交的审批，显示一级审批节点和二级审批节点 -->
        <div v-else
             class="three-step justify-center">

          <div class="item finish">
            <div class="index">1</div>
            <div class="first">审批发起</div>
            <p class="second">
              申请人：{{detail.approveFlow.createdBy}}
            </p>
            <p class="third">{{$common.timeToDate(detail.approveFlow.creationDate)}}</p>
          </div>
          <div class="item"
               :class="{'finish':currentStatus > 1, 'active' : currentStatus === 1}">
            <div class="line"></div>
            <div class="index">2</div>
            <p class="first">
              <span v-if="detail.approveFlow.status === 'PENDING'">待审批</span>
              <!--PENDING表示的是待审批，只是流程到当前节点时文案用‘审批中’-->
              <span v-else-if="detail.approveFlow.status === 'APPROVING'">待审批</span>
              <span v-else-if="detail.approveFlow.status === 'AGREE'">审批完成</span>
              <span v-else-if="detail.approveFlow.status === 'DISAGREE'">审批驳回</span>
              <span v-else-if="detail.approveFlow.status === 'CANCEL'">已撤销</span>
            </p>
            <p class="second">
              1级审批：
              <a-popover v-if="detail.approveFlow.updatedBy"
                         placement="topLeft">
                <template slot="content">
                  <div v-html="detail.approveFlow.updatedBy"
                       style="word-break: break-all;">
                  </div>
                </template>
                <span>{{detail.approveFlow.updatedBy}}</span>
              </a-popover>
              <a-popover v-else
                         placement="topLeft">
                <template slot="content">
                  <div v-html="detail.approveFlow.approver"
                       style="word-break: break-all;">
                  </div>
                </template>
                <span>{{detail.approveFlow.approver}}</span>
              </a-popover>
            </p>

            <p class="third">
              <span v-if="detail.approveFlow.status === 'AGREE' || detail.approveFlow.status === 'DISAGREE'">
                {{$common.timeToDate(detail.approveFlow.updationDate)}} {{detail.approveFlow.opinion}}</span>
              <span v-else-if="(detail.approveFlow.status === 'PENDING' || detail.approveFlow.status === 'APPROVING') && currentStatus === 1">滞留时长：{{detail.approveFlow.awaitTime}}</span>
            </p>
          </div>
          <div class="item"
               :class="{'finish':currentStatus > 2, 'active' : currentStatus === 2}">
            <div class="line"></div>
            <template>
              <img style="width:24px"
                   src="@/assets/icons/setp_success.png"
                   alt=""
                   v-if="detail.approveFlow.status2 === 'AGREE'">
              <img src="@/assets/icons/step_failure.png"
                   alt=""
                   v-else-if="detail.approveFlow.status2 === 'DISAGREE'">
              <div v-else
                   class="index">3</div>
            </template>
            <p class="first">
              <span v-if="detail.approveFlow.status2 === 'PENDING'"> </span>
              <span v-else-if="detail.approveFlow.status2 === 'APPROVING'">待审批</span>
              <span class="success"
                    v-else-if="detail.approveFlow.status2 === 'AGREE'">审批同意</span>
              <span class="fail"
                    v-else-if="detail.approveFlow.status2 === 'DISAGREE'">审批驳回</span>
              <span v-else-if="detail.approveFlow.status2 === 'CANCEL'">已撤销</span>
            </p>
            <p class="second">
              2级多人审批：
              <!-- 在一级审批节点时，不显示二级审批人 -->
              <template v-if="currentStatus >= 2">
                <a-popover v-if="detail.approveFlow.updatedBy2"
                           placement="topLeft">
                  <template slot="content">
                    <div v-html="detail.approveFlow.updatedBy2"
                         style="word-break: break-all;">
                    </div>
                  </template>
                  <span>{{detail.approveFlow.updatedBy2}}</span>
                </a-popover>
                <a-popover v-else
                           placement="topLeft">
                  <template slot="content">
                    <div v-html="detail.approveFlow.approver2"
                         style="word-break: break-all;">
                    </div>
                  </template>
                  <span>{{detail.approveFlow.approver2}}</span>
                </a-popover>
              </template>
            </p>

            <p class="third">
              <span v-if="detail.approveFlow.status2 === 'AGREE' || detail.approveFlow.status2 === 'DISAGREE'">
                {{$common.timeToDate(detail.approveFlow.updationDate2)}} {{detail.approveFlow.opinion2}}</span>
              <span v-else-if="(detail.approveFlow.status2 === 'PENDING' || detail.approveFlow.status2 === 'APPROVING') && currentStatus === 2">滞留时长：{{detail.approveFlow.awaitTime2}}</span>
            </p>
          </div>
        </div>
      </div>
      <div class="detail-item">
        <div class="title justify-start">
          <div class="line"></div>作业脚本预览
        </div>
        <job-version ref="jobVersion" />
      </div>
      <div v-if="detail.fileType == 'SQL'"
           class="detail-item">
        <div class="title justify-start">
          <div class="line"></div>元表
        </div>
        <div class="content">
          {{detail.metaTable}}
        </div>
      </div>
      <div class="detail-item">
        <div class="title justify-start">
          <div class="line"></div>描述
        </div>
        <div class="content">
          {{detail.description}}
        </div>
      </div>
      <div v-if="!isView"
           class="detail-item">
        <div class="title justify-start">
          <div class="line"></div>审批
        </div>
        <div class="content">
          <div class="opinion-wrapper">
            <div class="opinion justify-start">
              <p class="label">审批意见</p>
              <a-radio-group name="radioGroup"
                             v-model="opinionValue">
                <a-radio value="AGREE">
                  同意
                </a-radio>
                <a-radio value="DISAGREE">
                  不同意
                </a-radio>

              </a-radio-group>
            </div>
            <div class="opinion justify-start">
              <p class="label">审批详情</p>
              <a-textarea class="textarea"
                          :maxLength="200"
                          v-model="opinionText"></a-textarea>
            </div>
          </div>

        </div>
      </div>
      <div v-if="!isView"
           class="footer">
        <a-button :disabled="isSubmiting?true:false"
                  @click="submit"
                  type="primary"
                  size="small">
          提交
        </a-button>
        <a-button @click="back"
                  size="small"
                  style="margin-left:8px;">
          返回
        </a-button>

      </div>
    </div>
  </div>
</template>
<script>
  import jobVersion from '@/components/job-version/compare-noDialog'
  export default {
    data () {
      return {
        isLoading: false,
        detail: null,
        opinionValue: 'AGREE',
        opinionText: '',
        currentStatus: 1,
        isView: false,
        submitDebounce: null,
        isSubmiting: ''
      };
    },
    components: {
      jobVersion
    },
    async created () {
      this.isView = this.$route.query.type === 'view' ? true : false
      this.submitDebounce = this.$lodash.debounce(this.handleSubmit, 500)
    },
    async mounted () {

      let detail = await this.getData()
      if (detail.approveFlow.updatedBy) {//有一级审批人
        if (detail.approveFlow.updatedBy === 'system') {//项目管理员提交的审批一级自动审批，不显示一级审批节点
          if (!detail.approveFlow.updatedBy2) {//没有二级审批
            this.currentStatus = 1
          } else {
            this.currentStatus = 2
          }
        } else {
          if (!detail.approveFlow.updatedBy2) {//没有二级审批
            this.currentStatus = 2
          } else {
            this.currentStatus = 3
          }
        }
      } else {
        this.currentStatus = 1
      }
      this.detail = detail
      const fileId = Number(this.$route.query.fileId)
      this.$nextTick(() => {
        this.$refs['jobVersion'].open({ jobId: fileId, type: 'approve', fileType: this.detail.fileType })

      })

    },
    methods: {

      async getData () {
        const id = Number(this.$route.query.id)
        const fileId = Number(this.$route.query.fileId)
        const projectId = Number(this.$route.query.projectId)
        const params = {
          id: id,
          fileId: fileId,
          projectId: projectId
        }
        this.isLoading = true
        let res = await this.$http.post('/approve/detailApply', params)
        this.isLoading = false
        if (res.code === 0) {
          return res.data
        } else {
          this.$message.error(res.msg);
        }

      },
      async handleSubmit () {
        if (this.opinionValue === 'DISAGREE' && !this.opinionText) {
          this.$message.warning('请填写审批详情');
          return
        }
        const id = Number(this.$route.query.id)
        const fileId = Number(this.$route.query.fileId)
        const params = {
          id: id,
          fileId: fileId,
          status: this.opinionValue,
          opinion: this.opinionText
        }
        this.isSubmiting = true
        let res = await this.$http.post('/approve/executeApprove', params)
        this.isSubmiting = false
        if (res.code === 0) {
          this.$message.success('提交成功');
          this.$store.dispatch('approve/SET_APPROVE')//更新红点数
          this.$router.push({
            name: 'Approve',
            query: {//预留query
              active: 'approve'
            }
          })
        } else {
          this.$message.error(res.msg);
        }
      },
      async submit () {
        this.submitDebounce()
      },
      back () {
        this.$router.push({
          name: 'ApproveList',
          query: {//预留query
            active: this.$route.query.fromTab
          }
        })
      },
    }
  };
</script>

<style lang="scss" scoped>
  .detail-container {
    .success {
      color: #3cc422;
    }
    .fail {
      color: #f95353;
    }
    height: 100%;
    .detail-top {
      border-bottom: solid 1px #d9d9d9;
      h3 {
        width: 90%;
        margin: 0 auto;
        height: 52px;
        line-height: 52px;
        position: relative;

        font-weight: 600;
        span {
          display: flex;
          height: 100%;
          align-items: center;

          i {
            cursor: pointer;
            font-weight: 600;
            margin-right: 12px;
          }
        }
        em {
          font-style: normal;
          color: #ff9300;
          font-size: 12px;
          position: absolute;
          left: 50%;
          transform: translateX(-50%);
        }
      }
    }

    .detail-content {
      // width: 90%;
      // margin: 0 5%;
      // padding-bottom: 60px;
      // margin-bottom: 30px;
      padding: 0 5% 60px 5%;
      height: calc(100% - 52px);
      overflow-y: scroll;
      // &::-webkit-scrollbar {
      //   //整体样式
      //   height: 6px;
      //   width: 6px;
      // }
      // &::-webkit-scrollbar-thumb {
      //   //滑动滑块条样式
      //   border-radius: 6px;
      //   background: #ab90e8;
      //   height: 20px;
      // }
      // &::-webkit-scrollbar-track {
      //   //轨道的样式
      //   background-color: rgba(255, 255, 255, 0.75);
      // }
      .steps {
        margin: 20px auto 10px;
        width: 80%;
        .item {
          text-align: center;
          width: 320px;
          position: relative;
          display: flex;
          flex-direction: column;
          justify-content: flex-start;
          align-items: center;
          color: #999;
          font-size: 14px;
          &.finish {
            .index {
              border-color: #006eff;
              background-color: #006eff;
              color: #fff;
            }
            .line {
              border-color: #006eff;
            }
            .first {
              color: #333;
            }
          }
          .index {
            width: 24px;
            height: 24px;
            line-height: 24px;
            text-align: center;
            border-radius: 24px;
            border: solid 1px #cecece;
            overflow: hidden;
          }
          .first {
            font-size: 16px;
            font-weight: 600;
            margin: 6px 0 12px;
            height: 24px;
          }
          .second,
          .third {
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
            max-width: 80%;
            height: 24px;
          }
          .second {
            color: #2b2f37;
          }
          .line {
            width: 258px;
            position: absolute;
            left: -130px;
            top: 8px;
            height: 0;
            border-top: dashed 1px #006eff;
          }
        }
      }
      .detail-item {
        padding-top: 24px;
        .title {
          padding-left: 8px;
          font-size: 14px;
          font-weight: 600;
          margin-bottom: 8px;
          .line {
            height: 14px;
            width: 4px;
            background-color: #006eff;
            margin-right: 8px;
          }
        }
        .content {
          margin-left: 30px;
          word-break: break-all;
          .opinion-wrapper {
            .opinion {
              &:first-of-type {
                margin-bottom: 8px;
              }
              .label {
                margin-right: 16px;
              }
              .textarea {
                width: 480px;
                height: 81px;
                border: solid 1px #dddddd;
              }
            }
          }
        }
      }
      .footer {
        margin-top: 24px;
        height: 52px;
        padding-left: 90px;
      }
    }
  }
</style>;