<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="公司名称">
              <JInput
                v-model="queryParam.companyName"
                placeholder="请输入公司名称"
                type="like"
                allowClear
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="续费类型">
              <j-dict-select-tag
                v-model="queryParam.detailType"
                dictCode="detail_type"
                placeholder="请选择续费类型"
                allowClear
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="审核状态">
              <j-dict-select-tag
                v-model="queryParam.auditStatus"
                dictCode="audit_status"
                placeholder="请选择审核状态"
                allowClear
              />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span
              style="float: left; overflow: hidden"
              class="table-page-search-submitButtons"
            >
              <a-button type="primary" @click="searchQuery" icon="search">
                查询
              </a-button>
              <a-button
                type="primary"
                @click="searchReset"
                icon="reload"
                style="margin-left: 8px"
              >
                重置
              </a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- 类型切换按钮 -->
    <div style="margin-top: 16px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
      <a-radio-group 
        v-model="activeTab" 
        @change="handleTabChangeForRadio" 
        button-style="solid"
        style="margin-bottom: 0;"
      >
        <a-radio-button value="accounting">
          <a-icon type="file-text" />
          <span>代账续费</span>
        </a-radio-button>
        <a-radio-button value="address">
          <a-icon type="home" />
          <span>地址续费</span>
        </a-radio-button>
      </a-radio-group>
    </div>

    <!-- 代账续费内容 -->
    <div v-if="activeTab === 'accounting'">
      <a-tabs v-model="accountingSubTab" @change="handleAccountingSubTabChange" style="margin-bottom: 16px;">
        <a-tab-pane key="pending" tab="待审核">
            <a-table
              ref="table"
              size="middle"
              :scroll="{ x: true }"
              bordered
              rowKey="id"
              :columns="columns"
              :dataSource="dataSource"
              :pagination="ipagination"
              :loading="loading"
              class="j-table-force-nowrap"
              @change="handleTableChange"
            >
      <span slot="renewalTime" slot-scope="text">
        {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
      </span>
      <span slot="postExpirationDate" slot-scope="text">
        {{ text ? moment(text).format('YYYY-MM') : '-' }}
      </span>
      <span slot="amounts" slot-scope="text">
        <span v-if="text" style="color: #fa541c; font-weight: 600">
          ¥{{ formatAmount(text) }}
        </span>
        <span v-else>-</span>
      </span>
      <span slot="amountReceived" slot-scope="text">
        <span v-if="text" style="color: #52c41a; font-weight: 600">
          ¥{{ formatAmount(text) }}
        </span>
        <span v-else>-</span>
      </span>
      <span slot="detailType" slot-scope="text, record">
        {{ record.detailType_dictText || text || '-' }}
      </span>
      <span slot="auditStatus" slot-scope="text">
        <a-tag v-if="text === '1'" color="green">已通过</a-tag>
        <a-tag v-else-if="text === '2'" color="red">已驳回</a-tag>
        <a-tag v-else color="orange">待审核</a-tag>
      </span>
      <span slot="collectionAccount" slot-scope="text, record">
        <span
          v-if="
            record.paymentMethod ||
            record.payeePerson ||
            record.accountNotes ||
            record.collectionAccount
          "
        >
          {{
            [
              record.paymentMethod,
              record.payeePerson,
              record.accountNotes,
              record.collectionAccount,
            ]
              .filter(Boolean)
              .join(' / ')
          }}
        </span>
        <span v-else-if="text">{{ text }}</span>
        <span v-else>--</span>
      </span>
      <span slot="vouchers" slot-scope="text">
        <div
          v-if="text"
          style="display: flex; flex-wrap: wrap; gap: 6px; justify-content: center"
        >
          <img
            v-for="(img, index) in getImageList(text)"
            :key="index"
            :src="getImageUrl(img)"
            alt="凭证"
            style="width: 50px; height: 50px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
            @click="previewImage(getImageUrl(img))"
          />
        </div>
        <span v-else>--</span>
      </span>
      <span slot="action" slot-scope="text, record">
        <a-button
          type="link"
          size="small"
          @click="handleAudit(record)"
          v-if="!record.auditStatus || record.auditStatus === '0'"
        >
          <a-icon type="audit" />审核
        </a-button>
        <a-tag
          v-else
          :color="record.auditStatus === '1' ? 'green' : 'red'"
        >
          {{ record.auditStatus === '1' ? '已审核' : '已驳回' }}
        </a-tag>
      </span>
            </a-table>
          </a-tab-pane>
          <a-tab-pane key="completed" tab="已完成">
            <a-table
              ref="completedTable"
              size="middle"
              :scroll="{ x: true }"
              bordered
              rowKey="id"
              :columns="columns"
              :dataSource="completedDataSource"
              :pagination="completedPagination"
              :loading="completedLoading"
              class="j-table-force-nowrap"
              @change="handleCompletedTableChange"
            >
              <span slot="renewalTime" slot-scope="text">
                {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
              </span>
              <span slot="postExpirationDate" slot-scope="text">
                {{ text ? moment(text).format('YYYY-MM') : '-' }}
              </span>
              <span slot="amounts" slot-scope="text">
                <span v-if="text" style="color: #fa541c; font-weight: 600">
                  ¥{{ formatAmount(text) }}
                </span>
                <span v-else>-</span>
              </span>
              <span slot="amountReceived" slot-scope="text">
                <span v-if="text" style="color: #52c41a; font-weight: 600">
                  ¥{{ formatAmount(text) }}
                </span>
                <span v-else>-</span>
              </span>
              <span slot="detailType" slot-scope="text, record">
                {{ record.detailType_dictText || text || '-' }}
              </span>
              <span slot="auditStatus" slot-scope="text">
                <a-tag v-if="text === '1'" color="green">已通过</a-tag>
                <a-tag v-else-if="text === '2'" color="red">已驳回</a-tag>
                <a-tag v-else color="orange">待审核</a-tag>
              </span>
              <span slot="collectionAccount" slot-scope="text, record">
                <span
                  v-if="
                    record.paymentMethod ||
                    record.payeePerson ||
                    record.accountNotes ||
                    record.collectionAccount
                  "
                >
                  {{
                    [
                      record.paymentMethod,
                      record.payeePerson,
                      record.accountNotes,
                      record.collectionAccount,
                    ]
                      .filter(Boolean)
                      .join(' / ')
                  }}
                </span>
                <span v-else-if="text">{{ text }}</span>
                <span v-else>--</span>
              </span>
              <span slot="vouchers" slot-scope="text">
                <div
                  v-if="text"
                  style="display: flex; flex-wrap: wrap; gap: 6px; justify-content: center"
                >
                  <img
                    v-for="(img, index) in getImageList(text)"
                    :key="index"
                    :src="getImageUrl(img)"
                    alt="凭证"
                    style="width: 50px; height: 50px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                    @click="previewImage(getImageUrl(img))"
                  />
                </div>
                <span v-else>--</span>
              </span>
              <span slot="action" slot-scope="text, record">
                <a-tag
                  :color="record.auditStatus === '1' ? 'green' : 'red'"
                >
                  {{ record.auditStatus === '1' ? '已审核' : '已驳回' }}
                </a-tag>
              </span>
            </a-table>
          </a-tab-pane>
        </a-tabs>
    </div>

    <!-- 地址续费内容 -->
    <div v-if="activeTab === 'address'">
        <a-tabs v-model="addressSubTab" @change="handleAddressSubTabChange" style="margin-bottom: 16px">
          <a-tab-pane key="pending" tab="待审核">
            <a-table
              ref="addressTable"
              size="middle"
              :scroll="{ x: true }"
              bordered
              rowKey="id"
              :columns="columns"
              :dataSource="addressDataSource"
              :pagination="addressPagination"
              :loading="addressLoading"
              class="j-table-force-nowrap"
              @change="handleAddressTableChange"
            >
          <span slot="renewalTime" slot-scope="text">
            {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
          </span>
          <span slot="postExpirationDate" slot-scope="text">
            {{ text ? moment(text).format('YYYY-MM') : '-' }}
          </span>
          <span slot="amounts" slot-scope="text">
            <span v-if="text" style="color: #fa541c; font-weight: 600">
              ¥{{ formatAmount(text) }}
            </span>
            <span v-else>-</span>
          </span>
          <span slot="amountReceived" slot-scope="text">
            <span v-if="text" style="color: #52c41a; font-weight: 600">
              ¥{{ formatAmount(text) }}
            </span>
            <span v-else>-</span>
          </span>
          <span slot="detailType" slot-scope="text, record">
            {{ record.detailType_dictText || text || '-' }}
          </span>
          <span slot="auditStatus" slot-scope="text">
            <a-tag v-if="text === '1'" color="green">已通过</a-tag>
            <a-tag v-else-if="text === '2'" color="red">已驳回</a-tag>
            <a-tag v-else color="orange">待审核</a-tag>
          </span>
          <span slot="collectionAccount" slot-scope="text, record">
            <span
              v-if="
                record.paymentMethod ||
                record.payeePerson ||
                record.accountNotes ||
                record.collectionAccount
              "
            >
              {{
                [
                  record.paymentMethod,
                  record.payeePerson,
                  record.accountNotes,
                  record.collectionAccount,
                ]
                  .filter(Boolean)
                  .join(' / ')
              }}
            </span>
            <span v-else-if="text">{{ text }}</span>
            <span v-else>--</span>
          </span>
          <span slot="vouchers" slot-scope="text">
            <div
              v-if="text"
              style="display: flex; flex-wrap: wrap; gap: 6px; justify-content: center"
            >
              <img
                v-for="(img, index) in getImageList(text)"
                :key="index"
                :src="getImageUrl(img)"
                alt="凭证"
                style="width: 50px; height: 50px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                @click="previewImage(getImageUrl(img))"
              />
            </div>
            <span v-else>--</span>
          </span>
          <span slot="action" slot-scope="text, record">
            <a-button
              type="link"
              size="small"
              @click="handleAudit(record)"
              v-if="!record.auditStatus || record.auditStatus === '0'"
            >
              <a-icon type="audit" />审核
            </a-button>
            <a-tag
              v-else
              :color="record.auditStatus === '1' ? 'green' : 'red'"
            >
              {{ record.auditStatus === '1' ? '已审核' : '已驳回' }}
            </a-tag>
          </span>
            </a-table>
          </a-tab-pane>
          <a-tab-pane key="completed" tab="已完成">
            <a-table
              ref="completedAddressTable"
              size="middle"
              :scroll="{ x: true }"
              bordered
              rowKey="id"
              :columns="columns"
              :dataSource="completedAddressDataSource"
              :pagination="completedAddressPagination"
              :loading="completedAddressLoading"
              class="j-table-force-nowrap"
              @change="handleCompletedAddressTableChange"
            >
              <span slot="renewalTime" slot-scope="text">
                {{ text ? moment(text).format('YYYY-MM-DD') : '-' }}
              </span>
              <span slot="postExpirationDate" slot-scope="text">
                {{ text ? moment(text).format('YYYY-MM') : '-' }}
              </span>
              <span slot="amounts" slot-scope="text">
                <span v-if="text" style="color: #fa541c; font-weight: 600">
                  ¥{{ formatAmount(text) }}
                </span>
                <span v-else>-</span>
              </span>
              <span slot="amountReceived" slot-scope="text">
                <span v-if="text" style="color: #52c41a; font-weight: 600">
                  ¥{{ formatAmount(text) }}
                </span>
                <span v-else>-</span>
              </span>
              <span slot="detailType" slot-scope="text, record">
                {{ record.detailType_dictText || text || '-' }}
              </span>
              <span slot="auditStatus" slot-scope="text">
                <a-tag v-if="text === '1'" color="green">已通过</a-tag>
                <a-tag v-else-if="text === '2'" color="red">已驳回</a-tag>
                <a-tag v-else color="orange">待审核</a-tag>
              </span>
              <span slot="collectionAccount" slot-scope="text, record">
                <span
                  v-if="
                    record.paymentMethod ||
                    record.payeePerson ||
                    record.accountNotes ||
                    record.collectionAccount
                  "
                >
                  {{
                    [
                      record.paymentMethod,
                      record.payeePerson,
                      record.accountNotes,
                      record.collectionAccount,
                    ]
                      .filter(Boolean)
                      .join(' / ')
                  }}
                </span>
                <span v-else-if="text">{{ text }}</span>
                <span v-else>--</span>
              </span>
              <span slot="vouchers" slot-scope="text">
                <div
                  v-if="text"
                  style="display: flex; flex-wrap: wrap; gap: 6px; justify-content: center"
                >
                  <img
                    v-for="(img, index) in getImageList(text)"
                    :key="index"
                    :src="getImageUrl(img)"
                    alt="凭证"
                    style="width: 50px; height: 50px; object-fit: cover; cursor: pointer; border: 1px solid #d9d9d9; border-radius: 4px;"
                    @click="previewImage(getImageUrl(img))"
                  />
                </div>
                <span v-else>--</span>
              </span>
              <span slot="action" slot-scope="text, record">
                <a-tag
                  :color="record.auditStatus === '1' ? 'green' : 'red'"
                >
                  {{ record.auditStatus === '1' ? '已审核' : '已驳回' }}
                </a-tag>
              </span>
            </a-table>
          </a-tab-pane>
        </a-tabs>
    </div>

    <!-- 审核弹窗 -->
    <j-modal
      :visible.sync="auditModal.visible"
      :width="700"
      title="续费审核"
      @ok="handleAuditOk"
      @cancel="handleAuditCancel"
      :confirmLoading="auditModal.loading"
    >
      <a-form-model
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        :labelCol="{ span: 5 }"
        :wrapperCol="{ span: 18 }"
      >
        <a-form-model-item label="公司名称">
          <span style="font-weight: 600">
            {{ currentRecord.companyName || '-' }}
          </span>
        </a-form-model-item>
        <a-form-model-item label="续费类型">
          {{ currentRecord.detailType_dictText || currentRecord.detailType || '-' }}
        </a-form-model-item>
        <a-form-model-item label="应续费金额">
          <span style="color: #fa541c; font-weight: 600">
            ¥{{ formatAmount(currentRecord.amounts) }}
          </span>
        </a-form-model-item>
        <a-form-model-item label="到款金额">
          <span style="color: #52c41a; font-weight: 600">
            ¥{{ formatAmount(currentRecord.amountReceived) }}
          </span>
        </a-form-model-item>
        <a-form-model-item label="审核结果" prop="auditStatus" required>
          <a-radio-group v-model="auditForm.auditStatus">
            <a-radio value="1">通过</a-radio>
            <a-radio value="2">驳回</a-radio>
          </a-radio-group>
        </a-form-model-item>
        <a-form-model-item label="审核备注" prop="remark">
          <a-textarea
            v-model="auditForm.remark"
            :rows="4"
            placeholder="请输入审核备注"
            :maxLength="500"
          />
        </a-form-model-item>
      </a-form-model>
    </j-modal>

    <!-- 图片预览弹窗 -->
    <a-modal
      :visible="previewVisible"
      :footer="null"
      @cancel="previewVisible = false"
      :width="800"
    >
      <img alt="预览" style="width: 100%" :src="previewImageUrl" />
    </a-modal>
  </a-card>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { getAction, httpAction } from '@/api/manage'
import { getFileAccessHttpUrl } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'RenewalAuditList',
  mixins: [JeecgListMixin],
  data() {
    return {
      description: '续费审核列表',
      moment,
      activeTab: 'accounting', // 当前标签页：accounting-代账续费，address-地址续费
      accountingSubTab: 'pending', // 代账续费子标签页：pending-待审核，completed-已完成
      addressSubTab: 'pending', // 地址续费子标签页：pending-待审核，completed-已完成
      queryParam: {
        companyName: '',
        detailType: '',
        auditStatus: '0', // 默认显示待审核
      },
      // 已完成数据
      completedDataSource: [],
      completedPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      completedLoading: false,
      // 地址续费数据
      addressDataSource: [],
      addressPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      addressLoading: false,
      // 已完成地址续费数据
      completedAddressDataSource: [],
      completedAddressPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
      },
      completedAddressLoading: false,
      url: {
        list: '/renew/ghAddressRenew/list',
      },
      columns: [
        {
          title: '续费时间',
          align: 'center',
          dataIndex: 'renewalTime',
          width: 120,
          scopedSlots: { customRender: 'renewalTime' },
        },
        {
          title: '公司名称',
          align: 'center',
          dataIndex: 'companyName',
          width: 200,
        },
        {
          title: '续费类型',
          align: 'center',
          dataIndex: 'detailType',
          width: 120,
          scopedSlots: { customRender: 'detailType' },
        },
        {
          title: '应续费金额',
          align: 'center',
          dataIndex: 'amounts',
          width: 130,
          scopedSlots: { customRender: 'amounts' },
        },
        {
          title: '到款金额',
          align: 'center',
          dataIndex: 'amountReceived',
          width: 130,
          scopedSlots: { customRender: 'amountReceived' },
        },
        {
          title: '后到期月份',
          align: 'center',
          dataIndex: 'postExpirationDate',
          width: 130,
          scopedSlots: { customRender: 'postExpirationDate' },
        },
        {
          title: '收款账户',
          align: 'center',
          dataIndex: 'collectionAccountNumber',
          width: 220,
          scopedSlots: { customRender: 'collectionAccount' },
        },
        {
          title: '审核状态',
          align: 'center',
          dataIndex: 'auditStatus',
          width: 120,
          scopedSlots: { customRender: 'auditStatus' },
        },
        {
          title: '创建人',
          align: 'center',
          dataIndex: 'creator',
          width: 120,
        },
        {
          title: '创建时间',
          align: 'center',
          dataIndex: 'createTime',
          width: 160,
        },
        {
          title: '凭证',
          align: 'center',
          dataIndex: 'vouchers',
          width: 160,
          scopedSlots: { customRender: 'vouchers' },
        },
        {
          title: '备注',
          align: 'center',
          dataIndex: 'remarks',
          width: 200,
        },
        {
          title: '操作',
          align: 'center',
          dataIndex: 'action',
          width: 140,
          fixed: 'right',
          scopedSlots: { customRender: 'action' },
        },
      ],
      auditModal: {
        visible: false,
        loading: false,
      },
      currentRecord: {},
      auditForm: {
        auditStatus: '1',
        remark: '',
      },
      auditRules: {
        auditStatus: [
          { required: true, message: '请选择审核结果', trigger: 'change' },
        ],
      },
      previewVisible: false,
      previewImageUrl: '',
    }
  },
  methods: {
    // 查询按钮
    searchQuery() {
      if (this.activeTab === 'accounting') {
        if (this.accountingSubTab === 'pending') {
          this.loadData(1)
        } else if (this.accountingSubTab === 'completed') {
          this.completedPagination.current = 1
          this.loadCompletedData()
        }
      } else if (this.activeTab === 'address') {
        if (this.addressSubTab === 'pending') {
          this.addressPagination.current = 1
          this.loadAddressData()
        } else if (this.addressSubTab === 'completed') {
          this.completedAddressPagination.current = 1
          this.loadCompletedAddressData()
        }
      }
    },
    // 重写loadData方法，代账续费标签页排除地址续费
    loadData(arg) {
      if(!this.url.list){
        this.$message.error("请设置url.list属性!")
        return
      }
      //加载数据 若传入参数1则加载第一页的内容
      if (arg === 1) {
        this.ipagination.current = 1;
      }
      var params = this.getQueryParams();//查询条件
      // 代账续费标签页：排除地址续费（detailType='4'）
      if (this.activeTab === 'accounting') {
        // 如果用户选择了续费类型，且不是地址续费，则使用用户选择的值
        // 如果用户没有选择续费类型，则排除地址续费
        if (!params.detailType || params.detailType === '') {
          // 不设置detailType，但需要在后端或前端过滤掉detailType='4'
          // 这里我们通过设置一个特殊参数来标识
          params.excludeDetailType = '4'
        } else if (params.detailType === '4') {
          // 如果用户选择了地址续费，但在代账续费标签页，则清空
          params.detailType = ''
          params.excludeDetailType = '4'
        }
      }
      this.loading = true;
      getAction(this.url.list, params).then((res) => {
        if (res.success) {
          let records = res.result.records||res.result;
          // 前端再次过滤，确保代账续费标签页不显示地址续费
          if (this.activeTab === 'accounting' && Array.isArray(records)) {
            records = records.filter(item => item.detailType !== '4')
          }
          this.dataSource = records;
          if(res.result.total)
          {
            this.ipagination.total = res.result.total;
          }else{
            this.ipagination.total = 0;
          }
        }else{
          this.$message.warning(res.message)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    // 按钮切换处理（a-radio-group 的 change 事件）
    handleTabChangeForRadio(e) {
      // 由于使用了 v-model，activeTab 已经自动更新，直接使用 this.activeTab
      const activeKey = this.activeTab
      if (activeKey === 'accounting') {
        // 根据子标签页加载数据
        if (this.accountingSubTab === 'pending') {
          this.loadData(1)
        } else if (this.accountingSubTab === 'completed') {
          this.loadCompletedData()
        }
      } else if (activeKey === 'address') {
        // 根据子标签页加载数据
        if (this.addressSubTab === 'pending') {
          this.loadAddressData()
        } else if (this.addressSubTab === 'completed') {
          this.loadCompletedAddressData()
        }
      }
    },
    // Tab切换（保留用于其他可能的场景）
    handleTabChange(activeKey) {
      this.activeTab = activeKey
      if (activeKey === 'accounting') {
        // 根据子标签页加载数据
        if (this.accountingSubTab === 'pending') {
          this.loadData(1)
        } else if (this.accountingSubTab === 'completed') {
          this.loadCompletedData()
        }
      } else if (activeKey === 'address') {
        // 根据子标签页加载数据
        if (this.addressSubTab === 'pending') {
          this.loadAddressData()
        } else if (this.addressSubTab === 'completed') {
          this.loadCompletedAddressData()
        }
      }
    },
    // 代账续费子标签页切换
    handleAccountingSubTabChange(activeKey) {
      this.accountingSubTab = activeKey
      if (activeKey === 'pending') {
        this.loadData(1)
      } else if (activeKey === 'completed') {
        this.loadCompletedData()
      }
    },
    // 加载已完成数据
    loadCompletedData() {
      this.completedLoading = true
      // 查询已通过和已驳回的记录，分别查询然后合并
      const params1 = {
        pageNo: 1,
        pageSize: 1000, // 查询所有已通过的记录
        auditStatus: '1', // 已通过
      }
      const params2 = {
        pageNo: 1,
        pageSize: 1000, // 查询所有已驳回的记录
        auditStatus: '2', // 已驳回
      }
      
      // 添加查询条件
      if (this.queryParam.companyName) {
        params1.companyName = this.queryParam.companyName
        params2.companyName = this.queryParam.companyName
      }
      if (this.queryParam.detailType && this.queryParam.detailType !== '4') {
        params1.detailType = this.queryParam.detailType
        params2.detailType = this.queryParam.detailType
      }
      
      // 并行查询已通过和已驳回的记录
      Promise.all([
        getAction(this.url.list, params1),
        getAction(this.url.list, params2)
      ]).then(([res1, res2]) => {
        let allRecords = []
        
        if (res1.success && res1.result) {
          const records1 = res1.result.records || []
          // 排除地址续费
          allRecords = allRecords.concat(records1.filter(item => item.detailType !== '4'))
        }
        
        if (res2.success && res2.result) {
          const records2 = res2.result.records || []
          // 排除地址续费
          allRecords = allRecords.concat(records2.filter(item => item.detailType !== '4'))
        }
        
        // 按创建时间倒序排序
        allRecords.sort((a, b) => {
          const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
          const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
          return timeB - timeA
        })
        
        // 分页处理
        const total = allRecords.length
        const start = (this.completedPagination.current - 1) * this.completedPagination.pageSize
        const end = start + this.completedPagination.pageSize
        this.completedDataSource = allRecords.slice(start, end)
        this.completedPagination.total = total
      }).catch(() => {
        this.completedDataSource = []
        this.completedPagination.total = 0
      }).finally(() => {
        this.completedLoading = false
      })
    },
    // 已完成表格分页变化
    handleCompletedTableChange(pagination) {
      this.completedPagination.current = pagination.current
      this.completedPagination.pageSize = pagination.pageSize
      this.loadCompletedData()
    },
    // 加载地址续费数据（待审核）
    loadAddressData() {
      if (!this.url.list) {
        this.$message.error('请设置url.list属性!')
        return
      }
      this.addressLoading = true
      const params = {
        pageNo: this.addressPagination.current,
        pageSize: this.addressPagination.pageSize,
        detailType: '4', // 地址续费
        auditStatus: '0', // 待审核
      }
      // 添加查询条件
      if (this.queryParam.companyName) {
        params.companyName = this.queryParam.companyName
      }
      console.log('加载地址续费数据，请求参数:', params, 'API路径:', this.url.list)
      getAction(this.url.list, params).then((res) => {
        console.log('地址续费数据响应:', res)
        if (res.success && res.result) {
          this.addressDataSource = res.result.records || []
          this.addressPagination.total = res.result.total || 0
        } else {
          this.$message.error(res.message || '加载地址续费数据失败')
          this.addressDataSource = []
          this.addressPagination.total = 0
        }
      }).catch((err) => {
        console.error('加载地址续费数据失败:', err)
        let errorMsg = '未知错误'
        if (err.response && err.response.data && err.response.data.message) {
          errorMsg = err.response.data.message
        } else if (err.message) {
          errorMsg = err.message
        }
        this.$message.error('加载地址续费数据失败：' + errorMsg)
        this.addressDataSource = []
        this.addressPagination.total = 0
      }).finally(() => {
        this.addressLoading = false
      })
    },
    // 加载已完成地址续费数据
    loadCompletedAddressData() {
      if (!this.url.list) {
        this.$message.error('请设置url.list属性!')
        return
      }
      this.completedAddressLoading = true
      
      // 构建基础查询参数
      const baseParams = {
        detailType: '4', // 地址续费
      }
      // 添加查询条件
      if (this.queryParam.companyName) {
        baseParams.companyName = this.queryParam.companyName
      }
      
      // 分别查询已通过（auditStatus='1'）和已驳回（auditStatus='2'）的记录
      const params1 = {
        ...baseParams,
        pageNo: 1,
        pageSize: 1000, // 获取足够多的记录，然后在前端分页
        auditStatus: '1',
      }
      const params2 = {
        ...baseParams,
        pageNo: 1,
        pageSize: 1000,
        auditStatus: '2',
      }
      
      console.log('加载已完成地址续费数据，请求参数:', params1, params2, 'API路径:', this.url.list)
      Promise.all([
        getAction(this.url.list, params1),
        getAction(this.url.list, params2)
      ]).then(([res1, res2]) => {
        let allRecords = []
        
        if (res1.success && res1.result) {
          const records1 = res1.result.records || []
          allRecords = allRecords.concat(records1)
        }
        
        if (res2.success && res2.result) {
          const records2 = res2.result.records || []
          allRecords = allRecords.concat(records2)
        }
        
        // 按创建时间倒序排序
        allRecords.sort((a, b) => {
          const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
          const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
          return timeB - timeA
        })
        
        // 分页处理
        const total = allRecords.length
        const start = (this.completedAddressPagination.current - 1) * this.completedAddressPagination.pageSize
        const end = start + this.completedAddressPagination.pageSize
        this.completedAddressDataSource = allRecords.slice(start, end)
        this.completedAddressPagination.total = total
        
        console.log('已完成地址续费数据:', this.completedAddressDataSource)
      }).catch((err) => {
        console.error('加载已完成地址续费数据失败:', err)
        let errorMsg = '未知错误'
        if (err.response && err.response.data && err.response.data.message) {
          errorMsg = err.response.data.message
        } else if (err.message) {
          errorMsg = err.message
        }
        this.$message.error('加载已完成地址续费数据失败：' + errorMsg)
        this.completedAddressDataSource = []
        this.completedAddressPagination.total = 0
      }).finally(() => {
        this.completedAddressLoading = false
      })
    },
    // 地址续费表格分页变化
    handleAddressTableChange(pagination) {
      this.addressPagination.current = pagination.current
      this.addressPagination.pageSize = pagination.pageSize
      this.loadAddressData()
    },
    // 已完成地址续费表格分页变化
    handleCompletedAddressTableChange(pagination) {
      this.completedAddressPagination.current = pagination.current
      this.completedAddressPagination.pageSize = pagination.pageSize
      this.loadCompletedAddressData()
    },
    // 地址续费子标签页切换
    handleAddressSubTabChange(activeKey) {
      this.addressSubTab = activeKey
      if (activeKey === 'pending') {
        this.addressPagination.current = 1
        this.loadAddressData()
      } else if (activeKey === 'completed') {
        this.completedAddressPagination.current = 1
        this.loadCompletedAddressData()
      }
    },
    // 重置查询
    searchReset() {
      this.queryParam = {
        companyName: '',
        detailType: '',
        auditStatus: '0',
      }
      if (this.activeTab === 'accounting') {
        if (this.accountingSubTab === 'pending') {
          this.loadData(1)
        } else if (this.accountingSubTab === 'completed') {
          this.completedPagination.current = 1
          this.loadCompletedData()
        }
      } else if (this.activeTab === 'address') {
        if (this.addressSubTab === 'pending') {
          this.addressPagination.current = 1
          this.loadAddressData()
        } else if (this.addressSubTab === 'completed') {
          this.completedAddressPagination.current = 1
          this.loadCompletedAddressData()
        }
      }
    },
    // 金额格式化
    formatAmount(amount) {
      if (!amount && amount !== 0) return '0.00'
      const num = Number(amount)
      if (isNaN(num)) return '0.00'
      return num
        .toFixed(2)
        .replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    // 图片列表解析
    getImageList(val) {
      if (!val) return []
      if (typeof val === 'string') {
        try {
          // 兼容 JSON 数组和逗号分隔
          if (val.trim().startsWith('[')) {
            const arr = JSON.parse(val)
            if (Array.isArray(arr)) {
              return arr.filter(Boolean)
            }
          }
        } catch (e) {
          // ignore
        }
        return val.split(',').filter((item) => item && item.trim())
      }
      if (Array.isArray(val)) {
        return val.filter(Boolean)
      }
      return []
    },
    getImageUrl(path) {
      return getFileAccessHttpUrl(path)
    },
    previewImage(url) {
      this.previewImageUrl = url
      this.previewVisible = true
    },
    // 打开审核弹窗
    handleAudit(record) {
      this.currentRecord = record || {}
      this.auditForm = {
        auditStatus: '1',
        remark: '',
      }
      this.auditModal.visible = true
    },
    // 取消审核
    handleAuditCancel() {
      this.auditModal.visible = false
    },
    // 确认审核
    handleAuditOk() {
      if (!this.currentRecord || !this.currentRecord.id) {
        this.$message.error('未找到续费记录')
        return
      }
      this.$refs.auditFormRef.validate((valid) => {
        if (!valid) return
        this.auditModal.loading = true
        const params = {
          id: this.currentRecord.id,
          auditStatus: this.auditForm.auditStatus,
          remark: this.auditForm.remark,
        }
        httpAction('/renew/ghAddressRenew/audit', params, 'POST')
          .then((res) => {
            if (res.success) {
              this.$message.success(res.message || '审核成功')
              this.auditModal.visible = false
              // 根据当前标签页刷新对应的数据
              if (this.activeTab === 'accounting') {
                if (this.accountingSubTab === 'pending') {
                  this.loadData(1)
                } else if (this.accountingSubTab === 'completed') {
                  this.loadCompletedData()
                }
              } else if (this.activeTab === 'address') {
                // 审核后，从待审核列表移除，可能需要切换到已完成列表
                // 或者刷新当前列表
                if (this.addressSubTab === 'pending') {
                  this.loadAddressData()
                } else if (this.addressSubTab === 'completed') {
                  this.loadCompletedAddressData()
                }
              }
            } else {
              this.$message.error(res.message || '审核失败')
            }
          })
          .catch((err) => {
            console.error('续费审核失败:', err)
            this.$message.error(
              '续费审核失败：' + (err.message || '未知错误'),
            )
          })
          .finally(() => {
            this.auditModal.loading = false
          })
      })
    },
  },
}
</script>

<style scoped>
.table-page-search-submitButtons {
  display: inline-block;
}
</style>


