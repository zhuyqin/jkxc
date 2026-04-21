<template>
  <div>
    <a-input-search
      v-model="textVals"
      placeholder="请先选择用户"
      readOnly
      unselectable="on"
      @search="onSearchDepUser">
      <a-button slot="enterButton" :disabled="disabled">选择用户</a-button>
    </a-input-search>
    <j-select-user-by-dep-modal
      ref="selectModal"
      :modal-width="modalWidth"
      :multi="multi"
      @ok="selectOK"
      :user-ids="value"
      :store="storeField"
      :text="textField"
      :role-id="roleId"
      @initComp="initComp"/>
  </div>
</template>

<script>
  import JSelectUserByDepModal from './modal/JSelectUserByDepModal'
  import { underLinetoHump } from '@/components/_util/StringUtil'

  export default {
    name: 'JSelectUserByDep',
    components: {JSelectUserByDepModal},
    props: {
      modalWidth: {
        type: Number,
        default: 1250,
        required: false
      },
      value: {
        type: String,
        required: false
      },
      disabled: {
        type: Boolean,
        required: false,
        default: false
      },
      multi: {
        type: Boolean,
        default: true,
        required: false
      },
      backUser: {
        type: Boolean,
        default: false,
        required: false
      },
      // 存储字段 [key field]
      store: {
        type: String,
        default: 'username',
        required: false
      },
      // 显示字段 [label field]
      text: {
        type: String,
        default: 'realname',
        required: false
      },
      // 角色ID（用于过滤用户）
      roleId: {
        type: String,
        default: null,
        required: false
      }
    },
    data() {
      return {
        storeVals: '', //[key values]
        textVals: '' //[label values]
      }
    },
    computed:{
      storeField(){
        let field = this.customReturnField
        if(!field){
          field = this.store;
        }
        return underLinetoHump(field)
      },
      textField(){
        return underLinetoHump(this.text)
      }
    },
    mounted() {
      this.storeVals = this.value
    },
    watch: {
      value(val) {
        this.storeVals = val
      }
    },
    model: {
      prop: 'value',
      event: 'change'
    },
    methods: {
      initComp(textVals) {
        this.textVals = textVals
      },
      //返回选中的用户信息
      backDeparInfo(){
        if(this.backUser===true){
          if(this.storeVals && this.storeVals.length>0){
            let arr1 = this.storeVals.split(',')
            let arr2 = this.textVals.split(',')
            let info = []
            for(let i=0;i<arr1.length;i++){
              info.push({
                value: arr1[i],
                text: arr2[i]
              })
            }
            this.$emit('back', info)
          }
        }
      },
      onSearchDepUser() {
        console.log('[JSelectUserByDep] onSearchDepUser被调用, roleId:', this.roleId)
        if (this.$refs.selectModal) {
          this.$refs.selectModal.showModal()
        } else {
          console.error('[JSelectUserByDep] selectModal ref不存在')
        }
      },
      selectOK(rows) {
        console.log("当前选中用户", rows)
        if (!rows) {
          this.storeVals = ''
          this.textVals = ''
        } else {
          let temp1 = []
          let temp2 = []
          for (let item of rows) {
            temp1.push(item[this.storeField])
            temp2.push(item[this.textField])
          }
          this.storeVals = temp1.join(',')
          this.textVals = temp2.join(',')
        }
        this.$emit("change", this.storeVals)
      }
    }
  }
</script>

<style scoped>

</style>