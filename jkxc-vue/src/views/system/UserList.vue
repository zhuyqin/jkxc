<template>
  <a-card :bordered="false">
    <!-- Tab页：在职/离职 -->
    <a-tabs :activeKey="activeTab" @change="handleTabChange" style="margin-bottom: 16px;">
      <a-tab-pane key="active" tab="在职">
        <template slot="tab">
          <span>在职</span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="resigned" tab="离职">
        <template slot="tab">
          <span>离职</span>
        </template>
      </a-tab-pane>
    </a-tabs>

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="6" :sm="12">
            <a-form-item label="账号">
              <!--<a-input placeholder="请输入账号查询" v-model="queryParam.username"></a-input>-->
              <j-input placeholder="输入账号模糊查询" v-model="queryParam.username"></j-input>
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="性别">
              <a-select v-model="queryParam.sex" placeholder="请选择性别">
                <a-select-option value="">请选择</a-select-option>
                <a-select-option value="1">男</a-select-option>
                <a-select-option value="2">女</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>


          <template v-if="toggleSearchStatus">
            <a-col :md="6" :sm="8">
              <a-form-item label="真实名字">
                <a-input placeholder="请输入真实名字" v-model="queryParam.realname"></a-input>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="8">
              <a-form-item label="手机号码">
                <a-input placeholder="请输入手机号码查询" v-model="queryParam.phone"></a-input>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="8">
              <a-form-item label="当前状态">
                <a-select v-model="queryParam.status" placeholder="请选择">
                  <a-select-option value="">请选择</a-select-option>
                  <a-select-option value="1">正常</a-select-option>
                  <a-select-option value="2">冻结</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </template>

          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus" >添加员工</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('员工信息')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <j-third-app-button biz-type="user" :selected-row-keys="selectedRowKeys" syncToApp syncToLocal @sync-finally="onSyncFinally"/>
      <a-button type="primary" icon="hdd" @click="recycleBinVisible=true">回收站</a-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay" @click="handleMenuClick">
          <a-menu-item key="1">
            <a-icon type="delete" @click="batchDel"/>
            删除
          </a-menu-item>
          <a-menu-item key="2">
            <a-icon type="lock" @click="batchFrozen('2')"/>
            冻结
          </a-menu-item>
          <a-menu-item key="3">
            <a-icon type="unlock" @click="batchFrozen('1')"/>
            解冻
          </a-menu-item>
        </a-menu>
        <a-button>
          批量操作
          <a-icon type="down"/>
        </a-button>
      </a-dropdown>
      <j-super-query :fieldList="superQueryFieldList" @handleSuperQuery="handleSuperQuery"/>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i>已选择&nbsp;<a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项&nbsp;&nbsp;
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        bordered
        size="middle"
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        :scroll="{ x: 2000 }"
        @change="handleTableChange">

        <template slot="avatarslot" slot-scope="text, record, index">
          <div class="anty-img-wrap">
            <a-avatar shape="square" :src="getAvatarView(record.avatar)" icon="user"/>
          </div>
        </template>

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)" >编辑</a>

          <a-divider type="vertical" />

          <a-dropdown>
            <a class="ant-dropdown-link">
              更多 <a-icon type="down"/>
            </a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a href="javascript:;" @click="handleDetail(record)">详情</a>
              </a-menu-item>

              <a-menu-item>
                <a href="javascript:;" @click="handleChangePassword(record.username)">密码</a>
              </a-menu-item>

              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>

              <a-menu-item v-if="record.status==1">
                <a-popconfirm title="确定冻结吗?" @confirm="() => handleFrozen(record.id,2,record.username)">
                  <a>冻结</a>
                </a-popconfirm>
              </a-menu-item>

              <a-menu-item v-if="record.status==2">
                <a-popconfirm title="确定解冻吗?" @confirm="() => handleFrozen(record.id,1,record.username)">
                  <a>解冻</a>
                </a-popconfirm>
              </a-menu-item>

              <a-menu-item v-if="activeTab === 'active' && record.status != 3">
                <a-popconfirm title="确定离职吗?" @confirm="() => handleResign(record.id, record.username)">
                  <a>离职</a>
                </a-popconfirm>
              </a-menu-item>

              <a-menu-item>
                <a href="javascript:;" @click="handleAgentSettings(record.username)">代理人</a>
              </a-menu-item>

            </a-menu>
          </a-dropdown>
        </span>


      </a-table>
    </div>
    <!-- table区域-end -->

    <user-modal ref="modalForm" @ok="modalFormOk"></user-modal>

    <password-modal ref="passwordmodal" @ok="passwordModalOk"></password-modal>

    <sys-user-agent-modal ref="sysUserAgentModal"></sys-user-agent-modal>

    <!-- 用户回收站 -->
    <user-recycle-bin-modal :visible.sync="recycleBinVisible" @ok="modalFormOk"/>

  </a-card>
