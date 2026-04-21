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
      <div v-if="address && address.id" class="header-stats" style="margin-bottom: 24px;">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="服务人员"
                :value="address.advisor || '-'"
                :value-style="{ color: '#1890ff' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="到期时间"
                :value="formatMonth(address.terminationDate) || '-'"
                :value-style="{ color: '#f5222d' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="续费记录"
                :value="renewalRecords.length"
                suffix="条"
                :value-style="{ color: '#52c41a' }"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic
                title="续费总额"
                :value="renewalRecordsTotalAmount"
                prefix="¥"
                :precision="2"
                :value-style="{ color: '#fa8c16' }"
              />
            </a-card>
          </a-col>
        </a-row>
      </div>

      <a-tabs v-model="activeTab" :animated="false" type="card" @change="handleTabChange">
        <!-- 合同信息 -->
        <a-tab-pane key="contractInfo" tab="合同信息">
          <a-card :bordered="false" v-if="address && address.contractId">
            <a-spin :spinning="contractLoading">
              <a-empty v-if="!contractOrder" description="暂无合约信息" style="padding: 40px 0" />
              <div v-else>
                <!-- 基本信息卡片 -->
                <a-card title="基本信息" :bordered="false" style="margin-bottom: 16px;">
                  <a-descriptions bordered :column="2" size="middle">
                    <a-descriptions-item label="公司名称" :span="2">
                      <span style="font-size: 16px; font-weight: 600; color: #262626;">{{ contractOrder.companyName || address.companyName || '-' }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item label="订单编号">
                      <a-tag color="cyan" style="background-color: #13c2c2; color: #fff; font-weight: 600; padding: 4px 12px; border: none;">
                        {{ contractOrder.orderNo || '-' }}
                      </a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="业务人员">
                      <a-icon type="user" style="margin-right: 4px;" />{{ contractOrder.salesman || '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="业务类型">
                      {{ contractOrder.businessType_dictText || contractOrder.businessTypeName || contractOrder.businessType || '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="合同金额">
                      <span style="color: #f5222d; font-weight: 600; font-size: 16px;">¥{{ formatAmount(contractOrder.contractAmount || contractOrder.orderAmount || 0) }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item label="订单状态">
                      <a-tag :color="getOrderStatusColor(contractOrder.orderStatus)">
                        {{ getOrderStatusText(contractOrder) }}
                      </a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="下单时间">
                      {{ formatDateTime(contractOrder.createTime) || '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="审核状态">
                      <a-badge :status="getAuditStatusBadge(contractOrder.auditStatus)" :text="getAuditStatusText(contractOrder.auditStatus)" />
                    </a-descriptions-item>
                  </a-descriptions>
                </a-card>

                <!-- 地址信息卡片 -->
                <a-card title="地址信息" :bordered="false" style="margin-bottom: 16px;">
                  <a-descriptions bordered :column="2" size="middle">
                    <a-descriptions-item label="服务人员" :span="2">
                      <div class="service-person-block">
                        <div class="service-person-main">
                          <a-icon type="team" style="margin-right: 4px;" />
                          <span class="service-person-name">{{ address.advisor || '-' }}</span>
                          <a-button
                            type="link"
                            size="small"
                            icon="edit"
                            @click="showEditServicePerson"
                            style="padding: 0; height: auto;"
                          >
                            修改
                          </a-button>
                        </div>
                        <div v-if="sortedAuditSteps.length" class="audit-steps-box">
                          <div class="audit-steps-title">审核步骤（角色 / 人员）</div>
                          <div
                            v-for="step in sortedAuditSteps"
                            :key="'step-' + step.stepOrder"
                            class="audit-step-row"
                          >
                            <div class="audit-step-head">
                              <a-tag color="blue">第{{ step.stepOrder }}步</a-tag>
                              <a-tag :color="auditStepStatusColor(step.status)">{{ auditStepStatusText(step.status) }}</a-tag>
                            </div>
                            <div
                              v-for="(task, ti) in (step.tasks || [])"
                              :key="'task-' + step.stepOrder + '-' + ti + '-' + (task.id || ti)"
                              class="audit-task-line"
                            >
                              <a-icon type="solution" class="audit-task-icon" />
                              <span class="audit-role">{{ task.roleName || '审核角色' }}</span>
                              <span class="audit-sep">·</span>
                              <span class="audit-person">{{ formatAuditTaskPerson(task) }}</span>
                            </div>
                          </div>
                        </div>
                        <div
                          v-else-if="address && address.contractId && orderAuditProgressLoaded"
                          class="audit-steps-empty"
                        >
                          暂无审核任务记录
                        </div>
                      </div>
                    </a-descriptions-item>
                    <a-descriptions-item label="到期时间">
                      <span style="color: #f5222d; font-weight: 600;">{{ formatMonth(address.terminationDate) || '-' }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item label="地址成本">
                      <span style="color: #1890ff; font-weight: 600;">¥{{ formatAmount(address.addressCost || 0) }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item label="地址商">
                      {{ address.supplierCompany || '-' }}
                    </a-descriptions-item>
                  </a-descriptions>
                </a-card>
              </div>
            </a-spin>
          </a-card>
          <a-empty v-else description="该地址记录未关联订单，无法查看合同信息" style="padding: 40px 0" />
        </a-tab-pane>

        <!-- 续费操作 -->
        <a-tab-pane key="renewal" tab="续费操作">
          <a-card :bordered="false">
            <!-- 操作按钮 -->
            <div style="margin-bottom: 16px; text-align: right;">
              <a-button 
                type="primary" 
                icon="plus" 
                @click="showAddRenewalModal" 
                :disabled="!address || !address.id"
              >
                新增续费
              </a-button>
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
                <template v-if="text && text.trim()">
                  <div v-if="getImageList(text) && getImageList(text).length > 0" style="display: flex; flex-wrap: wrap; gap: 4px;">
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
                </template>
                <span v-else>-</span>
              </span>
              <span slot="remarks" slot-scope="text, record">
                <span v-if="text && text.trim()" :title="text" style="display: block; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ text }}
                </span>
                <span v-else>-</span>
              </span>
            </a-table>
          </a-card>
        </a-tab-pane>

        <!-- 报销申请 -->
        <a-tab-pane key="reimbursement" tab="报销申请">
          <a-card :bordered="false" v-if="address">
            <a-alert
              v-if="!canReimburseThisYear"
              type="info"
              show-icon
              :message="reimbursementAlertMessage"
              :description="reimbursementAlertDescription"
              style="margin-bottom: 16px"
              :closable="false"
            />
            <a-form-model ref="reimbursementForm" :model="reimbursementModel" :rules="reimbursementRules" :labelCol="{ span: 5 }" :wrapperCol="{ span: 19 }">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-model-item label="公司名称">
                    <a-input v-model="address.companyName" disabled />
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="地址成本">
                    <a-input-number 
                      v-model="reimbursementModel.addressCost" 
                      placeholder="请输入地址成本" 
                      :min="0" 
                      :precision="2" 
                      style="width: 100%"
                      :disabled="!canReimburseThisYear"
                    />
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="报销类目" prop="category">
                    <j-category-select 
                      v-model="reimbursementModel.category" 
                      back="value" 
                      pcode="A02" 
                      placeholder="请选择报销类目" 
                      :disabled="!canReimburseThisYear"
                      loadTriggleChange
                      style="width: 100%"
                    />
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="报销金额" prop="totalPrice">
                    <a-input-number 
                      v-model="reimbursementModel.totalPrice" 
                      placeholder="请输入报销金额" 
                      :min="0" 
                      :precision="2" 
                      style="width: 100%"
                      :disabled="!canReimburseThisYear"
                    />
                  </a-form-model-item>
                </a-col>
              </a-row>
              <div style="text-align: right; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0;">
                <a-button @click="handleReimbursementCancel" style="margin-right: 8px;">取消</a-button>
                <a-button 
                  type="primary" 
                  @click="handleReimbursementSubmit" 
                  :loading="reimbursementLoading"
                  :disabled="!canReimburseThisYear"
                >
                  提交报销申请
                </a-button>
              </div>
            </a-form-model>
          </a-card>
          <a-empty v-else description="暂无地址信息" style="padding: 40px 0" />
        </a-tab-pane>

        <!-- 信息编辑 -->
        <a-tab-pane key="infoEdit" tab="信息编辑">
          <a-card :bordered="false" v-if="address">
            <a-form-model ref="infoEditForm" :model="infoEditModel" :rules="infoEditRules" :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-model-item label="服务状态" prop="serviceStatus">
                    <j-dict-select-tag
                      v-model="infoEditModel.serviceStatus"
                      placeholder="请选择服务状态"
                      dictCode="service_status"
                      style="width: 100%"
                    />
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="地址商户" prop="supplier">
                    <j-search-select-tag
                      v-model="infoEditModel.supplier"
                      placeholder="请选择地址商户"
                      dict="gh_supplier,supplier_company,id,del_flag=0"
                      :async="false"
                      @change="handleSupplierChange"
                      style="width: 100%"
                    />
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="联系人员">
                    <a-input v-model="infoEditModel.supplierContact" placeholder="选择地址商户后自动带出" disabled />
                  </a-form-model-item>
                </a-col>
                <a-col :span="12">
                  <a-form-model-item label="联系电话">
                    <a-input v-model="infoEditModel.supplierContactNumber" placeholder="选择地址商户后自动带出" disabled />
                  </a-form-model-item>
                </a-col>
                <a-col :span="24">
                  <a-form-model-item label="备注信息" :labelCol="{ span: 3 }" :wrapperCol="{ span: 21 }">
                    <a-textarea v-model="infoEditModel.remarks" rows="4" placeholder="选择地址商户后自动带出" disabled />
                  </a-form-model-item>
                </a-col>
              </a-row>
              <div style="text-align: right; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0;">
                <a-button @click="handleInfoEditCancel" style="margin-right: 8px;">取消</a-button>
                <a-button type="primary" @click="handleInfoEditSubmit" :loading="infoEditLoading">保存</a-button>
              </div>
            </a-form-model>
          </a-card>
          <a-empty v-else description="暂无地址信息" style="padding: 40px 0" />
        </a-tab-pane>

        <!-- 复购情况 -->
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

        <!-- 操作记录 -->
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
        <!-- 原地址信息展示 -->
        <a-card title="原地址信息" :bordered="false" style="margin-bottom: 16px;" v-if="address">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="公司名称">{{ address.companyName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="当前到期日期">
              <span style="color: #1890ff; font-weight: 600;">{{ formatMonth(address.terminationDate) || '-' }}</span>
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
import { getAction, httpAction, postAction, getFileAccessHttpUrl } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'AddressDetailDrawer',
  data() {
    return {
      title: '地址详情',
      width: 1100,
      visible: false,
      loading: false,
      activeTab: 'contractInfo',
      record: null,
      address: null,
      // 合约详情相关
      contractOrder: null,
      contractLoading: false,
      // 复购详情相关
      customerOrders: [],
      // 操作记录相关
      operationLogs: [],
      // 信息编辑相关
      infoEditModel: {},
      infoEditLoading: false,
      infoEditRules: {
        supplier: [{ required: true, message: '请选择地址商户!', trigger: 'change' }],
      },
      // 续费操作相关
      renewalRecords: [],
      addRenewalVisible: false,
      addRenewalLoading: false,
      renewalForm: {
        renewalType: '4', // 默认选择地址续费
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
        { title: '续费时间', align: 'center', dataIndex: 'renewalTime', width: 100, scopedSlots: { customRender: 'renewalTime' } },
        { title: '续费类型', align: 'center', dataIndex: 'detailType', width: 100, scopedSlots: { customRender: 'detailType' } },
        { title: '合同金额', align: 'center', dataIndex: 'amounts', width: 110, scopedSlots: { customRender: 'amounts' } },
        { title: '收款金额', align: 'center', dataIndex: 'amountReceived', width: 110, scopedSlots: { customRender: 'amountReceived' } },
        { title: '收款时间', align: 'center', dataIndex: 'paymentTime', width: 160, scopedSlots: { customRender: 'paymentTime' } },
        { title: '到期月份', align: 'center', dataIndex: 'postExpirationDate', width: 100, scopedSlots: { customRender: 'postExpirationDate' } },
        { title: '收款账户', align: 'center', dataIndex: 'collectionAccountNumber', width: 200, scopedSlots: { customRender: 'collectionAccount' } },
        { title: '审核状态', align: 'center', dataIndex: 'auditStatus', width: 100, scopedSlots: { customRender: 'auditStatus' } },
        { title: '凭证附件', align: 'center', dataIndex: 'vouchers', width: 160, scopedSlots: { customRender: 'vouchers' } },
        { title: '备注信息', align: 'center', dataIndex: 'remarks', width: 200, scopedSlots: { customRender: 'remarks' } },
      ],
      orderColumns: [
        { title: '订单编号', align: 'center', dataIndex: 'orderNo', width: 140, scopedSlots: { customRender: 'orderNo' } },
        { title: '下单时间', align: 'center', dataIndex: 'createTime', width: 160, scopedSlots: { customRender: 'createTime' } },
        { title: '业务人员', align: 'center', dataIndex: 'salesman', width: 100 },
        {
          title: '业务类型',
          align: 'center',
          dataIndex: 'businessType',
          width: 120,
          customRender: (text, record) => record.businessType_dictText || record.businessTypeName || text || '-',
        },
        { title: '合同金额', align: 'center', dataIndex: 'contractAmount', width: 110, scopedSlots: { customRender: 'contractAmount' } },
        { title: '订单状态', align: 'center', dataIndex: 'orderStatus', width: 100, scopedSlots: { customRender: 'orderStatus' } },
      ],
      renewalFormRules: {
        renewalType: [{ required: true, message: '请选择续费类型', trigger: 'change' }],
        expireMonth: [{ required: true, message: '请选择到期月份', trigger: 'change' }],
        amounts: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
        amountReceived: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
        paymentTime: [{ required: true, message: '请选择收款时间', trigger: 'change' }],
        collectionAccountCascader: [{ required: true, message: '请选择收款账户', trigger: 'change' }],
      },
      // 服务人员相关
      editServicePersonVisible: false,
      editServicePersonLoading: false,
      servicePersonValue: '',
      /** 关联订单审核进度（用于服务人员区域展示各步角色/人员） */
      orderAuditProgress: null,
      orderAuditProgressLoaded: false,
      // 报销申请相关
      reimbursementModel: {
        addressCost: null,
        category: '',
        totalPrice: null,
      },
      reimbursementLoading: false,
      reimbursementRules: {
        category: [{ required: true, message: '请选择报销类目!', trigger: 'change' }],
        totalPrice: [{ required: true, message: '请输入报销金额!', trigger: 'blur' }],
      },
      reimbursementRecords: [], // 报销记录列表
      // 图片预览相关
      previewVisible: false,
      previewImageUrl: '',
    }
  },
  computed: {
    // 判断当前年份是否可以报销
    canReimburseThisYear() {
      if (!this.address || !this.address.companyName) {
        return true
      }
      
      const currentYear = new Date().getFullYear()
      // 检查是否有当前年份的报销记录
      const hasCurrentYearReimbursement = this.reimbursementRecords.some(record => {
        if (!record.reimbursementTime) {
          return false
        }
        const reimbursementYear = new Date(record.reimbursementTime).getFullYear()
        return reimbursementYear === currentYear
      })
      
      return !hasCurrentYearReimbursement
    },
    // 报销提示信息
    reimbursementAlertMessage() {
      if (!this.canReimburseThisYear) {
        const currentYear = new Date().getFullYear()
        return `${currentYear}年已报销`
      }
      return ''
    },
    // 报销提示描述
    reimbursementAlertDescription() {
      if (!this.canReimburseThisYear) {
        const currentYear = new Date().getFullYear()
        const lastReimbursement = this.reimbursementRecords
          .filter(record => {
            if (!record.reimbursementTime) return false
            const reimbursementYear = new Date(record.reimbursementTime).getFullYear()
            return reimbursementYear === currentYear
          })
          .sort((a, b) => {
            return new Date(b.reimbursementTime) - new Date(a.reimbursementTime)
          })[0]
        
        if (lastReimbursement && lastReimbursement.reimbursementTime) {
          const dateStr = moment(lastReimbursement.reimbursementTime).format('YYYY-MM-DD')
          return `该地址在${currentYear}年${dateStr}已报销，每年只能报销一次，明年可再次报销。`
        }
        return `该地址在${currentYear}年已报销，每年只能报销一次，明年可再次报销。`
      }
      return ''
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
    customerOrdersTotalAmount() {
      if (!this.customerOrders || this.customerOrders.length === 0) return 0
      return this.customerOrders.reduce((sum, order) => {
        const amount = parseFloat(order.contractAmount || order.orderAmount || 0)
        return sum + (isNaN(amount) ? 0 : amount)
      }, 0)
    },
    customerOrdersAvgAmount() {
      if (!this.customerOrders || this.customerOrders.length === 0) return 0
      return this.customerOrdersTotalAmount / this.customerOrders.length
    },
    repurchaseOrders() {
      if (!this.customerOrders || this.customerOrders.length === 0) return []
      const currentOrderId = this.address && this.address.contractId
      return this.customerOrders.filter((o) => {
        if (currentOrderId && o.id === currentOrderId) return false
        // 如果地址表里有 orderNo，也排除同一订单号
        if (this.address && this.address.orderNo && o.orderNo === this.address.orderNo) return false
        return true
      })
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
    /** 审核步骤按 stepOrder 升序，便于展示流程 */
    sortedAuditSteps() {
      const steps = (this.orderAuditProgress && this.orderAuditProgress.steps) || []
      return [...steps].sort((a, b) => (a.stepOrder || 0) - (b.stepOrder || 0))
    },
  },
  methods: {
    open(record, activeTab = 'contractInfo') {
      this.record = record || null
      this.title = `地址详情（${(record && record.companyName) || ''}）`
      this.address = null
      this.orderAuditProgress = null
      this.orderAuditProgressLoaded = false
      // 先设置 activeTab，再打开抽屉
      this.activeTab = activeTab || 'contractInfo'
      this.visible = true
      this.loadAll()
    },
    close() {
      this.visible = false
    },
    async loadAll() {
      if (!this.record || !this.record.id) return
      this.loading = true
      try {
        await this.loadAddressDetail()
        // 并行加载其他数据
        await Promise.all([
          this.loadContractOrder(),
          this.loadOrderAuditProgress(),
          this.loadCustomerOrders(),
          this.loadOperationLogs(),
          this.loadRenewalRecords(),
          this.loadReimbursementRecords(),
        ])
      } finally {
        this.loading = false
      }
    },
    loadAddressDetail() {
      return getAction('/address/ghAddressCenter/queryById', { id: this.record.id })
        .then((res) => {
          if (res.success && res.result) {
            this.address = res.result
            // 初始化信息编辑表单数据
            this.initInfoEditModel()
          }
        })
        .catch(() => {})
    },
    initInfoEditModel() {
      if (!this.address) return
      this.infoEditModel = {
        id: this.address.id,
        serviceStatus: this.address.serviceStatus || '1',
        supplier: this.address.supplier || '',
        supplierCompany: this.address.supplierCompany || '',
        supplierContact: this.address.supplierContact || '',
        supplierContactNumber: this.address.supplierContactNumber || '',
        remarks: this.address.remarks || '',
      }
      // 如果已有地址商ID，加载地址商信息
      if (this.address.supplier) {
        this.loadSupplierInfo(this.address.supplier)
      }
      // 初始化报销申请表单（在loadAll中已经加载了报销记录，所以这里可以直接初始化）
      this.initReimbursementModel()
    },
    loadSupplierInfo(supplierId) {
      if (!supplierId) return
      getAction('/supplier/ghSupplier/queryById', { id: supplierId })
        .then((res) => {
          if (res.success && res.result) {
            const supplier = res.result
            // 填充地址商信息
            this.infoEditModel.supplierCompany = supplier.supplierCompany || ''
            this.infoEditModel.supplierContact = supplier.supplierContact || ''
            this.infoEditModel.supplierContactNumber = supplier.supplierContactNumber || ''
            this.infoEditModel.remarks = supplier.remarks || ''
          }
        })
        .catch((err) => {
          console.error('加载地址商信息失败', err)
        })
    },
    handleSupplierChange(value) {
      if (value) {
        // 根据选择的地址商ID获取详细信息并填充表单
        getAction('/supplier/ghSupplier/queryById', { id: value })
          .then((res) => {
            if (res.success && res.result) {
              const supplier = res.result
              // 自动填充地址商相关信息
              this.infoEditModel.supplierCompany = supplier.supplierCompany || ''
              this.infoEditModel.supplierContact = supplier.supplierContact || ''
              this.infoEditModel.supplierContactNumber = supplier.supplierContactNumber || ''
              this.infoEditModel.remarks = supplier.remarks || ''
              this.infoEditModel.supplier = value
            }
          })
          .catch((err) => {
            console.error('获取地址商信息失败', err)
            this.$message.error('获取地址商信息失败')
          })
      } else {
        // 清空地址商相关信息
        this.infoEditModel.supplierCompany = ''
        this.infoEditModel.supplierContact = ''
        this.infoEditModel.supplierContactNumber = ''
        this.infoEditModel.remarks = ''
        this.infoEditModel.supplier = ''
      }
    },
    initReimbursementModel() {
      if (!this.address) return
      
      // 如果有报销记录，从最新的报销记录中加载数据
      let latestRecord = null
      if (this.reimbursementRecords && this.reimbursementRecords.length > 0) {
        // 按创建时间排序，获取最新的记录
        const sortedRecords = [...this.reimbursementRecords].sort((a, b) => {
          const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
          const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
          return timeB - timeA
        })
        latestRecord = sortedRecords[0]
      }
      
      this.reimbursementModel = {
        addressCost: this.address.addressCost || null,
        category: latestRecord ? (latestRecord.category || '') : '',
        totalPrice: latestRecord ? (latestRecord.totalPrice || this.address.addressCost || null) : (this.address.addressCost || null),
      }
    },
    handleInfoEditSubmit() {
      this.$refs.infoEditForm.validate((valid) => {
        if (valid) {
          this.infoEditLoading = true
          const submitData = {
            id: this.infoEditModel.id,
            serviceStatus: this.infoEditModel.serviceStatus,
            supplier: this.infoEditModel.supplier,
            supplierCompany: this.infoEditModel.supplierCompany,
            supplierContact: this.infoEditModel.supplierContact,
            supplierContactNumber: this.infoEditModel.supplierContactNumber,
            remarks: this.infoEditModel.remarks,
          }
          
          httpAction('/address/ghAddressCenter/edit', submitData, 'PUT')
            .then((res) => {
              if (res.success) {
                this.$message.success(res.message || '保存成功')
                this.loadAddressDetail() // 重新加载地址详情
                this.$emit('ok') // 通知父组件刷新
              } else {
                this.$message.error(res.message || '保存失败')
              }
            })
            .catch((err) => {
              console.error('保存失败:', err)
              this.$message.error('保存失败：' + (err.message || '未知错误'))
            })
            .finally(() => {
              this.infoEditLoading = false
            })
        }
      })
    },
    handleInfoEditCancel() {
      // 重置表单数据
      this.initInfoEditModel()
      if (this.$refs.infoEditForm) {
        this.$refs.infoEditForm.resetFields()
      }
    },
    handleTabChange(activeKey) {
      // tab切换时的处理
      if (activeKey === 'contractInfo' && this.address && this.address.contractId) {
        this.loadContractOrder()
        this.loadOrderAuditProgress()
      } else if (activeKey === 'renewal' && this.address && this.address.id) {
        this.loadRenewalRecords()
        this.loadCollectionAccountOptions()
      } else if (activeKey === 'reimbursement' && this.address) {
        // 先加载报销记录，再初始化表单（确保能从记录中加载数据）
        this.loadReimbursementRecords().then(() => {
          this.initReimbursementModel()
        })
      } else if (activeKey === 'infoEdit' && this.address) {
        this.initInfoEditModel()
      } else if (activeKey === 'repurchase' && this.address) {
        this.loadCustomerOrders()
      } else if (activeKey === 'operationRecord' && this.address && this.address.contractId) {
        this.loadOperationLogs()
      }
    },
    loadContractOrder() {
      if (!this.address || !this.address.contractId) return Promise.resolve()
      this.contractLoading = true
      return getAction('/order/queryById', { id: this.address.contractId })
        .then((res) => {
          if (res.success && res.result) {
            this.contractOrder = res.result
          } else {
            this.contractOrder = null
          }
        })
        .catch(() => {
          this.contractOrder = null
        })
        .finally(() => {
          this.contractLoading = false
        })
    },
    loadOrderAuditProgress() {
      if (!this.address || !this.address.contractId) {
        this.orderAuditProgress = null
        this.orderAuditProgressLoaded = true
        return Promise.resolve()
      }
      return getAction('/sys/auditTask/getOrderAuditProgress', { orderId: this.address.contractId })
        .then((res) => {
          if (res.success && res.result) {
            this.orderAuditProgress = res.result
          } else {
            this.orderAuditProgress = null
          }
        })
        .catch(() => {
          this.orderAuditProgress = null
        })
        .finally(() => {
          this.orderAuditProgressLoaded = true
        })
    },
    formatAuditTaskPerson(task) {
      if (!task) return '-'
      const st = task.taskStatus
      const assigned = task.assignedUserName
      const auditor = task.auditUserName
      if (st === 'pending') {
        return assigned ? `待审：${assigned}` : '待审'
      }
      if (st === 'approved') {
        return auditor ? `已通过 · ${auditor}` : '已通过'
      }
      if (st === 'rejected') {
        return auditor ? `已驳回 · ${auditor}` : '已驳回'
      }
      return auditor || assigned || '-'
    },
    auditStepStatusColor(status) {
      const map = {
        approved: 'green',
        pending: 'orange',
        rejected: 'red',
        unknown: 'default',
      }
      return map[status] || 'default'
    },
    auditStepStatusText(status) {
      const map = {
        approved: '已通过',
        pending: '待审核',
        rejected: '已驳回',
        unknown: '—',
      }
      return map[status] || status || '—'
    },
    loadCustomerOrders() {
      if (!this.address) return Promise.resolve()
      const companyId = this.address.dataId || ''
      const companyName = this.address.companyName || ''
      if (!companyId && !companyName) return Promise.resolve()
      
      const params = {}
      if (companyId) params.companyId = companyId
      else if (companyName) params.companyName = companyName
      params.pageNo = 1
      params.pageSize = 1000
      
      return getAction('/order/list', params)
        .then((res) => {
          if (res.success && res.result && res.result.records) {
            this.customerOrders = res.result.records || []
          } else {
            this.customerOrders = []
          }
        })
        .catch(() => {
          this.customerOrders = []
        })
    },
    loadOperationLogs() {
      if (!this.address || !this.address.contractId) return Promise.resolve()
      return getAction('/order/getOperationLogs', { orderId: this.address.contractId })
        .then((res) => {
          if (res.success && res.result) {
            this.operationLogs = res.result || []
          } else {
            this.operationLogs = []
          }
        })
        .catch(() => {
          this.operationLogs = []
        })
    },
    getOrderStatusColor(status) {
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
      if (record.orderStatus_dictText) {
        return record.orderStatus_dictText
      }
      const statusMap = {
        '0': '待审核',
        '1': '进行中',
        '2': '已完成',
        '3': '已取消',
        '4': '已驳回'
      }
      return statusMap[record.orderStatus] || record.orderStatus || '-'
    },
    getAuditStatusBadge(status) {
      const map = {
        'pending': 'processing',
        'approved': 'success',
        'rejected': 'error',
      }
      return map[status] || 'default'
    },
    getAuditStatusText(status) {
      const map = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已驳回',
      }
      return map[status] || status || '-'
    },
    formatDateTime(value) {
      if (!value) return '-'
      return moment(value).format('YYYY-MM-DD HH:mm:ss')
    },
    showEditServicePerson() {
      if (!this.address || !this.address.id) {
        this.$message.warning('地址信息不完整')
        return
      }
      this.servicePersonValue = this.address.advisor || ''
      this.editServicePersonVisible = true
    },
    handleEditServicePersonOk() {
      if (!this.servicePersonValue) {
        this.$message.warning('请选择服务人员')
        return
      }
      this.editServicePersonLoading = true
      const submitData = {
        id: this.address.id,
        advisor: this.servicePersonValue,
      }
      
      httpAction('/address/ghAddressCenter/edit', submitData, 'PUT')
        .then((res) => {
          if (res.success) {
            this.$message.success(res.message || '修改成功')
            this.editServicePersonVisible = false
            this.loadAddressDetail() // 重新加载地址详情
            this.$emit('ok') // 通知父组件刷新
          } else {
            this.$message.error(res.message || '修改失败')
          }
        })
        .catch((err) => {
          console.error('修改服务人员失败:', err)
          this.$message.error('修改服务人员失败：' + (err.message || '未知错误'))
        })
        .finally(() => {
          this.editServicePersonLoading = false
        })
    },
    handleEditServicePersonCancel() {
      this.editServicePersonVisible = false
      this.servicePersonValue = this.address ? (this.address.advisor || '') : ''
    },
    loadReimbursementRecords() {
      if (!this.address || !this.address.dataId) {
        this.reimbursementRecords = []
        return Promise.resolve()
      }
      
      // 根据公司ID查询该地址的报销记录（地址费用类型）
      return getAction('/GhReimbursement/ghReimbursement/list', {
        companyId: this.address.dataId,
        searchStatus: '3', // 3表示只查询地址费用
        pageNo: 1,
        pageSize: 1000, // 查询所有记录
      })
        .then((res) => {
          if (res.success && res.result) {
            this.reimbursementRecords = res.result.records || []
          } else {
            this.reimbursementRecords = []
          }
        })
        .catch((err) => {
          console.error('加载报销记录失败:', err)
          this.reimbursementRecords = []
        })
    },
    handleReimbursementSubmit() {
      // 再次检查是否可以报销
      if (!this.canReimburseThisYear) {
        this.$message.warning('当前年份已报销，每年只能报销一次')
        return
      }
      
      this.$refs.reimbursementForm.validate((valid) => {
        if (valid) {
          this.reimbursementLoading = true
          const submitData = {
            id: this.address.id,
            addressCost: this.reimbursementModel.addressCost,
            category: this.reimbursementModel.category,
            totalPrice: this.reimbursementModel.totalPrice,
            dataId: this.address.dataId,
            companyName: this.address.companyName,
          }
          
          postAction('/address/ghAddressCenter/baoxiao', submitData)
            .then((res) => {
              if (res.success) {
                this.$message.success(res.message || '报销申请提交成功')
                this.loadAddressDetail() // 重新加载地址详情
                this.loadReimbursementRecords() // 重新加载报销记录
                // 重置表单
                this.initReimbursementModel()
                if (this.$refs.reimbursementForm) {
                  this.$refs.reimbursementForm.resetFields()
                }
                this.$emit('ok') // 通知父组件刷新
              } else {
                this.$message.error(res.message || '报销申请提交失败')
              }
            })
            .catch((err) => {
              console.error('报销申请提交失败:', err)
              this.$message.error('报销申请提交失败：' + (err.message || '未知错误'))
            })
            .finally(() => {
              this.reimbursementLoading = false
            })
        }
      })
    },
    handleReimbursementCancel() {
      // 重置表单数据
      this.initReimbursementModel()
      if (this.$refs.reimbursementForm) {
        this.$refs.reimbursementForm.resetFields()
      }
    },
    loadRenewalRecords() {
      if (!this.address || !this.address.id) return Promise.resolve()
      // 通过公司名称查询地址续费记录（因为地址续费记录可能没有addressId字段，需要通过公司名称匹配）
      return getAction('/renew/ghAddressRenew/list', { 
        companyName: this.address.companyName,
        detailType: '4', // 地址续费
        pageNo: 1,
        pageSize: 1000
      })
        .then((res) => {
          if (res.success && res.result && res.result.records) {
            // 过滤出当前地址的续费记录（通过公司名称匹配）
            this.renewalRecords = (res.result.records || []).filter(record => {
              return record.companyName === this.address.companyName
            })
          } else if (res.success && Array.isArray(res.result)) {
            this.renewalRecords = res.result.filter(record => {
              return record.companyName === this.address.companyName
            }) || []
          } else {
            this.renewalRecords = []
          }
        })
        .catch(() => {
          this.renewalRecords = []
        })
    },
    showAddRenewalModal() {
      if (!this.address || !this.address.id) {
        this.$message.warning('地址信息不完整')
        return
      }
      this.renewalForm = {
        renewalType: '4', // 默认选择地址续费
        expireMonth: '',
        amounts: '',
        amountReceived: '',
        collectionAccountCascader: [],
        paymentMethod: '',
        collectionAccountNumber: '',
        vouchers: '',
        remarks: '',
      }
      this.addRenewalVisible = true
      this.loadCollectionAccountOptions()
    },
    handleAddRenewalOk() {
      this.$refs.renewalFormRef.validate((valid) => {
        if (valid) {
          this.addRenewalLoading = true
          // 处理到期月份
          let endTime = ''
          if (this.renewalForm.expireMonth) {
            if (moment.isMoment(this.renewalForm.expireMonth)) {
              endTime = this.renewalForm.expireMonth.format('YYYY-MM-DD')
            } else if (typeof this.renewalForm.expireMonth === 'string') {
              // 如果是字符串格式 YYYY-MM，转换为 YYYY-MM-01
              if (this.renewalForm.expireMonth.match(/^\d{4}-\d{2}$/)) {
                endTime = this.renewalForm.expireMonth + '-01'
              } else {
                endTime = this.renewalForm.expireMonth
              }
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
          
          const params = {
            addressId: this.address.id,
            companyId: this.address.dataId || '',
            companyName: this.address.companyName,
            type: this.renewalForm.renewalType || '4', // 使用续费类型字段，默认为地址续费
            receivedAmout: this.renewalForm.amountReceived ? this.renewalForm.amountReceived.toString() : '',
            paymentTime: paymentTime,
            paymentMethod: this.renewalForm.paymentMethod || '',
            collectionAccountNumber: this.renewalForm.collectionAccountNumber || '',
            endTime: endTime,
            amounts: this.renewalForm.amounts ? this.renewalForm.amounts.toString() : '',
            vouchers: this.renewalForm.vouchers || '',
            remarks: this.renewalForm.remarks || '',
          }
          console.log('地址续费请求参数:', params)
          console.log('地址续费请求路径:', '/address/ghAddressCenter/renew')
          postAction('/address/ghAddressCenter/renew', params)
            .then((res) => {
              console.log('地址续费响应:', res)
              if (res.success) {
                this.$message.success('续费记录保存成功！')
                this.addRenewalVisible = false
                this.loadRenewalRecords()
                this.loadAddressDetail() // 重新加载地址详情，更新到期日期
                this.$emit('ok')
              } else {
                this.$message.error(res.message || '续费记录保存失败！')
              }
            })
            .catch((err) => {
              console.error('续费记录保存失败:', err)
              let errorMsg = '未知错误'
              if (err.response) {
                let responseMsg = '请求失败'
                if (err.response.data && err.response.data.message) {
                  responseMsg = err.response.data.message
                } else if (err.response.statusText) {
                  responseMsg = err.response.statusText
                }
                errorMsg = err.response.status + ': ' + responseMsg
              } else if (err.message) {
                errorMsg = err.message
              }
              this.$message.error('续费记录保存失败：' + errorMsg)
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
        renewalType: '4',
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
      })
      .catch(() => {
        this.collectionAccountOptions = []
      })
      .finally(() => {
        this.collectionAccountLoading = false
      })
    },
    handleCollectionAccountChange(value, selectedOptions) {
      if (value && value.length >= 3) {
        // value[0] 是收款方式，value[1] 是 "收款方式__收款单位/人"，value[2] 是账号ID
        this.renewalForm.paymentMethod = value[0] || ''
        this.renewalForm.collectionAccountNumber = value[2] || ''
      } else {
        this.renewalForm.paymentMethod = ''
        this.renewalForm.collectionAccountNumber = ''
      }
    },
    formatMonth(value) {
      if (!value) return '-'
      return moment(value).format('YYYY-MM')
    },
    formatDate(value) {
      if (!value) return '-'
      return moment(value).format('YYYY-MM-DD')
    },
    formatAmount(amount) {
      if (amount == null || amount === '') return '0.00'
      return parseFloat(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    handleViewOrder(orderNo) {
      // 查看订单详情（可以打开订单详情弹窗）
      if (!orderNo) return
      this.$message.info('查看订单详情功能：' + orderNo)
    },
    getTimelineColor(idx) {
      const colors = ['blue', 'green', 'red', 'orange', 'purple']
      return colors[idx % colors.length]
    },
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
      if (t.match(/[\u4e00-\u9fa5]/)) {
        return type
      }
      return map[t] || type
    },
    getOperationIcon(type) {
      const t = (type || '').toString().toLowerCase()
      const map = {
        create: 'plus-circle',
        add: 'plus-circle',
        update: 'edit',
        edit: 'edit',
        delete: 'delete',
        remove: 'delete',
        audit: 'audit',
        approve: 'check-circle',
        check: 'check-circle',
        renew: 'reload',
        renewal: 'reload',
        repurchase: 'shopping',
        loss: 'disconnect',
        lost: 'disconnect',
        refund: 'rollback',
        submit: 'upload',
      }
      return map[t] || 'file-text'
    },
    getOperationTagColor(type) {
      const t = (type || '').toString().toLowerCase()
      const map = {
        create: 'blue',
        add: 'blue',
        update: 'orange',
        edit: 'orange',
        delete: 'red',
        remove: 'red',
        audit: 'purple',
        approve: 'green',
        check: 'green',
        renew: 'cyan',
        renewal: 'cyan',
        repurchase: 'gold',
        loss: 'volcano',
        lost: 'volcano',
        refund: 'magenta',
        submit: 'blue',
      }
      return map[t] || 'default'
    },
    formatOperationContent(content) {
      if (!content) return ''
      // 如果是JSON字符串，尝试解析
      try {
        const parsed = JSON.parse(content)
        if (typeof parsed === 'object') {
          return JSON.stringify(parsed, null, 2)
        }
      } catch (e) {
        // 不是JSON，直接返回
      }
      return content
    },
    getOrderStatusTagStyle(orderStatus) {
      const status = (orderStatus || '').toString()
      const styleMap = {
        '0': { backgroundColor: '#d9d9d9', color: '#595959', border: 'none' },
        '1': { backgroundColor: '#52c41a', color: '#fff', border: 'none' },
        '2': { backgroundColor: '#faad14', color: '#fff', border: 'none' },
        '3': { backgroundColor: '#1890ff', color: '#fff', border: 'none' },
        '4': { backgroundColor: '#f5222d', color: '#fff', border: 'none' },
      }
      return styleMap[status] || {}
    },
    getImageList(imageVoucher) {
      // 将凭证字符串转换为图片数组
      if (!imageVoucher) {
        return []
      }
      // 如果包含中文字符或不是图片路径格式，可能是备注内容，返回空数组
      if (typeof imageVoucher === 'string') {
        // 检查是否是图片路径（包含文件扩展名或路径分隔符）
        const isImagePath = /\.(jpg|jpeg|png|gif|bmp|webp|svg)$/i.test(imageVoucher) || 
                           imageVoucher.includes('/') || 
                           imageVoucher.startsWith('http') ||
                           imageVoucher.startsWith('[')
        if (!isImagePath && /[\u4e00-\u9fa5]/.test(imageVoucher)) {
          // 包含中文且不是图片路径，可能是备注内容，返回空数组
          return []
        }
        // 如果是JSON数组字符串，尝试解析
        if (imageVoucher.trim().startsWith('[')) {
          try {
            const arr = JSON.parse(imageVoucher)
            if (Array.isArray(arr)) {
              return arr.filter(img => img && (typeof img === 'string' ? img.trim() : img))
            }
          } catch (e) {
            // 解析失败，继续处理
          }
        }
        // 如果是逗号分隔的字符串，转换为数组
        if (imageVoucher.includes(',')) {
          return imageVoucher.split(',').filter(img => img && img.trim())
        }
        // 单个字符串
        return [imageVoucher]
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
        // 如果已经是完整的URL，直接返回
        if (path.startsWith('http://') || path.startsWith('https://')) {
          return path
        }
        // 使用已导入的 getFileAccessHttpUrl 处理相对路径
        const url = getFileAccessHttpUrl(path)
        return url || path
      } catch (e) {
        console.error('处理图片URL失败:', e, path)
        return path
      }
    },
    previewImage(url) {
      // 预览图片
      if (!url) return
      this.previewImageUrl = url
      this.previewVisible = true
    },
  },
}
</script>

<style scoped>
.drawer-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  border-top: 1px solid #e8e8e8;
  padding: 10px 16px;
  text-align: right;
  left: 0;
  background: #fff;
  border-radius: 0 0 2px 2px;
  z-index: 10;
}

.operation-timeline {
  padding: 10px 0;
}

.operation-timeline-item {
  padding-bottom: 20px;
}

.operation-record-card {
  margin-left: 24px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 16px;
  position: relative;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-color: #1890ff;
  }
  
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
}

.operation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.operation-title {
  display: flex;
  align-items: center;
  flex: 1;
}

.operation-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #1890ff;
}

.operation-type-text {
  font-weight: 600;
  font-size: 15px;
  color: #262626;
}

.operation-tag {
  font-weight: 500;
  padding: 2px 8px;
}

.operation-meta {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #8c8c8c;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-icon {
  margin-right: 4px;
  font-size: 12px;
}

.meta-divider {
  margin: 0 12px;
  height: 12px;
}

.operation-content {
  margin-top: 8px;
}

.content-text {
  color: #595959;
  line-height: 1.8;
  font-size: 14px;
  word-break: break-word;
  white-space: pre-wrap;
}

.operation-timeline {
  padding: 10px 0;
}

.operation-timeline-item {
  padding-bottom: 20px;
}

.operation-record-card {
  margin-left: 24px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 16px;
  position: relative;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-color: #1890ff;
  }
  
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
}

.operation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.operation-title {
  display: flex;
  align-items: center;
  flex: 1;
}

.operation-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #1890ff;
}

.operation-type-text {
  font-weight: 600;
  font-size: 15px;
  color: #262626;
}

.operation-tag {
  font-weight: 500;
  padding: 2px 8px;
}

.operation-meta {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #8c8c8c;
  font-size: 13px;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  color: #8c8c8c;
}

.meta-icon {
  font-size: 14px;
  margin-right: 6px;
  color: #bfbfbf;
}

.meta-divider {
  margin: 0 12px;
  background-color: #e8e8e8;
  height: 12px;
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
}

.content-text {
  color: #595959;
  line-height: 1.8;
  font-size: 14px;
  word-break: break-word;
  white-space: pre-wrap;
}

.header-stats {
  .stat-card {
    text-align: center;
    transition: all 0.3s;
    border-radius: 4px;
    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
  }
}

::v-deep .ant-tabs-card {
  .ant-tabs-tab {
    padding: 8px 16px;
    font-size: 14px;
    margin-right: 4px;
  }
}

::v-deep .ant-descriptions-item-label {
  font-weight: 500;
  color: #595959;
  width: 100px;
}

::v-deep .ant-card-head-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

::v-deep .ant-table-small {
  font-size: 12px;
}

::v-deep .ant-table-small .ant-table-thead > tr > th {
  padding: 8px;
  background: #fafafa;
  font-weight: 600;
}

::v-deep .ant-table-small .ant-table-tbody > tr > td {
  padding: 8px;
}

.service-person-block {
  width: 100%;
}
.service-person-main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 8px;
}
.service-person-name {
  margin-right: 4px;
  font-weight: 500;
  color: #262626;
}
.audit-steps-box {
  margin-top: 12px;
  padding: 12px 14px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
.audit-steps-title {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
}
.audit-step-row {
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e8e8e8;
}
.audit-step-row:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.audit-step-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}
.audit-task-line {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  margin-left: 4px;
  margin-top: 4px;
  padding-left: 8px;
  border-left: 2px solid #e6f7ff;
  font-size: 13px;
  line-height: 1.5;
  color: #595959;
}
.audit-task-icon {
  margin-right: 6px;
  margin-top: 3px;
  color: #1890ff;
  flex-shrink: 0;
}
.audit-role {
  font-weight: 500;
  color: #262626;
}
.audit-sep {
  margin: 0 6px;
  color: #d9d9d9;
}
.audit-person {
  color: #595959;
}
.audit-steps-empty {
  margin-top: 8px;
  font-size: 12px;
  color: #bfbfbf;
}
</style>

