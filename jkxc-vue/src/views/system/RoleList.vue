<template xmlns:background-color="http://www.w3.org/1999/xhtml">
  <a-row :gutter="10">
    <a-col :md="12" :sm="24">
      <a-card :bordered="false">

        <!-- 按钮操作区域 -->
        <a-row style="margin-left: 14px; margin-bottom: 16px">
          <a-button @click="handleAdd(1)" type="primary" icon="plus" style="margin-right: 8px">添加角色</a-button>
          <a-button @click="handleAdd(2)" type="primary" icon="plus-circle" style="margin-right: 8px">添加下级</a-button>
        </a-row>
        
        <!-- 角色类型筛选 -->
        <a-row style="margin-left: 14px; margin-top: 16px; margin-bottom: 16px">
          <a-radio-group v-model="roleTypeFilter" @change="onRoleTypeChange" buttonStyle="solid">
            <a-radio-button :value="1">操作角色</a-radio-button>
            <a-radio-button :value="2">数据角色</a-radio-button>
          </a-radio-group>
        </a-row>

        <div style="background: #fff;padding: 16px;height: 100%; margin-top: 5px; border-radius: 4px;">
          <a-alert type="info" :showIcon="true" style="margin-bottom: 12px">
            <div slot="message">
              当前选择：<span v-if="this.currSelected.title" style="font-weight: 500; color: #1890ff;">{{ getCurrSelectedTitle() }}</span>
              <a v-if="this.currSelected.title" style="margin-left: 10px" @click="onClearSelected">取消选择</a>
            </div>
          </a-alert>
          <a-input-search @search="onSearch" style="width:100%;margin-bottom: 16px" placeholder="请输入角色名称" allowClear/>
          <!-- 树-->
          <div class="role-tree-container">
            <a-dropdown :trigger="[this.dropTrigger]" @visibleChange="dropStatus">
              <span style="user-select: none">
                <a-tree
                  checkable
                  multiple
                  @select="onSelect"
                  @check="onCheck"
                  @rightClick="rightHandle"
                  :selectedKeys="selectedKeys"
                  :checkedKeys="checkedKeys"
                  :treeData="roleTree"
                  :checkStrictly="checkStrictly"
                  :expandedKeys="iExpandedKeys"
                  :autoExpandParent="autoExpandParent"
                  @expand="onExpand"/>
              </span>
              <!--新增右键点击事件,和增加添加和删除功能-->
              <a-menu slot="overlay">
                <a-menu-item @click="handleAdd(3)" key="1">
                  <a-icon type="plus" /> 添加
                </a-menu-item>
                <a-menu-item @click="handleDelete" key="2">
                  <a-icon type="delete" /> 删除
                </a-menu-item>
                <a-menu-item @click="closeDrop" key="3">取消</a-menu-item>
              </a-menu>
            </a-dropdown>
          </div>
        </div>
      </a-card>
      <!---- 树操作按钮 =======------>
      <div class="drawer-bootom-button">
        <a-dropdown :trigger="['click']" placement="topCenter">
          <a-menu slot="overlay">
            <a-menu-item key="1" @click="switchCheckStrictly(1)">
              <a-icon type="link" /> 父子关联
            </a-menu-item>
            <a-menu-item key="2" @click="switchCheckStrictly(2)">
              <a-icon type="disconnect" /> 取消关联
            </a-menu-item>
            <a-menu-item key="3" @click="checkALL">
              <a-icon type="check-square" /> 全部勾选
            </a-menu-item>
            <a-menu-item key="4" @click="cancelCheckALL">
              <a-icon type="border" /> 取消全选
            </a-menu-item>
            <a-menu-item key="5" @click="expandAll">
              <a-icon type="down" /> 展开所有
            </a-menu-item>
            <a-menu-item key="6" @click="closeAll">
              <a-icon type="up" /> 合并所有
            </a-menu-item>
          </a-menu>
          <a-button type="default" icon="setting">
            树操作 <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>
      <!---- 树操作按钮 =======------>
    </a-col>
    <a-col :md="12" :sm="24">
      <a-tabs :activeKey="activeTabKey" @change="onTabChange">
        <a-tab-pane tab="基本信息" key="1" >
          <a-card :bordered="false" v-if="!hiding">
            <a-alert v-if="!model.id" type="info" :showIcon="true" style="margin-bottom: 16px">
              <span slot="message">
                <a-icon type="plus-circle" /> {{ model.parentId ? '正在添加子角色' : '正在添加新角色' }}
              </span>
            </a-alert>
            <a-form-model ref="form" :model="model" :rules="validatorRules">
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" prop="roleName" label="角色名称">
                <a-input placeholder="请输入角色名称" v-model="model.roleName" />
              </a-form-model-item>
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="上级角色">
                <a-tree-select
                  style="width:100%"
                  :dropdownStyle="{maxHeight:'200px',overflow:'auto'}"
                  :treeData="treeData"
                  :disabled="disable"
                  v-model="model.parentId"
                  @change="onParentIdChange"
                  placeholder="无（根节点）">
                </a-tree-select>
              </a-form-model-item>
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" prop="roleCode" label="角色编码">
                <a-input :disabled="roleCodeDisabled" placeholder="请输入角色编码" v-model="model.roleCode" />
              </a-form-model-item>
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" prop="roleType" label="角色类型">
                <a-radio-group v-model="model.roleType" :disabled="roleTypeDisabled">
                  <a-radio :value="1">操作角色</a-radio>
                  <a-radio :value="2">数据角色</a-radio>
                </a-radio-group>
                <span v-if="roleTypeDisabled" style="margin-left: 8px; color: #999; font-size: 12px;">
                  （继承自上级角色）
                </span>
              </a-form-model-item>
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="角色排序">
                <a-input-number v-model="model.roleOrder" :min="0" style="width:100%" />
              </a-form-model-item>
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" prop="status" label="角色状态">
                <a-radio-group v-model="model.status">
                  <a-radio :value="1">启用</a-radio>
                  <a-radio :value="0">禁用</a-radio>
                </a-radio-group>
              </a-form-model-item>
              <a-form-model-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="描述">
                <a-textarea placeholder="请输入描述" v-model="model.description" :rows="4"/>
              </a-form-model-item>
            </a-form-model>
            <div class="anty-form-btn">
              <a-button @click="emptyCurrForm" type="default" htmlType="button" icon="sync">重置</a-button>
              <a-button @click="submitCurrForm" type="primary" htmlType="button" icon="save">保存</a-button>
              <a-button v-if="!model.id" @click="onClearSelected" type="default" htmlType="button" icon="close" style="margin-left: 8px">取消</a-button>
            </div>
          </a-card>
          <a-card v-else >
            <a-empty>
              <span slot="description"> 请选择一个角色进行编辑，或点击"添加角色"创建新角色 </span>
            </a-empty>
          </a-card>
        </a-tab-pane>
        <a-tab-pane tab="角色权限" key="2" forceRender>
          <role-permission-tab v-if="currSelected.id && currSelected.roleType === 1" :roleId="currSelected.id"/>
          <a-card v-else-if="currSelected.id && currSelected.roleType === 2" :bordered="false">
            <a-empty description="数据角色暂不支持菜单权限配置" />
          </a-card>
          <a-card v-else :bordered="false">
            <a-empty description="请先选择一个操作角色" />
          </a-card>
        </a-tab-pane>
      </a-tabs>

    </a-col>
  </a-row>
