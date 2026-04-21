<template>

  <a-select
    v-if="async"
    showSearch
    labelInValue
    :disabled="disabled"
    :getPopupContainer="getParentContainer"
    @search="loadData"
    :placeholder="placeholder"
    :value="selectedAsyncValue"
    style="width: 100%"
    :filterOption="false"
    @change="handleAsyncChange"
    allowClear
    :notFoundContent="loading ? undefined : null"
  >
    <a-spin v-if="loading" slot="notFoundContent" size="small"/>
    <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option>
  </a-select>

  <a-select
    v-else
    :getPopupContainer="getParentContainer"
    showSearch
    :disabled="disabled"
    :placeholder="placeholder"
    optionFilterProp="children"
    style="width: 100%"
    @change="handleChange"
    :filterOption="filterOption"
    v-model="selectedValue"
    allowClear
    :notFoundContent="loading ? undefined : null">
    <a-spin v-if="loading" slot="notFoundContent" size="small"/>
    <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option>
  </a-select>

</template>

<script>
  import { ajaxGetDictItems,getDictItemsFromCache } from '@/api/api'
  import debounce from 'lodash/debounce';
  import { getAction } from '../../api/manage'

  export default {
    name: 'JSearchSelectTag',
    props:{
      disabled: Boolean,
      value: [String, Number],
      dict: String,
      dictOptions: Array,
      async: Boolean,
      placeholder:{
        type:String,
        default:"请选择",
        required:false
      },
      popContainer:{
        type:String,
        default:'',
        required:false
      },
      pageSize:{
        type: Number,
        default: 10,
        required: false
      },
      getPopupContainer: {
        type:Function,
        default: null
      },
    },
    data(){
      this.loadData = debounce(this.loadData, 800);//消抖
      this.lastLoad = 0;
      return {
        loading:false,
        selectedValue:[],
        selectedAsyncValue:undefined,  // async模式下初始化为undefined
        options: [],
      }
    },
    created(){
      this.initDictData();
    },
    watch:{
      "value":{
        immediate:true,
        handler(val){
          if(this.async){
            // async模式下，value变化时重新初始化已选值
            // 避免重复调用：只有当value有值且selectedAsyncValue未设置或key不匹配时才调用
            if(val && (!this.selectedAsyncValue || !this.selectedAsyncValue.key || this.selectedAsyncValue.key != val)){
              this.initSelectValue()
            } else if(!val){
              this.selectedAsyncValue = undefined
            }
          }else{
          if(!val){
            if(val==0){
              this.initSelectValue()
            }else{
              this.selectedValue=[]
            }
          }else{
            this.initSelectValue()
            }
          }
        }
      },
      "dict":{
        handler(){
          this.initDictData()
        }
      },
      'dictOptions':{
        deep: true,
        handler(val){
          if(val && val.length>0){
            this.options = [...val]
          }
        }
      }
    },
    methods:{
      initSelectValue(){
        if(this.async){
          // 如果value有值且selectedAsyncValue还没有设置，或者key不匹配，则加载
          if(this.value && (!this.selectedAsyncValue || !this.selectedAsyncValue.key || this.selectedAsyncValue.key!=this.value)){
            getAction(`/sys/dict/loadDictItem/${this.dict}`,{key:this.value, delNotExist: false}).then(res=>{
              if(res.success){
                // 后端返回的是List<String>，取第一个元素，如果没有则使用key本身
                let label = res.result && res.result.length > 0 ? res.result[0] : this.value
                let obj = {
                  key:this.value,
                  label:label
                }
                this.$set(this, 'selectedAsyncValue', obj)
              } else {
                // 如果请求失败，使用key本身作为label
                let obj = {
                  key:this.value,
                  label:this.value
                }
                this.$set(this, 'selectedAsyncValue', obj)
              }
            }).catch(err => {
              // 如果请求出错，使用key本身作为label
              let obj = {
                key:this.value,
                label:this.value
              }
              this.$set(this, 'selectedAsyncValue', obj)
            })
          } else if(!this.value) {
            // 如果value为空，清空selectedAsyncValue
            this.selectedAsyncValue = undefined
          }
        }else{
          this.selectedValue = this.value ? this.value.toString() : undefined
        }
      },
      loadData(value){
        this.lastLoad +=1
        const currentLoad = this.lastLoad
        this.options = []
        this.loading=true
        // 字典code格式：table,text,code
        getAction(`/sys/dict/loadDict/${this.dict}`,{keyword:value, pageSize: this.pageSize}).then(res=>{
          this.loading=false
          if(res.success){
            if(currentLoad!=this.lastLoad){
              return
            }
            this.options = res.result
          }else{
            this.$message.warning(res.message)
          }

        })

      },
      initDictData(){
        if(!this.async){
          //如果字典项集合有数据
          if(this.dictOptions && this.dictOptions.length>0){
            this.options = [...this.dictOptions]
          }else{
            //根据字典Code, 初始化字典数组
            let dictStr = ''
            if(this.dict){
                let arr = this.dict.split(',')
                if(arr[0].indexOf('where')>0){
                  let tbInfo = arr[0].split('where')
                  dictStr = tbInfo[0].trim()+','+arr[1]+','+arr[2]+','+encodeURIComponent(tbInfo[1])
                }else{
                  dictStr = this.dict
                }
                if (this.dict.indexOf(",") == -1) {
                  //优先从缓存中读取字典配置
                  if (getDictItemsFromCache(this.dictCode)) {
                    this.options = getDictItemsFromCache(this.dictCode);
                    return
                  }
                }
                ajaxGetDictItems(dictStr, null).then((res) => {
                  if (res.success) {
                    this.options = res.result;
                  }
                })
            }
          }
        }else{
          //异步一开始也加载一点数据
          this.loading=true
          getAction(`/sys/dict/loadDict/${this.dict}`,{pageSize: this.pageSize, keyword:''}).then(res=>{
            this.loading=false
            if(res.success){
              this.options = res.result
            }else{
              this.$message.warning(res.message)
            }
          })
        }
      },
      filterOption(input, option) {
        return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      },
      handleChange (selectedValue) {
        console.log("selectedValue",selectedValue)
        this.selectedValue = selectedValue
        this.callback()
      },
      handleAsyncChange(selectedObj){
        //update-begin-author:scott date:20201222 for:【搜索】搜索查询组件，删除条件，默认下拉还是上次的缓存数据，不好 JT-191
        if(selectedObj){
          this.selectedAsyncValue = selectedObj
          this.selectedValue = selectedObj.key
        }else{
          this.selectedAsyncValue = undefined
          this.selectedValue = undefined
          this.options = []
          this.loadData("")
        }
        this.callback()
        //update-end-author:scott date:20201222 for:【搜索】搜索查询组件，删除条件，默认下拉还是上次的缓存数据，不好 JT-191
      },
      callback(){
        this.$emit('change', this.selectedValue);
      },
      setCurrentDictOptions(dictOptions){
        this.options = dictOptions
      },
      getCurrentDictOptions(){
        return this.options
      },
      getParentContainer(node){
        if(typeof this.getPopupContainer === 'function'){
          return this.getPopupContainer(node)
        } else if(!this.popContainer){
          return node.parentNode
        }else{
          return document.querySelector(this.popContainer)
        }
      },

    },
    model: {
      prop: 'value',
      event: 'change'
    }
  }
</script>

<style scoped>

</style>