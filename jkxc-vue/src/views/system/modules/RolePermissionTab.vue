<template>
  <div>
    <a-alert v-if="!currentRoleId" type="info" :showIcon="true" style="margin-bottom: 16px;">
      <div slot="message">请先选择一个角色，然后配置该角色的菜单权限</div>
    </a-alert>
    
    <div v-else>
      <a-spin :spinning="loading">
        <a-form>
          <a-form-item label='菜单权限配置'>
            <div v-if="treeData && treeData.length > 0" class="permission-tree-container">
              <a-tree
                checkable
                @check="onCheck"
                :checkedKeys="checkedKeys"
                :treeData="treeData"
                @expand="onExpand"
                @select="onTreeNodeSelect"
                :selectedKeys="selectedKeys"
                :expandedKeys="expandedKeys"
                :checkStrictly="checkStrictly">
                <span slot="hasDatarule" slot-scope="{slotTitle,ruleFlag}">
                  {{ slotTitle }}<a-icon v-if="ruleFlag" type="align-left" style="margin-left:5px;color: red;"></a-icon>
                </span>
              </a-tree>
            </div>
            <a-empty v-else description="暂无菜单数据" style="padding: 40px 0" />
          </a-form-item>
        </a-form>
      </a-spin>

      <div style="margin-top: 16px; text-align: right;">
        <a-dropdown style="float: left" :trigger="['click']" placement="topCenter">
          <a-menu slot="overlay">
            <a-menu-item key="1" @click="switchCheckStrictly(1)">父子关联</a-menu-item>
            <a-menu-item key="2" @click="switchCheckStrictly(2)">取消关联</a-menu-item>
            <a-menu-item key="3" @click="checkALL">全部勾选</a-menu-item>
            <a-menu-item key="4" @click="cancelCheckALL">取消全选</a-menu-item>
            <a-menu-item key="5" @click="expandAll">展开所有</a-menu-item>
            <a-menu-item key="6" @click="closeAll">合并所有</a-menu-item>
          </a-menu>
          <a-button>
            树操作 <a-icon type="up" />
          </a-button>
        </a-dropdown>
        <a-button @click="handleReset" style="margin-right: 8px">重置</a-button>
        <a-button @click="handleSave" type="primary" :loading="loading">保存权限</a-button>
      </div>
    </div>

    <role-datarule-modal ref="datarule"></role-datarule-modal>
  </div>
</template>