</template>

<script>
  import UserModal from './modules/UserModal'
  import PasswordModal from './modules/PasswordModal'
  import {putAction,getFileAccessHttpUrl} from '@/api/manage';
  import {frozenBatch} from '@/api/api'
  import {JeecgListMixin} from '@/mixins/JeecgListMixin'
  import SysUserAgentModal from "./modules/SysUserAgentModal";
  import JInput from '@/components/jeecg/JInput'
  import UserRecycleBinModal from './modules/UserRecycleBinModal'
  import JSuperQuery from '@/components/jeecg/JSuperQuery'
  import JThirdAppButton from '@/components/jeecgbiz/thirdApp/JThirdAppButton'

  export default {
    name: "UserList",
    mixins: [JeecgListMixin],
    components: {
      JThirdAppButton,
      SysUserAgentModal,
      UserModal,
      PasswordModal,
      JInput,
      UserRecycleBinModal,
      JSuperQuery
    },
    data() {
      return {
        description: '这是员工管理页面',
        queryParam: {},
        recycleBinVisible: false,
        activeTab: 'active', // 当前选中的tab：active-在职，resigned-离职
        columns: [
          {
            title: '员工账号',
            align: "center",
            dataIndex: 'username',
            width: 120,
            sorter: true
          },
          {
            title: '员工姓名',
            align: "center",
            width: 100,
            dataIndex: 'realname',
          },
          {
            title: '工作号码',
            align: "center",
            width: 100,
            dataIndex: 'workNo',
          },
          {
            title: '私人号码',
            align: "center",
            width: 120,
            dataIndex: 'phone'
          },
          {
            title: '性别',
            align: "center",
            width: 80,
            dataIndex: 'sex_dictText',
            sorter: true
          },
          {
            title: '员工生日',
            align: "center",
            width: 120,
            dataIndex: 'birthday',
            customRender: (text) => {
              if (!text) return '-';
              return text.split(' ')[0]; // 只显示日期部分
            }
          },
          {
            title: '入职时间',
            align: "center",
            width: 120,
            dataIndex: 'entryDate',
            customRender: (text) => {
              if (!text) return '-';
              return text.split(' ')[0]; // 只显示日期部分
            }
          },
          {
            title: '工作时长',
            align: "center",
            width: 120,
            dataIndex: 'entryDate',
            customRender: (text, record) => {
              return this.calculateWorkDuration(text);
            }
          },
          {
            title: '所属团队',
            align: "center",
            width: 150,
            dataIndex: 'teamId_dictText',
            customRender: (text, record) => {
              return text || '-';
            }
          },
          {
            title: '直属上级',
            align: "center",
            width: 120,
            dataIndex: 'directSupervisor_dictText',
            customRender: (text, record) => {
              return text || '-';
            }
          },
          {
            title: '当前状态',
            align: "center",
            width: 100,
            dataIndex: 'status_dictText'
          },
          {
            title: '紧急联系',
            align: "center",
            width: 120,
            dataIndex: 'emergencyContact',
            customRender: (text) => {
              return text || '-';
            }
          },
          {
            title: '紧急电话',
            align: "center",
            width: 120,
            dataIndex: 'emergencyPhone',
            customRender: (text) => {
              return text || '-';
            }
          },
          {
            title: '操作',
            dataIndex: 'action',
            scopedSlots: {customRender: 'action'},
            align: "center",
            width: 200
          }

        ],
        superQueryFieldList: [
          { type: 'input', value: 'username', text: '用户账号', },
          { type: 'input', value: 'realname', text: '用户姓名', },
          { type: 'select', value: 'sex', dbType: 'int', text: '性别', dictCode: 'sex' },
        ],
        url: {
          syncUser: "/act/process/extActProcess/doSyncUser",
          list: "/sys/user/list",
          delete: "/sys/user/delete",
          deleteBatch: "/sys/user/deleteBatch",
          exportXlsUrl: "/sys/user/exportXls",
          importExcelUrl: "sys/user/importExcel",
        },
      }
    },
    computed: {
      importExcelUrl: function(){
        return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
      }
    },
    created() {
      // 初始化时设置在职用户的查询条件（使用status_in支持多值查询）
      this.queryParam.status_in = '1,2';
    },
    methods: {
      // 计算工作时长
      calculateWorkDuration(entryDate) {
        if (!entryDate) return '-';
        
        try {
          // 处理日期字符串，可能包含时间部分
          const entryDateStr = entryDate.split(' ')[0];
          const entry = new Date(entryDateStr);
          const now = new Date();
          
          // 计算总天数
          const diffTime = now.getTime() - entry.getTime();
          const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
          
          if (diffDays < 0) {
            return '-'; // 入职时间在未来，返回-
          }
          
          // 少于30天，显示天数
          if (diffDays < 30) {
            return `${diffDays}天`;
          }
          
          // 计算年、月、日
          const entryYear = entry.getFullYear();
          const entryMonth = entry.getMonth() + 1;
          const entryDay = entry.getDate();
          
          const nowYear = now.getFullYear();
          const nowMonth = now.getMonth() + 1;
          const nowDay = now.getDate();
          
          let years = nowYear - entryYear;
          let months = nowMonth - entryMonth;
          let days = nowDay - entryDay;
          
          // 处理负数情况
          if (days < 0) {
            months--;
            // 获取上个月的天数
            const lastMonth = new Date(nowYear, nowMonth - 1, 0);
            days += lastMonth.getDate();
          }
          
          if (months < 0) {
            years--;
            months += 12;
          }
          
          // 超过1年，显示年月日
          if (years > 0) {
            return `${years}年${months}月${days}天`;
          }
          
          // 超过30天但少于1年，显示月日
          return `${months}月${days}天`;
        } catch (e) {
          console.error('计算工作时长失败:', e);
          return '-';
        }
      },
      getAvatarView: function (avatar) {
        return getFileAccessHttpUrl(avatar)
      },

      batchFrozen: function (status) {
        if (this.selectedRowKeys.length <= 0) {
          this.$message.warning('请选择一条记录！');
          return false;
        } else {
          let ids = "";
          let that = this;
          let isAdmin = false;
          that.selectionRows.forEach(function (row) {
            if (row.username == 'admin') {
              isAdmin = true;
            }
          });
          if (isAdmin) {
            that.$message.warning('管理员账号不允许此操作,请重新选择！');
            return;
          }
          that.selectedRowKeys.forEach(function (val) {
            ids += val + ",";
          });
          that.$confirm({
            title: "确认操作",
            content: "是否" + (status == 1 ? "解冻" : "冻结") + "选中账号?",
            onOk: function () {
              frozenBatch({ids: ids, status: status}).then((res) => {
                if (res.success) {
                  that.$message.success(res.message);
                  that.loadData();
                  that.onClearSelected();
                } else {
                  that.$message.warning(res.message);
                }
              });
            }
          });
        }
      },
      handleMenuClick(e) {
        if (e.key == 1) {
          this.batchDel();
        } else if (e.key == 2) {
          this.batchFrozen(2);
        } else if (e.key == 3) {
          this.batchFrozen(1);
        }
      },
      handleFrozen: function (id, status, username) {
        let that = this;
        //TODO 后台校验管理员角色
        if ('admin' == username) {
          that.$message.warning('管理员账号不允许此操作！');
          return;
        }
        frozenBatch({ids: id, status: status}).then((res) => {
          if (res.success) {
            that.$message.success(res.message);
            that.loadData();
          } else {
            that.$message.warning(res.message);
          }
        });
      },
      handleChangePassword(username) {
        this.$refs.passwordmodal.show(username);
      },
      handleAgentSettings(username){
        this.$refs.sysUserAgentModal.agentSettings(username);
        this.$refs.sysUserAgentModal.title = "用户代理人设置";
      },
      passwordModalOk() {
        //TODO 密码修改完成 不需要刷新页面，可以把datasource中的数据更新一下
      },
      onSyncFinally({isToLocal}) {
        // 同步到本地时刷新下数据
        if (isToLocal) {
          this.loadData()
        }
      },
      // Tab切换
      handleTabChange(key) {
        this.activeTab = key;
        // 重置查询条件
        this.queryParam = {};
        // 根据tab设置查询条件
        if (key === 'active') {
          // 在职：status in (1,2)（正常或冻结）
          this.queryParam.status_in = '1,2';
        } else if (key === 'resigned') {
          // 离职：status = 3
          this.queryParam.status = '3';
        }
        this.loadData(1);
      },
      // 离职功能
      handleResign(id, username) {
        let that = this;
        if ('admin' == username) {
          that.$message.warning('管理员账号不允许此操作！');
          return;
        }
        // 调用离职接口，将status设置为3
        frozenBatch({ids: id, status: 3}).then((res) => {
          if (res.success) {
            that.$message.success(res.message || '离职操作成功');
            that.loadData();
          } else {
            that.$message.warning(res.message);
          }
        });
      },
    }

  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
  
  /* 确保按钮区域布局稳定，防止按钮挤在一起 */
  .table-operator {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    margin-bottom: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
  
  .table-operator > * {
    margin-right: 8px;
    margin-bottom: 8px;
    flex-shrink: 0;
  }
  
  .table-operator .ant-btn,
  .table-operator .ant-upload,
  .table-operator .ant-dropdown {
    margin-right: 8px;
    margin-bottom: 8px;
  }
</style>