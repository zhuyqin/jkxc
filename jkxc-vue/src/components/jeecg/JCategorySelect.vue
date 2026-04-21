<template>
  <a-tree-select
    allowClear
    labelInValue
    style="width: 100%"
    :disabled="disabled"
    :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
    :placeholder="placeholder"
    :loadData="asyncLoadTreeData"
    :value="treeValue"
    :treeData="treeData"
    :multiple="multiple"
    @change="onChange">
  </a-tree-select>
</template>
<script>

  import { getAction } from '@/api/manage'

  export default {
    name: 'JCategorySelect',
    props: {
      value:{
        type: String,
        required: false
      },
      placeholder:{
        type: String,
        default: '请选择',
        required: false
      },
      disabled:{
        type:Boolean,
        default:false,
        required:false
      },
      condition:{
        type:String,
        default:'',
        required:false
      },
      // 是否支持多选
      multiple: {
        type: Boolean,
        default: false,
      },
      loadTriggleChange:{
        type: Boolean,
        default: false,
        required:false
      },
      pid:{
        type:String,
        default:'',
        required:false
      },
      pcode:{
        type:String,
        default:'',
        required:false
      },
      back:{
        type:String,
        default:'',
        required:false
      }
    },
    data () {
      return {
        treeValue:"",
        treeData:[],
        url:"/sys/category/loadTreeData",
        view:'/sys/category/loadDictItem/',
        tableName:"",
        text:"",
        code:"",

      }
    },
    watch: {
      value () {
        this.loadItemByCode()
      },
      pcode(){
        this.loadRoot();
      }
    },
    created(){
      this.validateProp().then(()=>{
        this.loadRoot()
        this.loadItemByCode()
      })
    },
    methods: {
      /**加载一级节点 */
      loadRoot(){
        let param = {
          pid:this.pid,
          pcode:!this.pcode?'0':this.pcode,
          condition:this.condition
        }
        getAction(this.url,param).then(res=>{
          if(res.success && res.result){
            for(let i of res.result){
              i.value = i.key
              if(i.leaf==false){
                i.isLeaf=false
                i.disabled=true  // 非叶子节点禁用，只能选择最底层节点
              }else if(i.leaf==true){
                i.isLeaf=true
                i.disabled=false  // 叶子节点可选中
              }
            }
            this.treeData = [...res.result]
          }else{
            console.log("树一级节点查询结果-else",res)
          }
        })
      },

      /** 数据回显*/
      loadItemByCode(){
        if(!this.value || this.value=="0"){
          this.treeValue = []
        }else{
          // 过滤掉空值
          const validValues = this.value.split(',').filter(v => v && v.trim() && v !== 'null' && v !== 'undefined')
          if(validValues.length === 0){
            this.treeValue = []
            return
          }
          getAction(this.view,{ids:validValues.join(',')}).then(res=>{
            if(res.success && res.result){
              // 确保values和result长度一致，过滤掉空值
              const filteredResult = res.result.filter((item, index) => {
                return item && validValues[index] && validValues[index].trim()
              })
              const filteredValues = validValues.filter((v, index) => {
                return res.result[index] && res.result[index].trim()
              })
              this.treeValue = filteredResult.map((item, index) => ({
                key: filteredValues[index],
                value: filteredValues[index],
                label: item
              }))
              if(filteredResult.length > 0){
                this.onLoadTriggleChange(filteredResult[0]);
              }
            }else{
              this.treeValue = []
            }
          })
        }
      },
      onLoadTriggleChange(text){
        //只有单选才会触发
        if(!this.multiple && this.loadTriggleChange){
          this.backValue(this.value,text)
        }
      },
      backValue(value,label){
        let obj = {}
        if(this.back){
          obj[this.back] = label
        }
        this.$emit('change', value, obj)
      },
      asyncLoadTreeData (treeNode) {
        return new Promise((resolve) => {
          if (treeNode.$vnode.children) {
            resolve()
            return
          }
          let pid = treeNode.$vnode.key
          let param = {
            pid:pid,
            condition:this.condition
          }
          getAction(this.url,param).then(res=>{
            if(res.success){
              for(let i of res.result){
                i.value = i.key
                if(i.leaf==false){
                  i.isLeaf=false
                  i.disabled=true  // 非叶子节点禁用，只能选择最底层节点
                }else if(i.leaf==true){
                  i.isLeaf=true
                  i.disabled=false  // 叶子节点可选中
                }
              }
              this.addChildren(pid,res.result,this.treeData)
              this.treeData = [...this.treeData]
            }
            resolve()
          })
        })
      },
      addChildren(pid,children,treeArray){
        if(treeArray && treeArray.length>0){
          for(let item of treeArray){
            if(item.key == pid){
              if(!children || children.length==0){
                item.isLeaf=true
              }else{
                item.children = children
              }
              break
            }else{
              this.addChildren(pid,children,item.children)
            }
          }
        }
      },

      onChange(value){
        if(!value){
          this.$emit('change', '');
          this.treeValue = ''
        } else if (Array.isArray(value)) {
          // 过滤掉空值、null、undefined和无效的选项
          const validItems = value.filter(item => {
            return item && item.value && String(item.value).trim() && 
                   item.value !== 'null' && item.value !== 'undefined' &&
                   item.label && String(item.label).trim()
          })
          if(validItems.length === 0){
            this.$emit('change', '');
            this.treeValue = []
            return
          }
          let labels = []
          let values = validItems.map(item => {
            labels.push(item.label)
            return item.value
          })
          this.backValue(values.join(','), labels.join(','))
          this.treeValue = validItems
        } else {
          // 单选模式，也要检查值是否有效
          if(value.value && String(value.value).trim() && value.value !== 'null' && value.value !== 'undefined'){
            this.backValue(value.value,value.label)
            this.treeValue = value
          }else{
            this.$emit('change', '');
            this.treeValue = ''
          }
        }
      },
      getCurrTreeData(){
        return this.treeData
      },
      validateProp(){
        let mycondition = this.condition
        return new Promise((resolve,reject)=>{
          if(!mycondition){
            resolve();
          }else{
            try {
              let test=JSON.parse(mycondition);
              if(typeof test == 'object' && test){
                resolve()
              }else{
                this.$message.error("组件JTreeSelect-condition传值有误，需要一个json字符串!")
                reject()
              }
            } catch(e) {
              this.$message.error("组件JTreeSelect-condition传值有误，需要一个json字符串!")
              reject()
            }
          }
        })
      }
    },
    //2.2新增 在组件内定义 指定父组件调用时候的传值属性和事件类型 这个牛逼
    model: {
      prop: 'value',
      event: 'change'
    }
  }
</script>