</template>
<script>
  import RolePermissionTab from './modules/RolePermissionTab'
  import {queryRoleTreeList, addRole, editRole, deleteAction, httpAction} from '@/api/api'
  import {JeecgListMixin} from '@/mixins/JeecgListMixin'

  export default {
    name: 'RoleList',
    mixins: [JeecgListMixin],
    components: {
      RolePermissionTab
    },
    data() {
      return {
        description: '角色管理页面',
        roleTypeFilter: 1, // 角色类型筛选：1-操作角色，2-数据角色，默认显示操作角色
        iExpandedKeys: [],
        loading: false,
        autoExpandParent: true,
        disable: false,
        treeData: [],
        visible: false,
        roleTree: [],
        rightClickSelectedKey: '',
        hiding: true,
        model: {},
        dropTrigger: '',
        checkedKeys: [],
        selectedKeys: [],
        currSelected: {},
        allTreeKeys: [],
        checkStrictly: true,
        roleCodeDisabled: false,
        roleTypeDisabled: false, // 角色类型是否禁用（有上级角色时禁用）
        activeTabKey: '1', // 当前激活的标签页：1-基本信息，2-角色权限
        labelCol: {
          xs: {span: 24},
          sm: {span: 5}
        },
        wrapperCol: {
          xs: {span: 24},
          sm: {span: 16}
        },
        validatorRules: {
          roleName: [
            {required: true, message: '请输入角色名称!'},
            {min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur'}
          ],
          roleCode: [
            {required: true, message: '请输入角色编码!'},
            {min: 0, max: 64, message: '长度不超过 64 个字符', trigger: 'blur'}
          ]
        },
        url: {
          delete: '/sys/role/delete',
          edit: '/sys/role/edit',
          add: '/sys/role/add',
        },
      }
    },
    mounted() {
      this.loadTree()
    },
    methods: {
      loadData() {
        this.refresh();
      },
      loadTree() {
        var that = this
        queryRoleTreeList({roleType: this.roleTypeFilter}).then((res) => {
          if (res.success) {
            this.allTreeKeys = [];
            // treeData和roleTree使用相同的数据源
            const treeList = res.result || []
            that.roleTree = treeList
            that.treeData = treeList
            // 遍历树结构设置展开键和所有键
            for (let i = 0; i < treeList.length; i++) {
              that.setThisExpandedKeys(treeList[i])
              that.getAllKeys(treeList[i])
            }
            this.loading = false
          }
        })
      },
      onRoleTypeChange() {
        this.loadTree()
      },
      setThisExpandedKeys(node) {
        if (node.children && node.children.length > 0) {
          this.iExpandedKeys.push(node.key)
          for (let a = 0; a < node.children.length; a++) {
            this.setThisExpandedKeys(node.children[a])
          }
        }
      },
      getAllKeys(node) {
        this.allTreeKeys.push(node.key)
        if (node.children && node.children.length > 0) {
          for (let a = 0; a < node.children.length; a++) {
            this.getAllKeys(node.children[a])
          }
        }
      },
      refresh() {
        this.loading = true
        this.loadTree()
      },
      // 右键操作方法
      rightHandle(node) {
        this.dropTrigger = 'contextmenu'
        this.rightClickSelectedKey = node.node.eventKey
      },
      onExpand(expandedKeys) {
        this.iExpandedKeys = expandedKeys
        this.autoExpandParent = false
      },
      // 右键点击下拉框改变事件
      dropStatus(visible) {
        if (visible == false) {
          this.dropTrigger = ''
        }
      },
      // 右键下拉关闭下拉框
      closeDrop() {
        this.dropTrigger = ''
      },
      handleAdd(type) {
        // type: 1-添加根节点，2-添加下级，3-右键添加
        let parentId = null
        let parentRoleType = null
        if (type === 2 && this.currSelected.id) {
          parentId = this.currSelected.id
          parentRoleType = this.currSelected.roleType
        } else if (type === 3 && this.rightClickSelectedKey) {
          parentId = this.rightClickSelectedKey
          // 查找上级角色的roleType
          parentRoleType = this.findRoleTypeById(parentId)
        }
        this.model = {
          parentId: parentId,
          roleType: parentRoleType || this.roleTypeFilter,
          status: 1,
          roleOrder: 0
        }
        this.roleCodeDisabled = false
        // 如果有上级角色，禁用角色类型选择
        this.roleTypeDisabled = !!parentId
        this.selectedKeys = []
        this.currSelected = {}
        this.hiding = false // 显示右侧表单区域
        this.activeTabKey = '1' // 切换到基本信息标签页
      },
      // 根据角色ID查找角色类型
      findRoleTypeById(roleId) {
        if (!roleId) return null
        const findInTree = (nodes) => {
          for (let node of nodes) {
            if (node.id === roleId || node.key === roleId) {
              return node.roleType
            }
            if (node.children && node.children.length > 0) {
              const found = findInTree(node.children)
              if (found !== null) return found
            }
          }
          return null
        }
        return findInTree(this.treeData)
      },
      // 上级角色变化时的处理
      onParentIdChange(parentId) {
        if (parentId) {
          // 有上级角色，查找上级角色的roleType并设置
          const parentRoleType = this.findRoleTypeById(parentId)
          if (parentRoleType !== null) {
            this.model.roleType = parentRoleType
            this.roleTypeDisabled = true
          }
        } else {
          // 没有上级角色，允许选择角色类型
          this.roleTypeDisabled = false
          // 强制使用当前筛选的类型（确保在数据角色页面添加的角色是数据角色）
          this.model.roleType = this.roleTypeFilter
        }
      },
      onTabChange(activeKey) {
        this.activeTabKey = activeKey
      },
      handleDelete() {
        if (!this.rightClickSelectedKey) {
          this.$message.warning('请选择要删除的角色!')
          return
        }
        var that = this
        this.$confirm({
          title: '确认删除',
          content: '确定要删除该角色及其子节点吗?',
          onOk: function () {
            deleteAction(that.url.delete, {id: that.rightClickSelectedKey}).then((res) => {
              if (res.success) {
                that.$message.success(res.message)
                that.loadTree()
                that.onClearSelected()
              } else {
                that.$message.warning(res.message)
              }
            })
          }
        })
      },
      onSearch(value) {
        // 搜索功能可以后续实现
        this.loadTree()
      },
      onCheck(checkedKeys, info) {
        this.hiding = false
        if(this.checkStrictly){
          this.checkedKeys = checkedKeys.checked;
        }else{
          this.checkedKeys = checkedKeys
        }
      },
      onSelect(selectedKeys, e) {
        this.hiding = false
        let record = e.node.dataRef
        this.currSelected = Object.assign({}, record)
        this.model = Object.assign({}, this.currSelected)
        this.selectedKeys = [record.key]
        this.model.parentId = record.parentId
        this.roleCodeDisabled = true // 编辑时禁用角色编码
        // 编辑时，角色类型可以选择（因为可能修改上级角色）
        this.roleTypeDisabled = false
        // 确保角色类型和状态有默认值
        if (this.model.roleType === undefined || this.model.roleType === null) {
          this.model.roleType = 1
        }
        if (this.model.status === undefined || this.model.status === null) {
          this.model.status = 1
        }
        if (this.model.roleOrder === undefined || this.model.roleOrder === null) {
          this.model.roleOrder = 0
        }
      },
      getCurrSelectedTitle() {
        return !this.currSelected.title ? '' : this.currSelected.title
      },
      onClearSelected() {
        this.hiding = true
        this.checkedKeys = []
        this.currSelected = {}
        this.selectedKeys = []
        this.model = {}
        this.roleTypeDisabled = false
      },
      submitCurrForm() {
        this.$refs.form.validate(valid => {
          if (valid) {
            if (!this.model.roleName) {
              this.$message.warning('请填写角色名称!')
              return
            }
            if (!this.model.roleCode) {
              this.$message.warning('请填写角色编码!')
              return
            }
            // 确保roleType正确：如果是新增且没有上级角色，使用当前筛选的类型
            if (!this.model.id && !this.model.parentId) {
              this.model.roleType = this.roleTypeFilter
            }
            // 如果有上级角色，确保roleType与上级角色一致
            if (this.model.parentId) {
              const parentRoleType = this.findRoleTypeById(this.model.parentId)
              if (parentRoleType !== null) {
                this.model.roleType = parentRoleType
              }
            }
            let url = this.url.add
            let method = 'post'
            if (this.model.id) {
              url = this.url.edit
              method = 'put'
            }
            httpAction(url, this.model, method).then((res) => {
              if (res.success) {
                this.$message.success('保存成功!')
                this.loadTree()
                // 如果是新增，保存后自动选中新创建的角色
                if (!this.model.id) {
                  // 延迟一下，等待树更新
                  setTimeout(() => {
                    // 尝试找到新创建的角色并选中
                    this.loadTree()
                    this.onClearSelected()
                  }, 500)
                } else {
                  // 编辑后更新当前选中
                  this.currSelected = Object.assign({}, this.model)
                }
              } else {
                this.$message.error(res.message)
              }
            })
          }
        })
      },
      emptyCurrForm() {
        if (this.model.id) {
          // 编辑状态：重置为当前选中角色的原始值
          this.model = Object.assign({}, this.currSelected)
        } else {
          // 新增状态：重置为空表单
          this.$refs.form.resetFields()
          const parentId = this.model.parentId || null
          let roleType = this.roleTypeFilter
          // 如果有上级角色，继承上级角色的类型
          if (parentId) {
            const parentRoleType = this.findRoleTypeById(parentId)
            if (parentRoleType !== null) {
              roleType = parentRoleType
            }
          }
          this.model = {
            parentId: parentId,
            roleType: roleType,
            status: 1,
            roleOrder: 0
          }
          // 如果有上级角色，禁用角色类型选择
          this.roleTypeDisabled = !!parentId
        }
      },
      switchCheckStrictly(v) {
        if (v == 1) {
          this.checkStrictly = false
        } else if (v == 2) {
          this.checkStrictly = true
        }
      },
      checkALL() {
        this.checkedKeys = this.allTreeKeys
      },
      cancelCheckALL() {
        this.checkedKeys = []
      },
      expandAll() {
        this.iExpandedKeys = this.allTreeKeys
      },
      closeAll() {
        this.iExpandedKeys = []
      },
    }
  }
</script>
<style lang="less" scoped>
  @import '~@assets/less/common.less';
  .drawer-bootom-button {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-top: 1px solid #e8e8e8;
    padding: 12px 16px;
    text-align: right;
    left: 0;
    background: #fff;
    border-radius: 0 0 4px 4px;
  }
  .anty-form-btn {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    text-align: right;
  }
  .anty-form-btn .ant-btn {
    margin-left: 8px;
  }
  .role-tree-container {
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    padding: 16px;
    height: 500px;
    overflow-y: auto;
    overflow-x: hidden;
    
    // 自定义滚动条样式
    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 4px;
      
      &:hover {
        background: #a8a8a8;
      }
    }
    
    // 树节点样式
    /deep/ .ant-tree {
      background: transparent;
      
      .ant-tree-node-content-wrapper {
        padding: 4px 8px;
        border-radius: 4px;
        transition: all 0.3s;
        
        &:hover {
          background: #e6f7ff;
        }
      }
      
      .ant-tree-node-selected {
        .ant-tree-node-content-wrapper {
          background: #bae7ff;
        }
      }
    }
  }
  
  .ant-card {
    box-shadow: 0 2px 8px rgba(0,0,0,0.09);
  }
</style>
