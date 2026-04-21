<template>
  <a-drawer
    :title="title"
    :maskClosable="true"
    :width="drawerWidth"
    placement="right"
    :closable="true"
    @close="handleCancel"
    :visible="visible"
    style="height: 100%;overflow: auto;padding-bottom: 53px;">

    <template slot="title">
      <div style="width: 100%;">
        <span>{{ title }}</span>
        <span style="display:inline-block;width:calc(100% - 51px);padding-right:10px;text-align: right">
          <a-button @click="toggleScreen" icon="appstore" style="height:20px;width:20px;border:0px"></a-button>
        </span>
      </div>

    </template>

    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules">

        <!-- 按照列表页面顺序排列字段 -->
        <a-form-model-item label="员工账号" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="username">
          <a-input placeholder="请输入员工账号" v-model="model.username" :readOnly="!!model.id"/>
        </a-form-model-item>

        <a-form-model-item label="员工姓名" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="realname">
          <a-input placeholder="请输入员工姓名" v-model="model.realname" />
        </a-form-model-item>

        <a-form-model-item label="工作号码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="workNo">
          <a-input placeholder="请输入工作号码" v-model="model.workNo" />
        </a-form-model-item>

        <a-form-model-item label="私人号码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="phone">
          <a-input placeholder="请输入私人号码" v-model="model.phone" />
        </a-form-model-item>

        <a-form-model-item label="性别" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sex">
          <a-select  v-model="model.sex"  placeholder="请选择性别" :getPopupContainer= "(target) => target.parentNode">
            <a-select-option :value="1">男</a-select-option>
            <a-select-option :value="2">女</a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item label="员工生日" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="birthday">
          <a-date-picker
            style="width: 100%"
            placeholder="请选择员工生日"
            v-model="model.birthday"
            :format="dateFormat"
            :getCalendarContainer="node => node.parentNode"/>
        </a-form-model-item>

        <a-form-model-item label="入职时间" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="entryDate">
          <a-date-picker
            style="width: 100%"
            placeholder="请选择入职时间"
            v-model="model.entryDate"
            :format="dateFormat"
            :getCalendarContainer="node => node.parentNode"/>
        </a-form-model-item>

        <a-form-model-item label="所属团队" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="teamId">
          <a-select v-model="model.teamId" placeholder="请选择所属团队">
            <a-select-option v-for="item in teamOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-model-item>

        <a-form-model-item label="直属上级" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="directSupervisor">
          <j-select-user-by-dep v-model="model.directSupervisor" :multi="false" placeholder="请选择直属上级"></j-select-user-by-dep>
        </a-form-model-item>

        <a-form-model-item label="紧急联系" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="emergencyContact">
          <a-input placeholder="请输入紧急联系人姓名" v-model="model.emergencyContact" />
        </a-form-model-item>

        <a-form-model-item label="紧急电话" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="emergencyPhone">
          <a-input placeholder="请输入紧急联系电话" v-model="model.emergencyPhone" />
        </a-form-model-item>

        <!-- 分隔线：以下为其他配置项 -->
        <a-divider>其他配置</a-divider>

        <template v-if="!model.id">
          <a-form-model-item label="登录密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="password" >
            <a-input type="password" placeholder="请输入登录密码" v-model="model.password" />
          </a-form-model-item>
  
          <a-form-model-item label="确认密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="confirmpassword" >
            <a-input type="password" @blur="handleConfirmBlur" placeholder="请重新输入登录密码" v-model="model.confirmpassword"/>
          </a-form-model-item>
        </template>

        <a-form-model-item label="操作角色" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="selectedOperationalRoles" v-show="!roleDisabled" >
          <j-multi-select-tag
            :disabled="disableSubmit"
            v-model="model.selectedOperationalRoles"
            :options="operationalRolesOptions"
            placeholder="请选择操作角色">
          </j-multi-select-tag>
        </a-form-model-item>

        <a-form-model-item label="数据角色" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="selectedDataRoles" v-show="!roleDisabled" >
          <j-multi-select-tag
            :disabled="disableSubmit"
            v-model="model.selectedDataRoles"
            :options="dataRolesOptions"
            placeholder="请选择数据角色">
          </j-multi-select-tag>
        </a-form-model-item>

        <a-form-model-item label="头像" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <j-image-upload class="avatar-uploader" text="上传" v-model="model.avatar" ></j-image-upload>
        </a-form-model-item>

      </a-form-model>
    </a-spin>


    <div class="drawer-bootom-button" v-show="!disableSubmit">
      <a-popconfirm title="确定放弃编辑？" @confirm="handleCancel" okText="确定" cancelText="取消">
        <a-button style="margin-right: .8rem">取消</a-button>
      </a-popconfirm>
      <a-button @click="handleSubmit" type="primary" :loading="confirmLoading">提交</a-button>
    </div>
  </a-drawer>