<script>
  import {queryTreeListForRole,queryRolePermission,saveRolePermission} from '@/api/api'
  import RoleDataruleModal from './RoleDataruleModal.vue'

  export default {
    name: "RolePermissionTab",
    components:{
      RoleDataruleModal
    },
    props: {
      roleId: {
        type: String,
        default: ''
      }
    },
    data(){
      return {
        currentRoleId: '',
        treeData: [],
        defaultCheckedKeys:[],
        checkedKeys:[],
        expandedKeys:[],
        allTreeKeys:[],
        autoExpandParent: true,
        checkStrictly: true,
        loading: false,
        selectedKeys:[]
      }
    },
    watch: {
      roleId: {
        immediate: true,
        handler(newVal) {
          if (newVal) {
            this.currentRoleId = newVal
            this.loadData()
          } else {
            this.currentRoleId = ''
            this.reset()
          }
        }
      }
    },
    methods: {
      onTreeNodeSelect(id){
        if(id && id.length>0){
          this.selectedKeys = id
          if (this.$refs.datarule) {
            this.$refs.datarule.show(this.selectedKeys[0], this.currentRoleId)
          }
        }
      },
      onCheck (o) {
        if(this.checkStrictly){
          this.checkedKeys = o.checked;
        }else{
          this.checkedKeys = o
        }
      },
      onExpand(expandedKeys){
        this.expandedKeys = expandedKeys;
        this.autoExpandParent = false
      },
      reset () {
        this.expandedKeys = []
        this.checkedKeys = []
        this.defaultCheckedKeys = []
        this.loading = false
        this.selectedKeys = []
      },
      expandAll () {
        this.expandedKeys = this.allTreeKeys
      },
      closeAll () {
        this.expandedKeys = []
      },
      checkALL () {
        this.checkedKeys = this.allTreeKeys
      },
      cancelCheckALL () {
        this.checkedKeys = []
      },
      switchCheckStrictly (v) {
        if(v==1){
          this.checkStrictly = false
        }else if(v==2){
          this.checkStrictly = true
        }
      },
      handleReset() {
        this.checkedKeys = [...this.defaultCheckedKeys]
        this.$message.info('已重置为原始权限配置')
      },
      handleSave() {
        if (!this.currentRoleId) {
          this.$message.warning('请先选择一个角色')
          return
        }
        let that = this;
        let params =  {
          roleId: that.currentRoleId,
          permissionIds: that.checkedKeys.join(","),
          lastpermissionIds: that.defaultCheckedKeys.join(","),
        };
        that.loading = true;
        saveRolePermission(params).then((res)=>{
          if(res.success){
            that.$message.success(res.message);
            that.defaultCheckedKeys = [...that.checkedKeys]
            that.loading = false;
          }else {
            that.$message.error(res.message);
            that.loading = false;
          }
        }).catch(() => {
          that.loading = false;
        })
      },
      // 递归处理树数据，将 null 值转换为合适的格式
      processTreeData(nodes) {
        if (!nodes || !Array.isArray(nodes)) {
          return []
        }
        return nodes.map(node => {
          const processedNode = { ...node }
          // 处理 icon 字段：null 转换为 undefined
          if (processedNode.icon === null || processedNode.icon === '') {
            processedNode.icon = undefined
          }
          // 处理 scopedSlots 字段：null 转换为 undefined
          if (processedNode.scopedSlots === null) {
            processedNode.scopedSlots = undefined
          }
          // 处理 ruleFlag 字段：null 转换为 undefined
          if (processedNode.ruleFlag === null) {
            processedNode.ruleFlag = undefined
          }
          // 处理 children 字段
          if (processedNode.isLeaf) {
            // 叶子节点，children 应该为 undefined
            processedNode.children = undefined
          } else if (processedNode.children === null) {
            // 非叶子节点但 children 为 null，转换为空数组
            processedNode.children = []
          } else if (Array.isArray(processedNode.children)) {
            // 递归处理子节点
            processedNode.children = this.processTreeData(processedNode.children)
          }
          return processedNode
        })
      },
      loadData(){
        if (!this.currentRoleId) {
          this.treeData = []
          this.reset()
          return
        }
        this.loading = true
        this.treeData = []
        // 查询权限树（菜单树）
        queryTreeListForRole().then((res) => {
          console.log('queryTreeListForRole 返回结果:', res)
          if (res.success && res.result) {
            const treeList = res.result.treeList || []
            this.allTreeKeys = res.result.ids || []
            console.log('菜单树数据:', treeList, '数量:', treeList.length)
            console.log('所有键:', this.allTreeKeys)
            
            // 处理树数据，确保 children 格式正确
            this.treeData = this.processTreeData(treeList)
            console.log('处理后的菜单树数据:', this.treeData)
            
            // 查询当前角色的权限
            queryRolePermission({roleId: this.currentRoleId}).then((permRes)=>{
              console.log('queryRolePermission 返回结果:', permRes)
              if (permRes.success) {
                this.checkedKeys = [...permRes.result];
                this.defaultCheckedKeys = [...permRes.result];
                // 默认不展开任何节点，全部收起
                this.expandedKeys = []
                console.log('已选权限:', this.checkedKeys)
              }
              this.loading = false
            }).catch((err) => {
              console.error('查询角色权限失败:', err)
              this.loading = false
            })
          } else {
            this.loading = false
            console.error('加载菜单权限树失败，返回结果:', res)
            this.$message.error(res.message || '加载菜单权限失败')
          }
        }).catch((err) => {
          this.loading = false
          console.error('加载菜单权限树失败:', err)
          this.$message.error('加载菜单权限失败，请稍后重试')
        })
      }
    }
  }
</script>

<style lang="less" scoped>
  .permission-tree-container {
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
      
      .ant-tree-checkbox {
        margin-right: 8px;
      }
      
      .ant-tree-title {
        font-size: 14px;
        color: #333;
      }
      
      .ant-tree-iconEle {
        margin-right: 6px;
      }
    }
  }
</style>

