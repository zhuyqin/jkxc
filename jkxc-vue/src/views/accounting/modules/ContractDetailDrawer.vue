<template>
  <a-drawer
    :title="title"
    :width="width"
    placement="right"
    :closable="false"
    @close="close"
    destroyOnClose
    :visible="visible"
    :bodyStyle="{ padding: '24px', paddingBottom: '60px' }"
  >
    <a-spin :spinning="loading">
      <!-- 头部统计卡片 -->
      <div v-if="contract && contract.id" class="header-stats" style="margin-bottom: 24px;">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="累计服务"
                :value="contract.serviceMonths != null ? contract.serviceMonths : 0"
                suffix="个月"
                :value-style="{ color: '#1890ff' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="续费订单"
                :value="renewalOrders.length"
                suffix="单"
                :value-style="{ color: '#52c41a' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="复购订单"
                :value="repurchaseOrders.length"
                suffix="单"
                :value-style="{ color: '#fa8c16' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="操作记录"
                :value="operationLogs.length"
                suffix="条"
                :value-style="{ color: '#722ed1' }"
              />
            </a-card>
          </a-col>
        </a-row>
      </div>

      <a-tabs v-model="activeTab" :animated="false" type="card" @change="handleTabChange">
        <a-tab-pane key="contractInfo" tab="合同信息">
          <a-empty v-if="!contract || !contract.id" description="暂无合同信息" style="padding: 40px 0" />
          <div v-else>
            <!-- 基本信息卡片 -->
            <a-card title="基本信息" :bordered="false" style="margin-bottom: 16px;">
              <a-descriptions bordered :column="2" size="middle">
                <a-descriptions-item label="公司名称" :span="2">
                  <span style="font-size: 16px; font-weight: 600; color: #262626;">{{ contract.companyName || '-' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="合同编号">
                  <a-tag color="blue">{{ contract.contractNo || '-' }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="订单编号">
                  <a-tag color="cyan" style="background-color: #13c2c2; color: #fff; font-weight: 600; padding: 4px 12px; border: none;">{{ contract.orderNo || '-' }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="签单日期">
                  {{ formatDate(contract.signDate) || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="签单人员">
                  <a-icon type="user" style="margin-right: 4px;" />{{ contract.signer || contract.salesman || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="来源">
                  {{ contract.source_dictText || contract.source || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="到期日期">
                  <span style="color: #f5222d; font-weight: 600;">{{ formatMonth(contract.expireDate) || '-' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="状态">
                  <a-badge :status="getContractStatusBadge(contract.contractStatus)" :text="getContractStatusText(contract.contractStatus)" />
                </a-descriptions-item>
                <a-descriptions-item label="产品名称">
                  {{ contract.productName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="合同金额">
                  <span style="color: #f5222d; font-weight: 600; font-size: 16px;">¥{{ formatAmount(contract.contractAmount || contract.orderAmount || 0) }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="到款金额">
                  <span style="color: #52c41a; font-weight: 600;">¥{{ formatAmount(contract.paidAmount || 0) }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="续签状态">
                  <a-tag :color="contract.renewalStatus === '1' ? 'green' : 'default'" 
                         :style="contract.renewalStatus === '1' ? 'background-color: #52c41a; color: #fff; font-weight: 600; padding: 4px 12px; border: none;' : 'background-color: #d9d9d9; color: #595959; font-weight: 600; padding: 4px 12px; border: none;'">
                    {{ contract.renewalStatus === '1' ? '已续签' : '未续签' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="到期时间">
                  {{ formatMonth(contract.expireDate) || '-' }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 服务信息卡片 -->
            <a-card title="服务信息" :bordered="false" style="margin-bottom: 16px;">
              <a-descriptions bordered :column="2" size="middle">
                <a-descriptions-item label="服务人员">
                  <span style="display: flex; align-items: center;">
                    <a-icon type="team" style="margin-right: 4px;" />
                    <span style="margin-right: 8px;">{{ contract.servicePerson || '-' }}</span>
                    <a-button 
                      type="link" 
                      size="small" 
                      icon="edit" 
                      @click="showEditServicePerson" 
                      :disabled="isLossContract"
                      style="padding: 0; height: auto;"
                    >
                      修改
                    </a-button>
                    <a-tooltip v-if="isLossContract" title="已流失的合同不能修改服务人员">
                      <a-icon type="info-circle" style="margin-left: 4px; color: #999;" />
                    </a-tooltip>
                  </span>
                </a-descriptions-item>
                <a-descriptions-item label="累计服务">
                  <span style="color: #1890ff; font-weight: 600;">{{ contract.serviceMonths != null ? contract.serviceMonths + '个月' : '-' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="企业等级">
                  <a-tag :color="getEnterpriseLevelColor(contract.enterpriseLevel)" style="font-size: 14px;">
                    {{ contract.enterpriseLevel_dictText || contract.enterpriseLevel || '-' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="业务标签" :span="2">
                  <template v-if="contract.businessTag">
                    <a-tag v-for="(tag, idx) in contract.businessTag.split(',')" :key="idx" color="blue" style="margin-bottom: 4px;">
                      {{ tag }}
                    </a-tag>
                  </template>
                  <span v-else>-</span>
                </a-descriptions-item>
                <a-descriptions-item label="关联企业" :span="2">
                  {{ contract.relatedCompanyName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="产品名称">
                  {{ contract.businessType_dictText || contract.businessTypeName || contract.businessType || contract.productName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="合同编号">
                  <a-tag color="blue" style="background-color: #1890ff; color: #fff; font-weight: 600; padding: 4px 12px; border: none;">{{ contract.contractNo || '-' }}</a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 备注信息卡片 -->
            <a-card title="备注信息" :bordered="false" v-if="contract.remarks">
              <p style="margin: 0; color: #595959; line-height: 1.8;">{{ contract.remarks }}</p>
            </a-card>
          </div>
        </a-tab-pane>

        <a-tab-pane key="renewal" tab="续费情况">
          <a-card :bordered="false">
            <!-- 操作按钮 -->
            <div style="margin-bottom: 16px; text-align: right;"n>
              <a-button 
                type="primary" 
                icon="plus" 
                @click="showAddRenewalModal" 
                :disabled="!contract || !contract.id || isLossContract"
              >
                新增续费
              </a-button>
              <a-tooltip v-if="isLossContract" title="已流失的合同不能新增续费">
                <a-icon type="info-circle" style="margin-left: 8px; color: #999;" />
              </a-tooltip>
            </div>

            <!-- 统计信息 -->
            <div v-if="renewalRecords && renewalRecords.length > 0" style="margin-bottom: 16px; padding: 16px; background: #f5f5f5; border-radius: 4px;">
              <a-row :gutter="24">
                <a-col :span="8">
                  <a-statistic
                    title="续费记录数"
                    :value="renewalRecords.length"
                    suffix="条"
                    :value-style="{ color: '#1890ff', fontSize: '24px' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="续费总金额"
                    :value="renewalRecordsTotalAmount"
                    prefix="¥"
                    :precision="2"
                    :value-style="{ color: '#f5222d', fontSize: '24px' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="到款总金额"
                    :value="renewalRecordsReceivedAmount"
                    prefix="¥"
                    :precision="2"
                    :value-style="{ color: '#52c41a', fontSize: '24px' }"
                  />
                </a-col>
              </a-row>
            </div>
            
            <a-empty v-if="!renewalRecords || renewalRecords.length === 0" description="暂无续费记录" style="padding: 40px 0" />
            <a-table
              v-else
              size="middle"
              bordered
              rowKey="id"
              :columns="renewalColumns"
              :dataSource="renewalRecords"
              :pagination="false"
              :scroll="{ x: true }"
            >
              <span slot="renewalTime" slot-scope="text">
                {{ formatDate(text) }}
              </span>
              <span slot="detailType" slot-scope="text, record">
                <a-tag v-if="record.detailType_dictText" color="blue">{{ record.detailType_dictText }}</a-tag>
                <a-tag v-else-if="text" color="blue">{{ text }}</a-tag>
                <span v-else>-</span>
              </span>
              <span slot="amounts" slot-scope="text">
                <span v-if="text != null && text !== ''" style="color: #f5222d; font-weight: 600; font-size: 15px;">¥{{ formatAmount(text) }}</span>
                <span v-else>-</span>
              </span>
              <span slot="amountReceived" slot-scope="text">
                <span v-if="text != null && text !== ''" style="color: #52c41a; font-weight: 600; font-size: 15px;">¥{{ formatAmount(text) }}</span>
                <span v-else>-</span>
              </span>
              <span slot="paymentTime" slot-scope="text">
                {{ formatDateTime(text) || '-' }}
              </span>
              <span slot="postExpirationDate" slot-scope="text">
                {{ formatMonth(text) }}
              </span>
              <span slot="auditStatus" slot-scope="text, record">
                <a-tag v-if="text === '1'" color="green">已通过</a-tag>
                <a-tag v-else-if="text === '2'" color="red">已驳回</a-tag>
                <a-tag v-else color="orange">待审核</a-tag>
              </span>
              <span slot="collectionAccount" slot-scope="text, record">
                <span v-if="record.paymentMethod_dictText || record.payeePerson || record.accountNotes || record.collectionAccount">
                  {{ [record.paymentMethod_dictText, record.payeePerson, record.accountNotes, record.collectionAccount].filter(Boolean).join(' / ') }}
                </span>
                <span v-else-if="text">{{ text }}</span>
                <span v-else>--</span>
              </span>
              <span slot="vouchers" slot-scope="text, record">
                <div v-if="text" style="display: flex; flex-wrap: wrap; gap: 4px;">
                  <img
                    v-for="(img, index) in getImageList(text)"
                    :key="index"
                    :src="getImageUrl(img)"
                    alt="凭证图片"
                    style="width: 50px; height: 50px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                    @click="previewImage(getImageUrl(img))"
                  />
                </div>
                <span v-else>-</span>
              </span>
              <span slot="remarks" slot-scope="text">
                <span v-if="text && text.trim()" :title="text" style="display: block; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ text }}
                </span>
                <span v-else>-</span>
              </span>
            </a-table>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="repurchase" tab="复购情况">
          <a-card :bordered="false">
            <!-- 统计信息 -->
            <div v-if="repurchaseOrders && repurchaseOrders.length > 0" style="margin-bottom: 16px; padding: 16px; background: #f5f5f5; border-radius: 4px;">
              <a-row :gutter="24">
                <a-col :span="8">
                  <a-statistic
                    title="复购订单数"
                    :value="repurchaseOrders.length"
                    suffix="单"
                    :value-style="{ color: '#fa8c16', fontSize: '24px' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="复购总金额"
                    :value="repurchaseTotalAmount"
                    prefix="¥"
                    :precision="2"
                    :value-style="{ color: '#f5222d', fontSize: '24px' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="平均金额"
                    :value="repurchaseAvgAmount"
                    prefix="¥"
                    :precision="2"
                    :value-style="{ color: '#52c41a', fontSize: '24px' }"
                  />
                </a-col>
              </a-row>
            </div>

            <a-empty v-if="!repurchaseOrders || repurchaseOrders.length === 0" description="暂无复购情况" style="padding: 40px 0" />
            <a-table
              v-else
              size="middle"
              bordered
              rowKey="id"
              :columns="orderColumns"
              :dataSource="repurchaseOrders"
              :pagination="false"
              :scroll="{ x: true }"
            >
              <span slot="orderNo" slot-scope="text">
                <a-icon type="file-text" style="margin-right: 4px; color: #1890ff;" />
                <span style="color:#1890ff; font-weight: 500;">{{ text }}</span>
              </span>
              <span slot="createTime" slot-scope="text">
                {{ formatDateTime(text) }}
              </span>
              <span slot="contractAmount" slot-scope="text">
                <span v-if="text != null && text !== ''" style="color: #f5222d; font-weight: 600; font-size: 15px;">¥{{ formatAmount(text) }}</span>
                <span v-else>-</span>
              </span>
              <span slot="orderStatus" slot-scope="text, record">
                <a-tag :color="getOrderStatusColor(record.orderStatus)" :style="getOrderStatusTagStyle(record.orderStatus)">
                  {{ getOrderStatusText(record) }}
                </a-tag>
              </span>
            </a-table>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="lossApply" tab="流失申请">
          <a-card :bordered="false">
            <!-- 已提交流失申请：显示流失原因和审批流程 -->
            <template v-if="hasLossApplication">
              <a-alert
                type="info"
                show-icon
                message="流失申请已提交"
                description="流失申请已提交，正在等待审核。审核通过后，该合同将被标记为流失客户。"
                style="margin-bottom: 24px"
              />
              
              <!-- 流失原因展示 -->
              <a-card title="流失原因" :bordered="false" style="margin-bottom: 24px;">
                <a-descriptions :column="1" size="small" bordered>
                  <a-descriptions-item label="流失类型">
                    <a-tag :color="lossReasons.lossType === 'abnormal' ? 'red' : 'orange'">
                      {{ lossReasons.lossType === 'abnormal' ? '非正常流失' : '正常流失' }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item label="正常流失原因" v-if="lossReasons.normalReasons && lossReasons.normalReasons.length > 0">
                    <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                      <a-tag v-for="(reasonId, idx) in lossReasons.normalReasons" :key="idx" color="blue">
                        {{ getLossReasonText(reasonId, 'normal') }}
                      </a-tag>
                    </div>
                  </a-descriptions-item>
                  <a-descriptions-item label="非正常流失原因" v-if="lossReasons.abnormalReasons && lossReasons.abnormalReasons.length > 0">
                    <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                      <a-tag v-for="(reasonId, idx) in lossReasons.abnormalReasons" :key="idx" color="red">
                        {{ getLossReasonText(reasonId, 'abnormal') }}
                      </a-tag>
                    </div>
                  </a-descriptions-item>
                  <a-descriptions-item label="补充说明" v-if="lossReasons.remark">
                    {{ lossReasons.remark }}
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
              
              <!-- 审批流程展示 -->
              <a-card title="审批流程" :bordered="false" v-if="lossAuditProgress && lossAuditProgress.steps">
                <a-timeline v-if="lossAuditProgress.steps && lossAuditProgress.steps.length > 0">
                  <a-timeline-item
                    v-for="(step, idx) in lossAuditProgress.steps"
                    :key="idx"
                    :color="getStepColor(step)"
                  >
                    <div style="margin-bottom: 8px;">
                      <strong>第{{ step.stepOrder }}步：</strong>
                      <template v-if="step.roleNames && step.roleNames.length > 0">
                        <a-tag v-for="(roleName, idx) in step.roleNames" :key="idx" color="blue" style="margin-right: 4px;">
                          {{ roleName }}
                        </a-tag>
                      </template>
                      <span v-else>{{ step.roleName || '-' }}</span>
                    </div>
                    <div v-if="step.tasks && step.tasks.length > 0">
                      <!-- 只显示流失审核任务，按角色分组避免重复 -->
                      <div v-for="(task, taskIdx) in step.tasks" :key="taskIdx" v-if="task.taskType === 'loss'" style="margin-bottom: 8px;">
                        <a-tag :color="getTaskStatusColor(task.taskStatus)">
                          {{ getTaskStatusText(task.taskStatus) }}
                        </a-tag>
                        <span v-if="task.auditUserName" style="margin-left: 8px;">
                          审核人：{{ task.auditUserName }}
                        </span>
                        <span v-if="task.auditTime" style="margin-left: 8px; color: #999;">
                          {{ formatDateTime(task.auditTime) }}
                        </span>
                        <div v-if="task.auditRemark" style="margin-top: 4px; color: #666;">
                          备注：{{ task.auditRemark }}
                        </div>
                        <div v-if="task.rejectReason" style="margin-top: 4px; color: #f5222d;">
                          驳回原因：{{ task.rejectReason }}
                        </div>
                      </div>
                    </div>
                    <div v-else style="color: #999;">
                      待审核
                    </div>
                  </a-timeline-item>
                </a-timeline>
                <a-empty v-else description="暂无审批流程信息" style="padding: 40px 0" />
              </a-card>
            </template>
            
            <!-- 未提交流失申请：显示申请表单 -->
            <template v-else>
              <a-alert
                v-if="contract && (contract.lossFlag === 1 || contract.lossFlag === '1')"
                type="warning"
                show-icon
                message="该合同已标记为流失客户"
                description="该合同已被标记为流失客户，合同状态已更新为「已终止」。如需恢复，请联系管理员。"
                style="margin-bottom: 24px"
              />
              <a-alert
                v-else
                type="info"
                show-icon
                message="流失申请说明"
                description="提交流失申请后，该合同将被标记为流失客户，合同状态将更新为「已终止」。此操作不可逆，请谨慎操作。"
                style="margin-bottom: 24px"
              />

              <a-card title="合同信息确认" :bordered="false" style="margin-bottom: 24px;" v-if="contract">
                <a-descriptions :column="2" size="small">
                  <a-descriptions-item label="公司名称">{{ contract.companyName || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="合同编号">{{ contract.contractNo || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="到期时间">{{ formatMonth(contract.expireDate) || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="累计服务">{{ contract.serviceMonths != null ? contract.serviceMonths + '个月' : '-' }}</a-descriptions-item>
                </a-descriptions>
              </a-card>
              <a-card title="流失原因选择" :bordered="false">
                <a-tabs v-model="lossTabKey">
                  <a-tab-pane key="normal" tab="正常流失">
                    <a-transfer
                      :dataSource="lossReasonOptions"
                      :targetKeys="lossReasonSelected"
                      :render="item => item.title"
                      :show-search="true"
                      :titles="['可选原因', '已选原因']"
                      @change="handleLossReasonChange"
                      :listStyle="{ width: '45%', height: '240px' }"
                    />
                  </a-tab-pane>
                  <a-tab-pane key="abnormal" tab="非正常流失">
                    <a-transfer
                      :dataSource="lossNoNormalOptions"
                      :targetKeys="lossNoNormalSelected"
                      :render="item => item.title"
                      :show-search="true"
                      :titles="['可选原因', '已选原因']"
                      @change="handleLossNoNormalChange"
                      :listStyle="{ width: '45%', height: '240px' }"
                    />
                  </a-tab-pane>
                </a-tabs>
                <a-form-model :model="lossForm" :labelCol="{ span: 4 }" :wrapperCol="{ span: 20 }" style="margin-top: 16px;">
                  <a-form-model-item label="补充说明">
                    <a-textarea
                      v-model="lossForm.remark"
                      :rows="3"
                      placeholder="请输入补充说明（可选）"
                      :maxLength="500"
                    />
                  </a-form-model-item>
                </a-form-model>
                <div style="text-align: center; padding: 12px 0;">
                  <a-button
                    type="danger"
                    size="large"
                    icon="disconnect"
                    :loading="lossApplying"
                    :disabled="!contract || !contract.id || isLossContract"
                    @click="handleSubmitLoss"
                    style="min-width: 200px;"
                  >
                    提交流失申请
                  </a-button>
                </div>
              </a-card>
            </template>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="operationRecord" tab="操作记录">
          <a-card :bordered="false" :bodyStyle="{ padding: '24px' }">
            <a-empty v-if="!operationLogs || operationLogs.length === 0" description="暂无操作记录" style="padding: 60px 0" />
            <a-timeline v-else mode="left" style="padding: 10px 0;" class="operation-timeline">
              <a-timeline-item
                v-for="(item, idx) in operationLogs"
                :key="idx"
                :color="getTimelineColor(idx)"
                class="operation-timeline-item"
              >
                <div class="operation-record-card">
                  <div class="operation-header">
                  <div class="operation-title">
                      <a-icon :type="getOperationIcon(getOperationTypeText(item.operationType || item.type))" class="operation-icon" />
                      <span class="operation-type-text">{{ getOperationTypeText(item.operationType || item.type) }}</span>
                    </div>
                    <a-tag :color="getOperationTagColor(getOperationTypeText(item.operationType || item.type))" class="operation-tag">
                      {{ getOperationTypeText(item.operationType || item.type) }}
                    </a-tag>
                  </div>
                  <div class="operation-meta">
                    <span class="meta-item">
                      <a-icon type="clock-circle" class="meta-icon" />
                      <span>{{ formatDateTime(item.operationTime || item.createTime) }}</span>
                    </span>
                    <a-divider type="vertical" class="meta-divider" />
                    <span class="meta-item">
                      <a-icon type="user" class="meta-icon" />
                      <span>{{ item.operatorName || item.createBy || '系统' }}</span>
                    </span>
                  </div>
                  <div
                    v-if="item.operationContent || item.content || item.operationDesc || item.remarks"
                    class="operation-content"
                  >
                    <div class="content-text">
                      {{ formatOperationContent(item.operationContent || item.content || item.operationDesc || item.remarks) }}
                    </div>
                  </div>
                </div>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <div class="drawer-footer">
      <a-button @click="close" style="margin-bottom: 0;">关闭</a-button>
    </div>

    <!-- 修改服务人员弹窗 -->
    <a-modal
      title="修改服务人员"
      :visible="editServicePersonVisible"
      :confirmLoading="editServicePersonLoading"
      @ok="handleEditServicePersonOk"
      @cancel="handleEditServicePersonCancel"
      :width="500"
    >
      <a-form-model-item label="服务人员" :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
        <j-search-select-tag
          placeholder="请选择服务人员"
          v-model="servicePersonValue"
          dict="sys_user,realname,realname,1=1 and del_flag = '0'"
          :async="false"
          style="width: 100%"
        />
      </a-form-model-item>
    </a-modal>

    <!-- 新增续费弹窗 -->
    <a-modal
      title="新增续费"
      :visible="addRenewalVisible"
      :confirmLoading="addRenewalLoading"
      @ok="handleAddRenewalOk"
      @cancel="handleAddRenewalCancel"
      :width="800"
    >
      <a-form-model ref="renewalFormRef" :model="renewalForm" :rules="renewalFormRules" :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
        <!-- 原合同信息展示 -->
        <a-card title="原合同信息" :bordered="false" style="margin-bottom: 16px;" v-if="contract">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="合同编号">{{ contract.contractNo || '-' }}</a-descriptions-item>
            <a-descriptions-item label="公司名称">{{ contract.companyName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="合同金额">
              <span style="color: #f5222d; font-weight: 600;">¥{{ formatAmount(contract.contractAmount || 0) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="到期时间">
              <span style="color: #1890ff; font-weight: 600;">{{ formatMonth(contract.expireDate) || '-' }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-form-model-item label="续费类型" prop="renewalType" required>
          <j-dict-select-tag
            v-model="renewalForm.renewalType"
            dictCode="detail_type"
            placeholder="请选择续费类型"
            type="list"
            :disabled="true"
            style="width: 100%"
          />
        </a-form-model-item>

        <a-form-model-item label="到期月份" prop="expireMonth" required>
          <a-month-picker
            v-model="renewalForm.expireMonth"
            placeholder="请选择到期月份"
            format="YYYY-MM"
            style="width: 100%"
            :getCalendarContainer="(triggerNode) => triggerNode.parentNode"
          />
        </a-form-model-item>

        <a-form-model-item label="合同金额" prop="amounts" required>
          <a-input-number
            v-model="renewalForm.amounts"
            placeholder="请输入合同金额"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </a-form-model-item>

        <a-form-model-item label="收款金额" prop="amountReceived" required>
          <a-input-number
            v-model="renewalForm.amountReceived"
            placeholder="请输入收款金额"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </a-form-model-item>

        <a-form-model-item label="收款时间" prop="paymentTime" required>
          <a-date-picker
            v-model="renewalForm.paymentTime"
            placeholder="请选择收款时间"
            format="YYYY-MM-DD HH:mm:ss"
            show-time
            style="width: 100%"
            :getCalendarContainer="(triggerNode) => triggerNode.parentNode"
          />
        </a-form-model-item>

        <a-form-model-item label="收款账户" prop="collectionAccountCascader" required>
          <a-cascader
            v-model="renewalForm.collectionAccountCascader"
            :options="collectionAccountOptions"
            placeholder="请选择收款方式/收款单位/人/网点名称"
            :loading="collectionAccountLoading"
            :change-on-select="false"
            :show-search="true"
            :field-names="{ label: 'label', value: 'value', children: 'children' }"
            @change="handleCollectionAccountChange"
            style="width: 100%"
            :getPopupContainer="(triggerNode) => triggerNode.parentNode"
            :popupStyle="{ minWidth: '600px', maxHeight: '400px' }"
          />
        </a-form-model-item>

        <a-form-model-item label="凭证附件" prop="vouchers">
          <j-image-upload v-model="renewalForm.vouchers" text="上传" :isMultiple="true" bizPath="renewal" />
          <div style="color: #999; margin-top: 4px; font-size: 12px;">支持上传多张图片作为续费凭证</div>
        </a-form-model-item>

        <a-form-model-item label="备注信息" prop="remarks">
          <a-textarea v-model="renewalForm.remarks" :rows="4" placeholder="请输入备注信息" :maxLength="500" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <!-- 图片预览弹窗 -->
    <a-modal :visible="previewVisible" :footer="null" @cancel="previewVisible = false" :width="800">
      <img alt="预览" style="width: 100%" :src="previewImageUrl" />
    </a-modal>
  </a-drawer>
</template>

<script>
import { getAction, postAction, getFileAccessHttpUrl } from '@/api/manage'
import { ajaxGetDictItems, getOrderAuditProgress } from '@/api/api'
import moment from 'moment'

export default {
  name: 'ContractDetailDrawer',
  data() {
    return {
      title: '合约详情',
      width: 1100,
      visible: false,
      loading: false,
      lossApplying: false,
      activeTab: 'contractInfo',
      record: null,
      contract: null,
      customerOrders: [],
      operationLogs: [],
      editServicePersonVisible: false,
      editServicePersonLoading: false,
      servicePersonValue: '',
      renewalRecords: [],
      renewalSubTab: 'current', // 续费子tab：current/t2/t3/lossAudit/lossCustomer
      addRenewalVisible: false,
      addRenewalLoading: false,
      previewVisible: false,
      previewImageUrl: '',
      // 流失申请相关
      lossTabKey: 'normal',
      lossReasonOptions: [],
      lossReasonSelected: [],
      lossNoNormalOptions: [],
      lossNoNormalSelected: [],
      lossForm: {
        remark: '',
      },
      lossApplying: false,
      // 流失申请状态
      hasLossApplication: false,
      lossAuditProgress: null,
      lossReasons: {
        lossType: '',
        normalReasons: [],
        abnormalReasons: [],
        remark: '',
      },
      renewalForm: {
        renewalType: '代账续费',
        expireMonth: '',
        amounts: '',
        amountReceived: '',
        paymentTime: null, // 收款时间
        collectionAccountCascader: [],
        paymentMethod: '',
        collectionAccountNumber: '',
        vouchers: '',
        remarks: '',
      },
      collectionAccountOptions: [],
      collectionAccountLoading: false,
      renewalColumns: [
        { title: '续费时间', align: 'center', dataIndex: 'renewalTime', width: 120, scopedSlots: { customRender: 'renewalTime' } },
        { title: '续费类型', align: 'center', dataIndex: 'detailType', width: 120, scopedSlots: { customRender: 'detailType' } },
        { title: '合同金额', align: 'center', dataIndex: 'amounts', width: 120, scopedSlots: { customRender: 'amounts' } },
        { title: '收款金额', align: 'center', dataIndex: 'amountReceived', width: 120, scopedSlots: { customRender: 'amountReceived' } },
        { title: '收款时间', align: 'center', dataIndex: 'paymentTime', width: 160, scopedSlots: { customRender: 'paymentTime' } },
        { title: '收款账户', align: 'center', dataIndex: 'collectionAccountNumber', width: 200, scopedSlots: { customRender: 'collectionAccount' } },
        { title: '到期月份', align: 'center', dataIndex: 'postExpirationDate', width: 120, scopedSlots: { customRender: 'postExpirationDate' } },
        { title: '审核状态', align: 'center', dataIndex: 'auditStatus', width: 120, scopedSlots: { customRender: 'auditStatus' } },
        { title: '凭证', align: 'center', dataIndex: 'vouchers', width: 150, scopedSlots: { customRender: 'vouchers' } },
        { title: '备注信息', align: 'center', dataIndex: 'remarks', width: 200 },
      ],
      renewalFormRules: {
        renewalType: [{ required: true, message: '请选择续费类型', trigger: 'change' }],
        expireMonth: [{ required: true, message: '请选择到期月份', trigger: 'change' }],
        amounts: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
        amountReceived: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
        paymentTime: [{ required: true, message: '请选择收款时间', trigger: 'change' }],
        collectionAccountCascader: [{ required: true, message: '请选择收款账户', trigger: 'change' }],
      },
      orderColumns: [
        { title: '订单编号', align: 'center', dataIndex: 'orderNo', width: 160, scopedSlots: { customRender: 'orderNo' } },
        { title: '下单时间', align: 'center', dataIndex: 'createTime', width: 180 },
        { title: '业务人员', align: 'center', dataIndex: 'salesman', width: 120 },
        {
          title: '业务类型',
          align: 'center',
          dataIndex: 'businessType',
          width: 160,
          customRender: (text, record) => record.businessType_dictText || record.businessTypeName || text || '-',
        },
        { title: '合同金额', align: 'center', dataIndex: 'contractAmount', width: 120, scopedSlots: { customRender: 'contractAmount' } },
        { title: '订单状态', align: 'center', dataIndex: 'orderStatus', width: 120, scopedSlots: { customRender: 'orderStatus' } },
      ],
    }
  },
  computed: {
    // 判断合同是否已流失
    isLossContract() {
      if (!this.contract) return false
      return this.contract.lossFlag === 1 || 
             this.contract.lossFlag === '1' || 
             this.contract.contractStatus === 'terminated'
    },
    renewalOrders() {
      if (!this.customerOrders || this.customerOrders.length === 0) return []
      return this.customerOrders.filter((o) => {
        const t = (o.businessType_dictText || o.businessTypeName || o.businessType || '').toString()
        return t.includes('续费')
      })
    },
    renewalRecordsTotalAmount() {
      if (!this.renewalRecords || this.renewalRecords.length === 0) return 0
      return this.renewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amounts || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    renewalRecordsReceivedAmount() {
      if (!this.renewalRecords || this.renewalRecords.length === 0) return 0
      return this.renewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amountReceived || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    // 当前续费（0-30天内）
    currentRenewalRecords() {
      if (!this.renewalRecords || this.renewalRecords.length === 0) return []
      const now = moment()
      const days30 = moment().add(30, 'days')
      return this.renewalRecords.filter(record => {
        if (!record.postExpirationDate) return false
        const expireDate = moment(record.postExpirationDate)
        return expireDate.isAfter(now) && expireDate.isSameOrBefore(days30)
      })
    },
    currentRenewalTotalAmount() {
      if (!this.currentRenewalRecords || this.currentRenewalRecords.length === 0) return 0
      return this.currentRenewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amounts || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    currentRenewalReceivedAmount() {
      if (!this.currentRenewalRecords || this.currentRenewalRecords.length === 0) return 0
      return this.currentRenewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amountReceived || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    // t-2（0-60天内）
    t2RenewalRecords() {
      if (!this.renewalRecords || this.renewalRecords.length === 0) return []
      const now = moment()
      const days60 = moment().add(60, 'days')
      return this.renewalRecords.filter(record => {
        if (!record.postExpirationDate) return false
        const expireDate = moment(record.postExpirationDate)
        return expireDate.isAfter(now) && expireDate.isSameOrBefore(days60)
      })
    },
    t2RenewalTotalAmount() {
      if (!this.t2RenewalRecords || this.t2RenewalRecords.length === 0) return 0
      return this.t2RenewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amounts || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    t2RenewalReceivedAmount() {
      if (!this.t2RenewalRecords || this.t2RenewalRecords.length === 0) return 0
      return this.t2RenewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amountReceived || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    // t-3（61-365天内）
    t3RenewalRecords() {
      if (!this.renewalRecords || this.renewalRecords.length === 0) return []
      const days61 = moment().add(61, 'days')
      const days365 = moment().add(365, 'days')
      return this.renewalRecords.filter(record => {
        if (!record.postExpirationDate) return false
        const expireDate = moment(record.postExpirationDate)
        return expireDate.isAfter(days61) && expireDate.isSameOrBefore(days365)
      })
    },
    t3RenewalTotalAmount() {
      if (!this.t3RenewalRecords || this.t3RenewalRecords.length === 0) return 0
      return this.t3RenewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amounts || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    t3RenewalReceivedAmount() {
      if (!this.t3RenewalRecords || this.t3RenewalRecords.length === 0) return 0
      return this.t3RenewalRecords.reduce((sum, record) => {
        const amount = parseFloat(record.amountReceived || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    repurchaseOrders() {
      if (!this.customerOrders || this.customerOrders.length === 0) return []
      const currentOrderId = this.contract && (this.contract.orderId || this.contract.order_id)
      return this.customerOrders.filter((o) => {
        if (currentOrderId && o.id === currentOrderId) return false
        // 如果合同表里有 orderNo，也排除同一订单号
        if (this.contract && this.contract.orderNo && o.orderNo === this.contract.orderNo) return false
        return true
      })
    },
    renewalTotalAmount() {
      if (!this.renewalOrders || this.renewalOrders.length === 0) return 0
      return this.renewalOrders.reduce((sum, order) => {
        const amount = parseFloat(order.contractAmount || order.orderAmount || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    renewalAvgAmount() {
      if (!this.renewalOrders || this.renewalOrders.length === 0) return 0
      return this.renewalTotalAmount / this.renewalOrders.length
    },
    repurchaseTotalAmount() {
      if (!this.repurchaseOrders || this.repurchaseOrders.length === 0) return 0
      return this.repurchaseOrders.reduce((sum, order) => {
        const amount = parseFloat(order.contractAmount || order.orderAmount || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    repurchaseAvgAmount() {
      if (!this.repurchaseOrders || this.repurchaseOrders.length === 0) return 0
      return this.repurchaseTotalAmount / this.repurchaseOrders.length
    },
  },
  methods: {
    async open(record, activeTab = 'contractInfo') {
      this.visible = true
      this.activeTab = activeTab
      this.record = record || null
      this.title = `合约详情（${(record && (record.companyName || record.contractNo)) || ''}）`
      this.contract = null
      this.customerOrders = []
      this.operationLogs = []
      this.renewalRecords = []
      await this.loadAll()
      // 如果直接打开续费tab，需要等待合同信息加载完成后再加载续费记录
      if (activeTab === 'renewal' && this.contract && this.contract.id) {
        await this.loadRenewalRecords()
      }
    },
    close() {
      this.visible = false
    },
    async loadAll() {
      if (!this.record || !this.record.id) return
      this.loading = true
      try {
        // 先加载合同详情，因为续费记录需要合同ID
        await this.loadContractDetail()
        // 然后并行加载其他数据（包含流失原因字典）
        await Promise.all([
          this.loadCustomerOrders(),
          this.loadOperationLogs(),
          this.loadRenewalRecords(),
          this.loadLossReasonDicts(),
        ])
        // 检查是否已提交流失申请
        await this.checkLossApplication()
      } finally {
        this.loading = false
      }
    },
    loadContractDetail() {
      return getAction('/order/accountingContract/queryById', { id: this.record.id })
        .then((res) => {
          if (res.success && res.result) {
            this.contract = res.result
          }
        })
        .catch(() => {})
    },
    loadCustomerOrders() {
      const companyId = (this.record && (this.record.customerId || this.record.companyId)) || ''
      const companyName = (this.record && this.record.companyName) || ''
      const baseParams = {}
      if (companyId) baseParams.companyId = companyId
      else if (companyName) baseParams.companyName = companyName
      return this.loadAllOrders(baseParams, 1, []).then((allOrders) => {
        this.customerOrders = allOrders || []
      })
    },
    // 加载流失原因字典
    loadLossReasonDicts() {
      const promises = []
      if (!this.lossReasonOptions || this.lossReasonOptions.length === 0) {
        promises.push(
          ajaxGetDictItems('lossReason', null)
            .then((res) => {
              if (res && res.success && Array.isArray(res.result)) {
                this.lossReasonOptions = res.result.map((item) => ({
                  key: item.value,
                  title: item.text,
                }))
              } else {
                this.lossReasonOptions = []
              }
            })
            .catch(() => {
              this.lossReasonOptions = []
            }),
        )
      }
      if (!this.lossNoNormalOptions || this.lossNoNormalOptions.length === 0) {
        promises.push(
          ajaxGetDictItems('lossNoNormalReason', null)
            .then((res) => {
              if (res && res.success && Array.isArray(res.result)) {
                this.lossNoNormalOptions = res.result.map((item) => ({
                  key: item.value,
                  title: item.text,
                }))
              } else {
                this.lossNoNormalOptions = []
              }
            })
            .catch(() => {
              this.lossNoNormalOptions = []
            }),
        )
      }
      return Promise.all(promises)
    },
    loadAllOrders(baseParams, pageNo, allOrders) {
      const params = {
        ...baseParams,
        pageNo,
        pageSize: 1000,
        ignoreDataPermission: true,
      }
      return getAction('/order/list', params).then((res) => {
        if (res.success && res.result && res.result.records) {
          const records = res.result.records || []
          allOrders = allOrders.concat(records)
          if (records.length === 1000 && res.result.total > allOrders.length) {
            return this.loadAllOrders(baseParams, pageNo + 1, allOrders)
          }
          return allOrders
        }
        return allOrders
      })
    },
    handleLossReasonChange(targetKeys) {
      this.lossReasonSelected = targetKeys
    },
    handleLossNoNormalChange(targetKeys) {
      this.lossNoNormalSelected = targetKeys
    },
    // 获取流失原因文本
    getLossReasonText(reasonId, type) {
      if (type === 'normal') {
        const reason = this.lossReasonOptions.find(item => item.key === reasonId)
        return reason ? reason.title : reasonId
      } else {
        const reason = this.lossNoNormalOptions.find(item => item.key === reasonId)
        return reason ? reason.title : reasonId
      }
    },
    // 格式化操作记录内容，将流失原因ID转换为中文
    formatOperationContent(content) {
      if (!content) return content
      
      // 如果流失原因字典还没有加载，直接返回原内容
      if ((!this.lossReasonOptions || this.lossReasonOptions.length === 0) && 
          (!this.lossNoNormalOptions || this.lossNoNormalOptions.length === 0)) {
        return content
      }
      
      let formattedContent = content
      
      // 替换正常流失原因ID为中文
      // 匹配格式：正常流失原因ID：zero_declare 或 正常流失原因ID：zero_declare,other_reason
      formattedContent = formattedContent.replace(/正常流失原因ID[：:]([^；;，,]+)/g, (match, reasonIds) => {
        const ids = reasonIds.split(/[,，]/).map(s => s.trim()).filter(Boolean)
        const chineseReasons = ids.map(id => this.getLossReasonText(id, 'normal'))
        return `正常流失原因：${chineseReasons.join('、')}`
      })
      
      // 替换非正常流失原因ID为中文
      formattedContent = formattedContent.replace(/非正常流失原因ID[：:]([^；;，,]+)/g, (match, reasonIds) => {
        const ids = reasonIds.split(/[,，]/).map(s => s.trim()).filter(Boolean)
        const chineseReasons = ids.map(id => this.getLossReasonText(id, 'abnormal'))
        return `非正常流失原因：${chineseReasons.join('、')}`
      })
      
      return formattedContent
    },
    // 获取步骤颜色
    getStepColor(step) {
      if (!step.tasks || step.tasks.length === 0) {
        return 'blue' // 待审核
      }
      const hasApproved = step.tasks.some(task => task.taskStatus === 'approved')
      const hasRejected = step.tasks.some(task => task.taskStatus === 'rejected')
      if (hasApproved) {
        return 'green' // 已通过
      } else if (hasRejected) {
        return 'red' // 已驳回
      }
      return 'blue' // 待审核
    },
    // 获取任务状态颜色
    getTaskStatusColor(status) {
      if (status === 'approved') return 'green'
      if (status === 'rejected') return 'red'
      return 'orange'
    },
    // 获取任务状态文本
    getTaskStatusText(status) {
      if (status === 'approved') return '已通过'
      if (status === 'rejected') return '已驳回'
      return '待审核'
    },
    // 检查是否已提交流失申请
    async checkLossApplication() {
      if (!this.contract || !this.contract.orderId) {
        this.hasLossApplication = false
        return
      }
      
      try {
        // 如果合同备注中包含"【流失申请】"，则认为已提交流失申请
        const hasLossInRemarks = this.contract.remarks && this.contract.remarks.includes('【流失申请】')
        
        if (hasLossInRemarks) {
          this.hasLossApplication = true
          // 解析流失原因（从合同备注中）
          this.parseLossReasons()
          // 加载审批流程
          await this.loadLossAuditProgress()
        } else {
          this.hasLossApplication = false
        }
      } catch (err) {
        console.error('检查流失申请状态失败', err)
        this.hasLossApplication = false
      }
    },
    // 解析流失原因（从合同备注中）
    parseLossReasons() {
      if (!this.contract || !this.contract.remarks) {
        return
      }
      
      const remarks = this.contract.remarks
      // 查找"【流失申请】"之后的内容
      const lossIndex = remarks.indexOf('【流失申请】')
      if (lossIndex === -1) {
        return
      }
      
      const lossInfo = remarks.substring(lossIndex)
      
      // 解析流失类型
      if (lossInfo.includes('类型：非正常流失')) {
        this.lossReasons.lossType = 'abnormal'
      } else if (lossInfo.includes('类型：正常流失')) {
        this.lossReasons.lossType = 'normal'
      }
      
      // 解析正常流失原因ID
      const normalMatch = lossInfo.match(/正常流失原因ID：([^；]+)/)
      if (normalMatch) {
        this.lossReasons.normalReasons = normalMatch[1].split(',').filter(Boolean)
      }
      
      // 解析非正常流失原因ID
      const abnormalMatch = lossInfo.match(/非正常流失原因ID：([^；]+)/)
      if (abnormalMatch) {
        this.lossReasons.abnormalReasons = abnormalMatch[1].split(',').filter(Boolean)
      }
      
      // 解析补充说明
      const remarkMatch = lossInfo.match(/补充说明：(.+)/)
      if (remarkMatch) {
        this.lossReasons.remark = remarkMatch[1].trim()
      }
    },
    // 加载流失审核流程进度
    async loadLossAuditProgress() {
      if (!this.contract || !this.contract.orderId) {
        return
      }
      
      try {
        const res = await getAction('/sys/auditTask/getOrderAuditProgress', { orderId: this.contract.orderId })
        if (res.success && res.result && res.result.steps) {
          // 显示所有步骤，即使某些步骤还没有流失审核任务
          // 处理每个步骤，只保留流失审核任务（taskType='loss'）
          const processedSteps = res.result.steps.map(step => {
            // 只保留流失审核任务
            const lossTasks = (step.tasks || []).filter(task => task.taskType === 'loss')
            
            // 合并同一步骤的多个角色任务（类似LossAuditList的处理）
            const roleNames = []
            const roleIds = []
            const mergedTasks = []
            
            if (lossTasks.length > 0) {
              lossTasks.forEach(task => {
                const roleName = task.roleName || task.currentRoleName
                const roleId = task.currentRoleId || task.current_role_id
                
                if (roleName && !roleNames.includes(roleName)) {
                  roleNames.push(roleName)
                }
                if (roleId && !roleIds.includes(roleId)) {
                  roleIds.push(roleId)
                }
                
                // 保留所有任务（用于显示审核历史）
                mergedTasks.push(task)
              })
            }
            
            // 即使没有流失审核任务，也返回步骤（显示为"待审核"）
            return {
              ...step,
              tasks: mergedTasks,
              roleNames: roleNames,
              roleIds: roleIds,
              roleName: roleNames.length > 0 ? roleNames.join('、') : (step.roleName || '-'), // 合并角色名称，如果没有则使用步骤的角色名称
            }
          })
          
          // 如果没有找到流失审核任务，返回空
          if (processedSteps.length === 0) {
            this.lossAuditProgress = null
            return
          }
          
          // 反转步骤顺序（流失审核是反向流程）
          // 先按原始步骤顺序排序（从大到小），然后反转
          const sortedSteps = [...processedSteps].sort((a, b) => b.stepOrder - a.stepOrder)
          
          // 不过滤任何步骤，显示所有3步
          // 流失审核流程：第1步（原第3步）-> 第2步（原第2步）-> 第3步（原第1步）
          const reversedSteps = sortedSteps.map((step, index) => {
            const reversedStep = { ...step }
            // 重新编号：从1开始
            reversedStep.stepOrder = index + 1
            return reversedStep
          })
          
          this.lossAuditProgress = {
            ...res.result,
            steps: reversedSteps,
            totalSteps: reversedSteps.length
          }
        }
      } catch (err) {
        console.error('加载流失审核流程失败', err)
      }
    },
    loadOperationLogs() {
      const orderId = (this.record && this.record.orderId) || (this.contract && this.contract.orderId)
      if (!orderId) return Promise.resolve()
      return getAction('/order/getOperationLogs', { orderId })
        .then((res) => {
          if (res.success && res.result) {
            this.operationLogs = res.result || []
          }
        })
        .catch(() => {})
    },
    // 提交流失申请（发起流失流程）
    handleSubmitLoss() {
      if (!this.contract || !this.contract.id) {
        this.$message.error('未找到合同信息')
        return
      }
      if (this.isLossContract) {
        this.$message.warning('该合同已流失，不能再次提交流失申请')
        return
      }
      const normalIds = this.lossReasonSelected || []
      const abnormalIds = this.lossNoNormalSelected || []
      if ((!normalIds || normalIds.length === 0) && (!abnormalIds || abnormalIds.length === 0)) {
        this.$message.error('请选择至少一个流失原因')
        return
      }
      const lossType = this.lossTabKey === 'abnormal' ? 'abnormal' : 'normal'
      const payload = {
        id: this.contract.id,
        lossType,
        normalReasons: normalIds,
        abnormalReasons: abnormalIds,
        remark: this.lossForm.remark,
      }
      this.lossApplying = true
      postAction('/order/accountingContract/applyLoss', payload)
        .then(async (res) => {
          if (res.success) {
            this.$message.success(res.message || '流失申请已提交，等待审核！')
            await this.loadContractDetail()
            await this.loadOperationLogs()
            // 重新检查流失申请状态
            await this.checkLossApplication()
            this.$emit('changed')
          } else {
            this.$message.error(res.message || '流失申请失败！')
          }
        })
        .catch((err) => {
          this.$message.error('流失申请失败：' + (err.message || '未知错误'))
        })
        .finally(() => {
          this.lossApplying = false
        })
    },
    loadRenewalRecords() {
      if (!this.contract || !this.contract.id) return Promise.resolve()
      return getAction('/order/accountingContract/getRenewalRecords', { contractId: this.contract.id })
        .then((res) => {
          if (res.success && res.result) {
            this.renewalRecords = res.result || []
          }
        })
        .catch(() => {})
    },
    handleTabChange(activeKey) {
      // 当切换到续费情况tab时，重新加载续费记录
      if (activeKey === 'renewal' && this.contract && this.contract.id) {
        this.loadRenewalRecords()
      }
    },
    handleRenewalSubTabChange(activeKey) {
      // 子tab切换时的处理逻辑
      this.renewalSubTab = activeKey
      // 如果切换到流失审核tab，加载流失审核进度
      if (activeKey === 'lossAudit' && this.contract && this.contract.orderId) {
        this.loadLossAuditProgress()
      }
    },
    getStepColor(status) {
      if (status === 'approved') return 'green'
      if (status === 'rejected') return 'red'
      return 'orange'
    },
    getStepStatusText(status) {
      if (status === 'approved') return '已通过'
      if (status === 'rejected') return '已驳回'
      return '待审核'
    },
    getStepColorForLossAudit(step) {
      if (!step.tasks || step.tasks.length === 0) return 'orange'
      // 如果所有任务都已通过，返回绿色
      const allApproved = step.tasks.every(task => task.taskStatus === 'approved')
      if (allApproved) return 'green'
      // 如果有任务被驳回，返回红色
      const hasRejected = step.tasks.some(task => task.taskStatus === 'rejected')
      if (hasRejected) return 'red'
      // 如果有任务已通过，返回蓝色
      const hasApproved = step.tasks.some(task => task.taskStatus === 'approved')
      if (hasApproved) return 'blue'
      return 'orange'
    },
    getStepStatusTextForLossAudit(step) {
      if (!step.tasks || step.tasks.length === 0) return '待审核'
      // 如果所有任务都已通过，返回已通过
      const allApproved = step.tasks.every(task => task.taskStatus === 'approved')
      if (allApproved) return '已通过'
      // 如果有任务被驳回，返回已驳回
      const hasRejected = step.tasks.some(task => task.taskStatus === 'rejected')
      if (hasRejected) return '已驳回'
      // 如果有任务已通过，返回部分通过
      const hasApproved = step.tasks.some(task => task.taskStatus === 'approved')
      if (hasApproved) return '部分通过'
      return '待审核'
    },
    getTaskStatusColor(status) {
      if (status === 'approved') return 'green'
      if (status === 'rejected') return 'red'
      return 'orange'
    },
    getTaskStatusText(status) {
      if (status === 'approved') return '已通过'
      if (status === 'rejected') return '已驳回'
      return '待审核'
    },
    getImageList(imageVoucher) {
      // 将凭证字符串转换为图片数组
      if (!imageVoucher) {
        return []
      }
      if (typeof imageVoucher === 'string') {
        // 如果是字符串，按逗号分割
        return imageVoucher.split(',').filter(img => img && img.trim())
      }
      if (Array.isArray(imageVoucher)) {
        // 如果已经是数组，直接返回
        return imageVoucher.filter(img => img && (typeof img === 'string' ? img.trim() : img))
      }
      return []
    },
    getImageUrl(path) {
      // 处理图片URL，如果是相对路径则转换为完整URL
      if (!path || (typeof path === 'string' && path.trim() === '')) {
        return ''
      }
      try {
        const url = getFileAccessHttpUrl(path)
        return url || path
      } catch (e) {
        console.error('处理图片URL失败:', e, path)
        return path
      }
    },
    previewImage(url) {
      // 预览图片
      this.previewImageUrl = url
      this.previewVisible = true
    },
    applyLoss() {
      if (!this.contract || !this.contract.id) return
      const that = this
      this.$confirm({
        title: '确认流失申请',
        content: `确定将合同“${this.contract.companyName || this.contract.contractNo}”标记为流失客户吗？`,
        onOk() {
          that.lossApplying = true
          return postAction('/order/accountingContract/applyLoss', { id: that.contract.id })
            .then((res) => {
              if (res.success) {
                that.$message.success(res.message || '流失申请成功！')
                that.loadContractDetail()
                that.$emit('changed')
              } else {
                that.$message.error(res.message || '流失申请失败！')
              }
            })
            .finally(() => {
              that.lossApplying = false
            })
        },
      })
    },
    formatMonth(value) {
      if (!value) return '-'
      return moment(value).format('YYYY-MM')
    },
    formatAmount(amount) {
      if (amount == null || amount === '') return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    getOrderStatusColor(status) {
      // 订单状态颜色映射
      const map = { 
        '0': 'orange',      // 待审核 - 橙色
        '1': 'blue',        // 进行中 - 蓝色
        '2': 'green',       // 已完成 - 绿色
        '3': 'volcano',     // 已取消 - 火山红
        '4': 'red'          // 已驳回 - 红色
      }
      return map[status] || 'default'
    },
    getOrderStatusText(record) {
      // 优先使用字典文本
      if (record.orderStatus_dictText) {
        return record.orderStatus_dictText
      }
      // 如果没有字典文本，使用状态映射
      const statusMap = {
        '0': '待审核',
        '1': '进行中',
        '2': '已完成',
        '3': '已取消',
        '4': '已驳回'
      }
      return statusMap[record.orderStatus] || record.orderStatus || '-'
    },
    getOrderStatusTagStyle(status) {
      // 为不同状态设置更明显的样式，确保字体颜色和背景色有对比
      const styleMap = {
        '0': { backgroundColor: '#fa8c16', color: '#fff', fontWeight: '600', padding: '2px 8px', border: 'none' }, // 待审核 - 橙色背景，白色文字
        '1': { backgroundColor: '#1890ff', color: '#fff', fontWeight: '600', padding: '2px 8px', border: 'none' }, // 进行中 - 蓝色背景，白色文字
        '2': { backgroundColor: '#52c41a', color: '#fff', fontWeight: '600', padding: '2px 8px', border: 'none' }, // 已完成 - 绿色背景，白色文字
        '3': { backgroundColor: '#fa541c', color: '#fff', fontWeight: '600', padding: '2px 8px', border: 'none' }, // 已取消 - 火山红背景，白色文字
        '4': { backgroundColor: '#f5222d', color: '#fff', fontWeight: '600', padding: '2px 8px', border: 'none' }  // 已驳回 - 红色背景，白色文字
      }
      return styleMap[status] || { backgroundColor: '#d9d9d9', color: '#595959', fontWeight: '600', padding: '2px 8px', border: 'none' }
    },
    formatDate(value) {
      if (!value) return '-'
      return moment(value).format('YYYY-MM-DD')
    },
    formatDateTime(value) {
      if (!value) return '-'
      return moment(value).format('YYYY-MM-DD HH:mm:ss')
    },
    getContractStatusBadge(status) {
      const map = {
        'draft': 'default',
        'signed': 'success',
        'executing': 'processing',
        'completed': 'success',
        'terminated': 'error',
      }
      return map[status] || 'default'
    },
    getContractStatusText(status) {
      const map = {
        'draft': '草稿',
        'signed': '已签订',
        'executing': '执行中',
        'completed': '已完成',
        'terminated': '已终止',
      }
      return map[status] || status || '未知'
    },
    getEnterpriseLevelColor(level) {
      const map = {
        '1': 'gold',
        '2': 'orange',
        '3': 'red',
        '4': 'purple',
        '5': 'cyan',
      }
      return map[level] || 'default'
    },
    getTimelineColor(idx) {
      const colors = ['blue', 'green', 'red', 'orange', 'purple']
      return colors[idx % colors.length]
    },
    // 操作类型中英文/代码统一转成中文显示
    getOperationTypeText(type) {
      const t = (type || '').toString().toLowerCase()
      if (!t) return '操作'
      const map = {
        create: '创建',
        add: '创建',
        update: '修改',
        edit: '修改',
        delete: '删除',
        remove: '删除',
        audit: '审核',
        approve: '审核',
        check: '审核',
        renew: '续费',
        renewal: '续费',
        repurchase: '复购',
        loss: '流失',
        lost: '流失',
        refund: '退单',
        submit: '提交',
      }
      // 如果本身就包含中文关键词，直接返回
      if (t.match(/[\u4e00-\u9fa5]/)) {
        return type
      }
      return map[t] || type || '操作'
    },
    getOperationIcon(type) {
      const map = {
        '创建': 'plus-circle',
        '编辑': 'edit',
        '删除': 'delete',
        '审核': 'check-circle',
        '续费': 'reload',
        '复购': 'shopping-cart',
        '流失': 'disconnect',
      }
      const t = (type || '').toString()
      for (const key in map) {
        if (t.includes(key)) return map[key]
      }
      return 'file-text'
    },
    getOperationTagColor(type) {
      const map = {
        '创建': 'blue',
        '编辑': 'blue', // 改为蓝色，确保对比度
        '修改': 'blue', // 改为蓝色，确保对比度
        '删除': 'red',
        '审核': 'green',
        '续费': 'orange',
        '复购': 'purple',
        '流失': 'volcano',
      }
      const t = (type || '').toString()
      for (const key in map) {
        if (t.includes(key)) return map[key]
      }
      return 'blue' // 默认返回蓝色，确保对比度
    },
    showEditServicePerson() {
      if (!this.contract || !this.contract.id) return
      this.servicePersonValue = this.contract.servicePerson || ''
      this.editServicePersonVisible = true
    },
    handleEditServicePersonOk() {
      if (!this.contract || !this.contract.id) return
      if (this.isLossContract) {
        this.$message.warning('已流失的合同不能修改服务人员')
        return
      }
      if (!this.servicePersonValue || this.servicePersonValue.trim() === '') {
        this.$message.warning('请输入服务人员')
        return
      }
      this.editServicePersonLoading = true
      postAction('/order/accountingContract/editServicePerson', {
        id: this.contract.id,
        servicePerson: this.servicePersonValue.trim(),
      })
        .then((res) => {
          if (res.success) {
            this.$message.success('修改服务人员成功！')
            this.editServicePersonVisible = false
            this.loadContractDetail()
            this.$emit('changed')
          } else {
            this.$message.error(res.message || '修改服务人员失败！')
          }
        })
        .catch(() => {
          this.$message.error('修改服务人员失败！')
        })
        .finally(() => {
          this.editServicePersonLoading = false
        })
    },
    handleEditServicePersonCancel() {
      this.editServicePersonVisible = false
      this.servicePersonValue = ''
    },
    showAddRenewalModal() {
      if (!this.contract || !this.contract.id) return
      if (this.isLossContract) {
        this.$message.warning('已流失的合同不能新增续费')
        return
      }
      // 初始化表单数据
      this.renewalForm = {
        renewalType: '代账续费',
        expireMonth: '',
        amounts: this.contract.contractAmount ? this.contract.contractAmount.toString() : '',
        amountReceived: '',
        paymentTime: null,
        collectionAccountCascader: [],
        paymentMethod: '',
        collectionAccountNumber: '',
        vouchers: '',
        remarks: '',
      }
      // 加载收款账户级联选项
      this.loadCollectionAccountOptions()
      this.addRenewalVisible = true
    },
    handleAddRenewalOk() {
      if (!this.contract || !this.contract.id) return
      this.$refs.renewalFormRef.validate((valid) => {
        if (valid) {
          this.addRenewalLoading = true
          // 处理到期月份格式
          let expireMonth = ''
          if (this.renewalForm.expireMonth) {
            if (moment.isMoment(this.renewalForm.expireMonth)) {
              expireMonth = this.renewalForm.expireMonth.format('YYYY-MM')
            } else if (typeof this.renewalForm.expireMonth === 'string') {
              expireMonth = this.renewalForm.expireMonth
            }
          }
          // 处理收款时间
          let paymentTime = ''
          if (this.renewalForm.paymentTime) {
            if (moment.isMoment(this.renewalForm.paymentTime)) {
              paymentTime = this.renewalForm.paymentTime.format('YYYY-MM-DD HH:mm:ss')
            } else if (typeof this.renewalForm.paymentTime === 'string') {
              paymentTime = this.renewalForm.paymentTime
            }
          }
          // 从级联选择器中提取收款方式和账号ID
          let paymentMethod = null
          let accountNumber = null
          if (this.renewalForm.collectionAccountCascader && this.renewalForm.collectionAccountCascader.length === 3) {
            paymentMethod = this.renewalForm.collectionAccountCascader[0]
            accountNumber = this.renewalForm.collectionAccountCascader[2] // 第三级是账号ID
          }
          const params = {
            contractId: this.contract.id,
            renewalType: this.renewalForm.renewalType,
            expireMonth: expireMonth,
            amounts: this.renewalForm.amounts ? this.renewalForm.amounts.toString() : '',
            amountReceived: this.renewalForm.amountReceived ? this.renewalForm.amountReceived.toString() : '',
            paymentTime: paymentTime,
            paymentMethod: paymentMethod,
            collectionAccountNumber: accountNumber,
            vouchers: this.renewalForm.vouchers,
            remarks: this.renewalForm.remarks,
          }
          postAction('/order/accountingContract/addRenewalRecord', params)
            .then((res) => {
              if (res.success) {
                this.$message.success('续费记录保存成功！')
                this.addRenewalVisible = false
                this.loadRenewalRecords()
                this.loadContractDetail() // 重新加载合同详情，更新到期日期
                this.$emit('changed')
              } else {
                this.$message.error(res.message || '续费记录保存失败！')
              }
            })
            .catch(() => {
              this.$message.error('续费记录保存失败！')
            })
            .finally(() => {
              this.addRenewalLoading = false
            })
        }
      })
    },
    handleAddRenewalCancel() {
      this.addRenewalVisible = false
      this.renewalForm = {
        renewalType: '代账续费',
        expireMonth: '',
        amounts: '',
        amountReceived: '',
        paymentTime: null,
        collectionAccountCascader: [],
        paymentMethod: '',
        collectionAccountNumber: '',
        vouchers: '',
        remarks: '',
      }
      if (this.$refs.renewalFormRef) {
        this.$refs.renewalFormRef.resetFields()
      }
    },
    loadCollectionAccountOptions() {
      // 加载所有收款账号，构建三级级联数据：收款方式 -> 收款单位/人 -> 网点名称
      this.collectionAccountLoading = true
      return getAction('/bank/ghBankManagement/listAll').then((res) => {
        if (res.success && res.result) {
          const accounts = Array.isArray(res.result) ? res.result : []
          
          // 按收款方式分组
          const paymentMethodMap = {}
          accounts.forEach(acc => {
            // 只要求有paymentMethod和payeePerson，accountNotes可以为空
            if (!acc.paymentMethod || !acc.payeePerson) {
              return // 跳过不完整的数据
            }
            
            if (!paymentMethodMap[acc.paymentMethod]) {
              paymentMethodMap[acc.paymentMethod] = {}
            }
            
            if (!paymentMethodMap[acc.paymentMethod][acc.payeePerson]) {
              paymentMethodMap[acc.paymentMethod][acc.payeePerson] = []
            }
            
            paymentMethodMap[acc.paymentMethod][acc.payeePerson].push({
              id: acc.id,
              accountNotes: acc.accountNotes || '',
              pic: acc.pic
            })
          })
          
          // 构建级联数据结构
          this.collectionAccountOptions = Object.keys(paymentMethodMap).map(paymentMethod => {
            const payeePersonMap = paymentMethodMap[paymentMethod]
            const payeePersonList = Object.keys(payeePersonMap).map(payeePerson => ({
              label: payeePerson,
              value: `${paymentMethod}__${payeePerson}`, // 使用分隔符组合作为value
              children: payeePersonMap[payeePerson].map(acc => ({
                label: acc.accountNotes || '（无网点名称）',
                value: acc.id, // 第三级保存的是账号ID
                accountId: acc.id,
                pic: acc.pic
              }))
            }))
            
            return {
              label: paymentMethod,
              value: paymentMethod,
              children: payeePersonList
            }
          })
        } else {
          this.collectionAccountOptions = []
        }
      }).catch(() => {
        this.collectionAccountOptions = []
        return Promise.resolve()
      }).finally(() => {
        this.collectionAccountLoading = false
      })
    },
    handleCollectionAccountChange(value, selectedOptions) {
      // 级联选择器变化时，保存账号ID和收款方式
      if (value && value.length === 3) {
        const accountId = value[2] // 第三级是账号ID
        const paymentMethod = value[0] // 第一级是收款方式
        this.renewalForm.collectionAccountNumber = accountId
        this.renewalForm.paymentMethod = paymentMethod
      } else {
        this.renewalForm.collectionAccountNumber = ''
        this.renewalForm.paymentMethod = ''
      }
    },
  },
}
</script>

<style lang="less" scoped>
.drawer-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  border-top: 1px solid #e8e8e8;
  padding: 12px 24px;
  text-align: right;
  left: 0;
  background: #fff;
  border-radius: 0 0 2px 2px;
  z-index: 10;
}

.header-stats {
  .stat-card {
    text-align: center;
    transition: all 0.3s;
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
  }
}

/deep/ .ant-tabs-card {
  .ant-tabs-tab {
    padding: 12px 24px;
    font-size: 15px;
    font-weight: 500;
  }
}

/deep/ .ant-descriptions-item-label {
  font-weight: 600;
  color: #595959;
  width: 120px;
}

/deep/ .ant-card-head-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

/deep/ .ant-timeline-item-head {
  width: 16px;
  height: 16px;
  left: 0;
}

/deep/ .ant-timeline-item-content {
  top: -4px;
}

/* 操作记录样式优化 */
.operation-timeline {
  .operation-timeline-item {
    margin-bottom: 24px;
    
    /deep/ .ant-timeline-item-head {
      width: 18px;
      height: 18px;
      border-width: 3px;
      box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.1);
    }
    
    /deep/ .ant-timeline-item-tail {
      border-left: 2px solid #e8e8e8;
      left: 8px;
    }
  }
}

.operation-record-card {
  margin-left: 32px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: -8px;
    top: 20px;
    width: 0;
    height: 0;
    border-top: 8px solid transparent;
    border-bottom: 8px solid transparent;
    border-right: 8px solid #fff;
  }
  
  &::after {
    content: '';
    position: absolute;
    left: -9px;
    top: 20px;
    width: 0;
    height: 0;
    border-top: 8px solid transparent;
    border-bottom: 8px solid transparent;
    border-right: 8px solid #e8e8e8;
  }
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    border-color: #1890ff;
    transform: translateX(4px);
    
    &::after {
      border-right-color: #1890ff;
    }
  }
}

.operation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.operation-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  
  .operation-icon {
    font-size: 18px;
    margin-right: 10px;
    color: #1890ff;
  }
  
  .operation-type-text {
    color: #262626;
    letter-spacing: 0.5px;
  }
}

.operation-tag {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  
  /* 确保所有标签的文字颜色为白色，背景色有足够的对比度 */
  ::v-deep .ant-tag {
    color: #fff !important;
    border-color: transparent !important;
    font-weight: 500 !important;
  }
  
  /* 针对不同颜色的标签，确保背景色和文字颜色对比度足够 */
  ::v-deep .ant-tag-blue {
    background-color: #1890ff !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-green {
    background-color: #52c41a !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-orange {
    background-color: #fa8c16 !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-red {
    background-color: #f5222d !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-purple {
    background-color: #722ed1 !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-cyan {
    background-color: #13c2c2 !important;
    color: #fff !important;
  }
  /* 确保所有通过 color 属性设置的标签都有正确的文字颜色 */
  /* Ant Design 的 color 属性会生成内联样式，需要强制覆盖 */
  ::v-deep .ant-tag[style*="background"] {
    color: #fff !important;
  }
  ::v-deep .ant-tag-volcano {
    background-color: #fa541c !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-gold {
    background-color: #faad14 !important;
    color: #fff !important;
  }
  ::v-deep .ant-tag-lime {
    background-color: #a0d911 !important;
    color: #1f1f1f !important; /* 浅色背景用深色文字 */
  }
  ::v-deep .ant-tag-geekblue {
    background-color: #2f54eb !important;
    color: #fff !important;
  }
  /* 确保所有通过 color 属性设置的标签都有正确的文字颜色 */
  ::v-deep .ant-tag[class*="ant-tag-"] {
    color: #fff !important;
  }
  /* 特殊处理：如果标签没有明确的颜色类，使用默认样式 */
  ::v-deep .ant-tag:not([class*="ant-tag-blue"]):not([class*="ant-tag-green"]):not([class*="ant-tag-orange"]):not([class*="ant-tag-red"]):not([class*="ant-tag-purple"]):not([class*="ant-tag-cyan"]):not([class*="ant-tag-volcano"]):not([class*="ant-tag-gold"]):not([class*="ant-tag-lime"]):not([class*="ant-tag-geekblue"]) {
    background-color: #1890ff !important;
    color: #fff !important;
  }
}

.operation-meta {
  display: flex;
  align-items: center;
  color: #8c8c8c;
  font-size: 13px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
  
  .meta-item {
    display: flex;
    align-items: center;
    color: #8c8c8c;
    
    .meta-icon {
      font-size: 14px;
      margin-right: 6px;
      color: #bfbfbf;
    }
  }
  
  .meta-divider {
    margin: 0 12px;
    background-color: #e8e8e8;
  }
}

.operation-content {
  margin-top: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  border-radius: 6px;
  border-left: 3px solid #1890ff;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: linear-gradient(180deg, #1890ff 0%, #40a9ff 100%);
    border-radius: 3px 0 0 3px;
  }
  
  .content-text {
    color: #595959;
    line-height: 1.8;
    font-size: 14px;
    word-break: break-word;
    white-space: pre-wrap;
  }
}
</style>