</template>

<script>
  import moment from 'moment'
  import Vue from 'vue'
  import { ACCESS_TOKEN } from "@/store/mutation-types"
  import { getAction } from '@/api/manage'
  import { addUser,editUser,queryUserRole,queryall } from '@/api/api'
  import { disabledAuthFilter } from "@/utils/authFilter"
  import { duplicateCheck } from '@/api/api'

  export default {
    name: "UserModal",
    components: {
    },
    data () {
      return {
        departDisabled: false, //是否是我的部门调用该页面
        roleDisabled: false, //是否是角色维护调用该页面
        modalWidth:800,
        drawerWidth:700,
        modaltoggleFlag:true,
        confirmDirty: false,
        userId:"", //保存用户id
        disableSubmit:false,
        dateFormat:"YYYY-MM-DD",
        validatorRules:{
          username:[{required: true, message: '请输入用户账号!'},
                    {validator: this.validateUsername,}],
          password: [{required: true,pattern:/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,message: '密码由8位数字、大小写字母和特殊符号组成!'},
                     {validator: this.validateToNextPassword,trigger: 'change'}],
          confirmpassword: [{required: true, message: '请重新输入登录密码!',},
                            { validator: this.compareToFirstPassword,}],
          realname:[{ required: true, message: '请输入用户名称!' }],
          phone: [{required: true, message: '请输入手机号!'}, {validator: this.validatePhone}],
          roles:{},
          workNo:[ { required: true, message: '请输入工号' },
                  { validator: this.validateWorkNo }],
          sex: [{ required: true, message: '请选择性别!' }],
          teamId: [{ required: true, message: '请选择所属团队!' }],
          selectedOperationalRoles: [{ required: false, message: '请选择操作角色!' }],
          selectedDataRoles: [{ required: false, message: '请选择数据角色!' }],
          birthday: [{ required: true, message: '请选择员工生日!' }],
          directSupervisor: [{ required: true, message: '请选择直属上级!' }],
          emergencyContact: [{ required: true, message: '请输入紧急联系人姓名!' }],
          emergencyPhone: [
            { required: true, message: '请输入紧急联系电话!' },
            { pattern: /^1[3|4|5|7|8|9][0-9]\d{8}$/, message: '请输入正确格式的手机号码' }
          ],
          entryDate: [{ required: true, message: '请选择入职时间!' }]
        },
        title:"操作",
        visible: false,
        model: {},
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        uploadLoading:false,
        confirmLoading: false,
        headers:{},
        url: {
          fileUpload: window._CONFIG['domianURL']+"/sys/common/upload",
          userWithDepart: "/sys/user/userDepartList", // 引入为指定用户查看部门信息需要的url
          userId:"/sys/user/generateUserId", // 引入生成添加用户情况下的url
          syncUserByUserName:"/act/process/extActProcess/doSyncUserByUserName",//同步用户到工作流
          // queryTenantList: '/sys/tenant/queryList' // 租户管理功能已移除
        },
        // tenantsOptions: [], // 租户管理功能已移除
        rolesOptions:[], // 保留用于兼容
        operationalRolesOptions:[], // 操作角色选项
        dataRolesOptions:[], // 数据角色选项
        teamOptions:[],
      }
    },
    created () {
      const token = Vue.ls.get(ACCESS_TOKEN);
      this.headers = {"X-Access-Token":token}
      this.initRoleList()
      this.initTeamList()
      // this.initTenantList() // 租户管理功能已移除
    },
    computed:{
      uploadAction:function () {
        return this.url.fileUpload;
      }
    },
    methods: {
      add () {
        this.refresh();
        this.edit({
          selectedOperationalRoles: '',
          selectedDataRoles: '',
          selectedroles: ''
        });
      },
      edit (record) {
        let that = this;
        that.visible = true;
        //根据屏幕宽度自适应抽屉宽度
        this.resetScreenSize();
        that.userId = record.id;
        that.model = Object.assign({},{selectedroles:'',selectedOperationalRoles:'',selectedDataRoles:'',selecteddeparts:''}, record);
        
        // 处理日期字段，将字符串转换为moment对象
        if (that.model.birthday) {
          that.model.birthday = moment(that.model.birthday, 'YYYY-MM-DD');
        }
        if (that.model.entryDate) {
          that.model.entryDate = moment(that.model.entryDate, 'YYYY-MM-DD');
        }

        if(record.hasOwnProperty("id")){
          that.getUserRoles(record.id);
        }
        console.log('that.model=',that.model)
      },
      isDisabledAuth(code){
        return disabledAuthFilter(code);
      },
      //窗口最大化切换
      toggleScreen(){
        if(this.modaltoggleFlag){
          this.modalWidth = window.innerWidth;
        }else{
          this.modalWidth = 800;
        }
        this.modaltoggleFlag = !this.modaltoggleFlag;
      },
      // 根据屏幕变化,设置抽屉尺寸
      resetScreenSize(){
        let screenWidth = document.body.clientWidth;
        if(screenWidth < 500){
          this.drawerWidth = screenWidth;
        }else{
          this.drawerWidth = 700;
        }
      },
      //初始化租户字典 - 租户管理功能已移除
      // initTenantList(){
      //   getAction(this.url.queryTenantList).then(res=>{
      //     if(res.success){
      //       this.tenantsOptions = res.result.map((item,index,arr)=>{
      //         let c = {label:item.name, value: item.id+""}
      //         return c;
      //       })
      //       console.log('this.tenantsOptions: ',this.tenantsOptions)
      //     }
      //   })
      // },
      //初始化角色字典（分别加载操作角色和数据角色）
      initRoleList(){
        queryall().then((res)=>{
          if(res.success){
            // 保留原有的rolesOptions用于兼容
            this.rolesOptions = res.result.map((item,index,arr)=>{
              let c = {label:item.roleName, value:item.id}
              return c;
            })
            // 分离操作角色和数据角色
            this.operationalRolesOptions = res.result
              .filter(item => item.roleType === 1 || item.roleType === '1') // 操作角色
              .map((item,index,arr)=>{
                return {label:item.roleName, value:item.id}
              })
            this.dataRolesOptions = res.result
              .filter(item => item.roleType === 2 || item.roleType === '2') // 数据角色
              .map((item,index,arr)=>{
                return {label:item.roleName, value:item.id}
              })
            console.log('操作角色选项: ',this.operationalRolesOptions)
            console.log('数据角色选项: ',this.dataRolesOptions)
          }
        });
      },
      //初始化团队列表
      initTeamList(){
        getAction('/sys/team/listAll').then((res)=>{
          if(res.success && res.result){
            this.teamOptions = res.result.map(item => ({
              label: item.teamName,
              value: item.id
            }));
          }
        })
      },
      getUserRoles(userid){
        queryUserRole({userid:userid}).then((res)=>{
          if(res.success){
            const allRoleIds = res.result || []
            // 查询所有角色信息，分离操作角色和数据角色
            queryall().then((roleRes)=>{
              if(roleRes.success){
                const allRoles = roleRes.result || []
                // 分离操作角色ID和数据角色ID
                const operationalRoleIds = []
                const dataRoleIds = []
                
                allRoleIds.forEach(roleId => {
                  const role = allRoles.find(r => r.id === roleId)
                  if(role){
                    if(role.roleType === 1 || role.roleType === '1'){
                      operationalRoleIds.push(roleId)
                    } else if(role.roleType === 2 || role.roleType === '2'){
                      dataRoleIds.push(roleId)
                    }
                  }
                })
                
                // 设置操作角色和数据角色
                this.model.selectedOperationalRoles = operationalRoleIds.join(",")
                this.model.selectedDataRoles = dataRoleIds.join(",")
                // 保留selectedroles用于兼容后端
                this.model.selectedroles = allRoleIds.join(",")
                console.log('操作角色IDs: ',this.model.selectedOperationalRoles)
                console.log('数据角色IDs: ',this.model.selectedDataRoles)
              }
            })
          }
        });
      },
      refresh () {
        this.userId=""
      },
      close () {
        this.$emit('close');
        this.visible = false;
        this.disableSubmit = false;
        this.$refs.form.resetFields();
      },
      moment,
      handleSubmit () {
        const that = this;
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            that.confirmLoading = true;
            // 格式化日期字段
            let submitData = Object.assign({}, this.model);
            if (submitData.birthday && moment.isMoment(submitData.birthday)) {
              submitData.birthday = submitData.birthday.format('YYYY-MM-DD');
            }
            if (submitData.entryDate && moment.isMoment(submitData.entryDate)) {
              submitData.entryDate = submitData.entryDate.format('YYYY-MM-DD');
            }
            // 合并操作角色和数据角色为selectedroles
            const operationalRoleIds = submitData.selectedOperationalRoles ? submitData.selectedOperationalRoles.split(',').filter(id => id) : []
            const dataRoleIds = submitData.selectedDataRoles ? submitData.selectedDataRoles.split(',').filter(id => id) : []
            submitData.selectedroles = [...operationalRoleIds, ...dataRoleIds].join(',')
            let obj;
            if(!submitData.id){
              submitData.id = this.userId;
              obj=addUser(submitData);
            }else{
              obj=editUser(submitData);
            }
            obj.then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
              that.close();
            })
          }else{
            return false;
          }
        })
      },
      handleCancel () {
        this.close()
      },
      validateToNextPassword (rule, value, callback) {
        const confirmpassword=this.model.confirmpassword;
        if (value && confirmpassword && value !== confirmpassword) {
          callback('两次输入的密码不一样！');
        }
        if (value && this.confirmDirty) {
          this.$refs.form.validateField(['confirmpassword']);
        }
        callback();
      },
      compareToFirstPassword (rule, value, callback) {
        if (value && value !== this.model.password) {
          callback('两次输入的密码不一样！');
        } else {
          callback()
        }
      },
      validatePhone(rule, value, callback){
        if(!value){
          callback()
        }else{
          if(new RegExp(/^1[3|4|5|7|8|9][0-9]\d{8}$/).test(value)){
            var params = {
              tableName: 'sys_user',
              fieldName: 'phone',
              fieldVal: value,
              dataId: this.userId
            };
            duplicateCheck(params).then((res) => {
              if (res.success) {
                callback()
              } else {
                callback("手机号已存在!")
              }
            })
          }else{
            callback("请输入正确格式的手机号码!");
          }
        }
      },
      validateUsername(rule, value, callback){
        var params = {
          tableName: 'sys_user',
          fieldName: 'username',
          fieldVal: value,
          dataId: this.userId
        };
        duplicateCheck(params).then((res) => {
          if (res.success) {
          callback()
        } else {
          callback("用户名已存在!")
        }
      })
      },
      validateWorkNo(rule, value, callback){
        var params = {
          tableName: 'sys_user',
          fieldName: 'work_no',
          fieldVal: value,
          dataId: this.userId
        };
        duplicateCheck(params).then((res) => {
          if (res.success) {
            callback()
          } else {
            callback("工号已存在!")
          }
        })
      },
      handleConfirmBlur(e) {
        const value = e.target.value;
        this.confirmDirty = this.confirmDirty || !!value
      },
      beforeUpload: function(file){
        var fileType = file.type;
        if(fileType.indexOf('image')<0){
          this.$message.warning('请上传图片');
          return false;
        }
        //TODO 验证文件大小
      },
    }
  }
</script>

<style scoped>
  .avatar-uploader > .ant-upload {
    width:104px;
    height:104px;
  }
  .ant-upload-select-picture-card i {
    font-size: 49px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }

  .ant-table-tbody .ant-table-row td{
    padding-top:10px;
    padding-bottom:10px;
  }

  .drawer-bootom-button {
    position: absolute;
    bottom: -8px;
    width: 100%;
    border-top: 1px solid #e8e8e8;
    padding: 10px 16px;
    text-align: right;
    left: 0;
    background: #fff;
    border-radius: 0 0 2px 2px;
  }
</style>