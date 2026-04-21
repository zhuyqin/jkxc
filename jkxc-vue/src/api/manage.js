import Vue from 'vue'
import { axios } from '@/utils/request'
import signMd5Utils from '@/utils/encryption/signMd5Utils'

const api = {
  user: '/mock/api/user',
  role: '/mock/api/role',
  service: '/mock/api/service',
  permission: '/mock/api/permission',
  permissionNoPager: '/mock/api/permission/no-pager'
}

export default api

//post
export function postAction(url,parameter) {
  let sign = signMd5Utils.getSign(url, parameter);
  //将签名和时间戳，添加在请求接口 Header
  let signHeader = {
    "X-Sign": sign,
    "X-TIMESTAMP": signMd5Utils.getDateTimeToString(),
    "Content-Type": "application/json;charset=UTF-8"
  };

  return axios({
    url: url,
    method:'post' ,
    data: parameter,
    headers: signHeader
  })
}

//post method= {post | put}
export function httpAction(url,parameter,method) {
  let sign = signMd5Utils.getSign(url, parameter);
  //将签名和时间戳，添加在请求接口 Header
  let signHeader = {
    "X-Sign": sign,
    "X-TIMESTAMP": signMd5Utils.getDateTimeToString(),
    "Content-Type": "application/json;charset=UTF-8"
  };

  return axios({
    url: url,
    method:method ,
    data: parameter,
    headers: signHeader
  })
}

//put
export function putAction(url,parameter) {
  return axios({
    url: url,
    method:'put',
    data: parameter,
    headers: {
      "Content-Type": "application/json;charset=UTF-8"
    }
  })
}

//get
export function getAction(url,parameter) {
  let sign = signMd5Utils.getSign(url, parameter);
  //将签名和时间戳，添加在请求接口 Header
  let signHeader = {"X-Sign": sign,"X-TIMESTAMP": signMd5Utils.getDateTimeToString()};

  return axios({
    url: url,
    method: 'get',
    params: parameter,
    headers: signHeader
  })
}

//get with custom timeout(ms)
export function getActionTimeout(url, parameter, timeoutMs) {
  let sign = signMd5Utils.getSign(url, parameter);
  let signHeader = {"X-Sign": sign, "X-TIMESTAMP": signMd5Utils.getDateTimeToString()};
  return axios({
    url: url,
    method: 'get',
    params: parameter,
    headers: signHeader,
    timeout: timeoutMs && timeoutMs > 0 ? timeoutMs : 60000
  })
}

//deleteAction
export function deleteAction(url,parameter) {
  return axios({
    url: url,
    method: 'delete',
    params: parameter
  })
}

export function getUserList(parameter) {
  return axios({
    url: api.user,
    method: 'get',
    params: parameter
  })
}

export function getRoleList(parameter) {
  return axios({
    url: api.role,
    method: 'get',
    params: parameter
  })
}

export function getServiceList(parameter) {
  return axios({
    url: api.service,
    method: 'get',
    params: parameter
  })
}

export function getPermissions(parameter) {
  return axios({
    url: api.permissionNoPager,
    method: 'get',
    params: parameter
  })
}

// id == 0 add     post
// id != 0 update  put
export function saveService(parameter) {
  return axios({
    url: api.service,
    method: parameter.id == 0 ? 'post' : 'put',
    data: parameter
  })
}

/**
 * 下载文件 用于excel导出
 * @param url
 * @param parameter
 * @returns {*}
 */
export function downFile(url,parameter){
  return axios({
    url: url,
    params: parameter,
    method:'get' ,
    responseType: 'blob'
  })
}

/**
 * 下载文件
 * @param url 文件路径
 * @param fileName 文件名
 * @param parameter
 * @returns {*}
 */
export function downloadFile(url, fileName, parameter) {
  return downFile(url, parameter).then((data) => {
    if (!data || data.size === 0) {
      Vue.prototype['$message'].warning('文件下载失败')
      return
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data]), fileName)
    } else {
      let url = window.URL.createObjectURL(new Blob([data]))
      let link = document.createElement('a')
      link.style.display = 'none'
      link.href = url
      link.setAttribute('download', fileName)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link) //下载完成移除元素
      window.URL.revokeObjectURL(url) //释放掉blob对象
    }
  })
}

/**
 * 文件上传 用于富文本上传图片
 * @param url
 * @param parameter
 * @returns {*}
 */
export function uploadAction(url,parameter){
  return axios({
    url: url,
    data: parameter,
    method:'post' ,
    headers: {
      'Content-Type': 'multipart/form-data',  // 文件上传
    },
  })
}

/**
 * 获取文件服务访问路径
 * @param avatar
 * @param subStr
 * @returns {*}
 */
export function getFileAccessHttpUrl(avatar,subStr) {
  if(!subStr) subStr = 'http'
  try {
    // 如果已经是完整的URL（以http或https开头），直接返回
    if(avatar && avatar.startsWith(subStr)){
      return avatar;
    }else{
      // 如果是相对路径，且不是JSON数组格式
      if(avatar && avatar.length>0 && avatar.indexOf('[')==-1){
        // 使用后端返回的minio URL，后端已经返回完整URL，这里直接返回
        // 如果后端返回的是相对路径，需要通过 /sys/common/static/ 接口访问
        // 检查是否是相对路径（不包含http）
        if(!avatar.startsWith('http') && !avatar.startsWith('/')) {
          // 相对路径，通过后端接口访问
          return '/sys/common/static/' + avatar;
        }
        return avatar;
      }
    }
  }catch(err){
   return;
  }
}
