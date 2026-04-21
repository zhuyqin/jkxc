import { getAction, deleteAction, putAction, postAction, httpAction } from '@/api/manage'
import Vue from 'vue'
import {UI_CACHE_DB_DICT_DATA } from "@/store/mutation-types"

//角色管理
const addRole = (params)=>postAction("/sys/role/add",params);
const editRole = (params)=>putAction("/sys/role/edit",params);
const checkRoleCode = (params)=>getAction("/sys/role/checkRoleCode",params);
const queryall = (params)=>getAction("/sys/role/queryall",params);
// 角色树结构查询
const queryRoleTreeList = (params)=>getAction("/sys/role/queryTreeList",params);
const queryRoleTreeListByPid = (params)=>getAction("/sys/role/queryTreeListByPid",params);

//用户管理
const addUser = (params)=>postAction("/sys/user/add",params);
const editUser = (params)=>putAction("/sys/user/edit",params);
const queryUserRole = (params)=>getAction("/sys/user/queryUserRole",params);
const getUserList = (params)=>getAction("/sys/user/list",params);
const frozenBatch = (params)=>putAction("/sys/user/frozenBatch",params);
//验证用户是否存在
const checkOnlyUser = (params)=>getAction("/sys/user/checkOnlyUser",params);
//改变密码
const changePassword = (params)=>putAction("/sys/user/changePassword",params);

//权限管理
const addPermission= (params)=>postAction("/sys/permission/add",params);
const editPermission= (params)=>putAction("/sys/permission/edit",params);
const getPermissionList = (params)=>getAction("/sys/permission/list",params);
const getSystemMenuList = (params)=>getAction("/sys/permission/getSystemMenuList",params);
const getSystemSubmenu = (params)=>getAction("/sys/permission/getSystemSubmenu",params);
const getSystemSubmenuBatch = (params) => getAction('/sys/permission/getSystemSubmenuBatch', params)
const queryTreeList = (params)=>getAction("/sys/permission/queryTreeList",params);
const queryTreeListForRole = (params)=>getAction("/sys/permission/queryTreeList",params);
const queryListAsync = (params)=>getAction("/sys/permission/queryListAsync",params);
const queryRolePermission = (params)=>getAction("/sys/permission/queryRolePermission",params);
const saveRolePermission = (params)=>postAction("/sys/permission/saveRolePermission",params);
const queryPermissionsByUser = ()=>getAction("/sys/permission/getUserPermissionByToken");
const loadAllRoleIds = (params)=>getAction("/sys/permission/loadAllRoleIds",params);
const getPermissionRuleList = (params)=>getAction("/sys/permission/getPermRuleListByPermId",params);
const queryPermissionRule = (params)=>getAction("/sys/permission/queryPermissionRule",params);

// 部门管理
const queryDepartTreeList = (params)=>getAction("/sys/sysDepart/queryTreeList",params);
const queryDepartTreeSync = (params)=>getAction("/sys/sysDepart/queryDepartTreeSync",params);
const queryIdTree = (params)=>getAction("/sys/sysDepart/queryIdTree",params);
const queryParentName   = (params)=>getAction("/sys/sysDepart/queryParentName",params);
const searchByKeywords   = (params)=>getAction("/sys/sysDepart/searchBy",params);
const deleteByDepartId   = (params)=>deleteAction("/sys/sysDepart/delete",params);

//二级部门管理
const queryDepartPermission = (params)=>getAction("/sys/permission/queryDepartPermission",params);
const saveDepartPermission = (params)=>postAction("/sys/permission/saveDepartPermission",params);
const queryTreeListForDeptRole = (params)=>getAction("/sys/sysDepartPermission/queryTreeListForDeptRole",params);
const queryDeptRolePermission = (params)=>getAction("/sys/sysDepartPermission/queryDeptRolePermission",params);
const saveDeptRolePermission = (params)=>postAction("/sys/sysDepartPermission/saveDeptRolePermission",params);
// const queryMyDepartTreeList = (params)=>getAction("/sys/sysDepart/queryMyDeptTreeList",params); // 我的部门功能已移除

//日志管理
const deleteLog = (params)=>deleteAction("/sys/log/delete",params);
const deleteLogList = (params)=>deleteAction("/sys/log/deleteBatch",params);

//数据字典
const addDict = (params)=>postAction("/sys/dict/add",params);
const editDict = (params)=>putAction("/sys/dict/edit",params);
const treeList = (params)=>getAction("/sys/dict/treeList",params);
const addDictItem = (params)=>postAction("/sys/dictItem/add",params);
const editDictItem = (params)=>putAction("/sys/dictItem/edit",params);
//获取公司信息
const getCompanyInformation = (params)=>getAction("/customer/ghCustomerInformation/queryById",params);
const updateCompanyInformation = (params)=>putAction("/customer/ghCustomerInformation/edit",params);
const updateGhContractCenter = (params)=>putAction("/contract/ghContractCenter/edit",params);
const reverseGhContractCenter = (params)=>putAction("/contract/ghContractCenter/reverse",params);

const getContractSteps = (params)=>getAction("/contract/ghContractCenter/getContractSteps",params);


//字典标签专用（通过code获取字典数组）
export const ajaxGetDictItems = (code, params)=>getAction(`/sys/dict/getDictItems/${code}`,params);
//从缓存中获取字典配置
function getDictItemsFromCache(dictCode) {
  if (Vue.ls.get(UI_CACHE_DB_DICT_DATA) && Vue.ls.get(UI_CACHE_DB_DICT_DATA)[dictCode]) {
    let dictItems = Vue.ls.get(UI_CACHE_DB_DICT_DATA)[dictCode];
    //console.log("-----------getDictItemsFromCache----------dictCode="+dictCode+"---- dictItems=",dictItems)
    return dictItems;
  }
}

//系统通告
const doReleaseData = (params)=>getAction("/sys/annountCement/doReleaseData",params);
const doReovkeData = (params)=>getAction("/sys/annountCement/doReovkeData",params);
//获取系统访问量
const getLoginfo = (params)=>getAction("/sys/loginfo",params);
const getVisitInfo = (params)=>getAction("/sys/visitInfo",params);

// 根据部门主键查询用户信息
const queryUserByDepId = (params)=>getAction("/sys/user/queryUserByDepId",params);

// 重复校验
const duplicateCheck = (params)=>getAction("/sys/duplicate/check",params);
// 加载分类字典
const loadCategoryData = (params)=>getAction("/sys/category/loadAllData",params);
const checkRuleByCode = (params) => getAction('/sys/checkRule/checkByCode', params)
//加载我的通告信息
const getUserNoticeInfo= (params)=>getAction("/sys/sysAnnouncementSend/getMyAnnouncementSend",params);
const getTransitURL = url => `/sys/common/transitRESTful?url=${encodeURIComponent(url)}`
// 中转HTTP请求
export const transitRESTful = {
  get: (url, parameter) => getAction(getTransitURL(url), parameter),
  post: (url, parameter) => postAction(getTransitURL(url), parameter),
  put: (url, parameter) => putAction(getTransitURL(url), parameter),
  http: (url, parameter) => httpAction(getTransitURL(url), parameter),
}

export {
  // imgView,
  // doMian,
  addRole,
  editRole,
  checkRoleCode,
  queryRoleTreeList,
  queryRoleTreeListByPid,
  addUser,
  editUser,
  queryUserRole,
  getUserList,
  queryall,
  frozenBatch,
  checkOnlyUser,
  changePassword,
  getPermissionList,
  addPermission,
  editPermission,
  queryTreeList,
  queryListAsync,
  queryRolePermission,
  saveRolePermission,
  queryPermissionsByUser,
  loadAllRoleIds,
  getPermissionRuleList,
  queryPermissionRule,
  queryDepartTreeList,
  queryDepartTreeSync,
  queryIdTree,
  queryParentName,
  searchByKeywords,
  deleteByDepartId,
  deleteLog,
  deleteLogList,
  addDict,
  editDict,
  treeList,
  addDictItem,
  editDictItem,
  doReleaseData,
  doReovkeData,
  getLoginfo,
  getVisitInfo,
  queryUserByDepId,
  duplicateCheck,
  queryTreeListForRole,
  getSystemMenuList,
  getSystemSubmenu,
  getSystemSubmenuBatch,
  loadCategoryData,
  checkRuleByCode,
  queryDepartPermission,
  saveDepartPermission,
  queryTreeListForDeptRole,
  queryDeptRolePermission,
  saveDeptRolePermission,
  // queryMyDepartTreeList, // 我的部门功能已移除
  getUserNoticeInfo,
  getDictItemsFromCache,
  getCompanyInformation,
  updateGhContractCenter,
  updateCompanyInformation,
  reverseGhContractCenter,
  getContractSteps,
  addAuditProcess,
  editAuditProcess,
  queryAuditProcessList,
  queryAuditProcessById,
  deleteAuditProcess,
  deleteBatchAuditProcess,
  queryAllAuditProcess,
  saveStepForm,
  getStepForm,
  getProcessByBusinessType,
  queryAuditTaskById,
  getPendingTasks,
  getApprovedTasks,
  getRejectedTasks,
  executeAudit,
  resubmitTask,
  getOrderAuditProgress,
  // 订单管理
  addOrder,
  editOrder,
  queryOrderList,
  queryOrderById,
  deleteOrder,
  deleteBatchOrder,
  getOrderSteps,
  getPaymentList,
  getExpenseList,
  // Customer Management APIs
  addCustomer,
  editCustomer,
  queryCustomerList,
  queryCustomerById,
    deleteCustomer,
    deleteBatchCustomer,
    exportCustomerXls,
    importCustomerExcel,
    queryCustomerByLegalPersonPhone,
    // 动态表单管理
    addDynamicForm,
    editDynamicForm,
    queryDynamicFormList,
    queryDynamicFormById,
    deleteDynamicForm,
    deleteBatchDynamicForm,
    createFormVersion,
    getFormVersions,
    getFormVersionByBusinessType,
    bindBusinessType,
    getBindingList,
    queryVersionById,
    copyDynamicForm,
    // 商机管理
    addOpportunity,
    editOpportunity,
    queryOpportunityList,
    queryOpportunityById,
    deleteOpportunity,
    deleteBatchOpportunity,
    exportOpportunityXls,
    importOpportunityExcel,
    // 跟进记录
    addFollowupDetail,
    editFollowupDetail,
    queryFollowupDetailList,
    queryFollowupDetailById,
    deleteFollowupDetail,
    deleteBatchFollowupDetail
}

// 导出基础方法供其他模块使用
export { deleteAction, getAction, postAction, putAction, httpAction }

//订单管理
const addOrder = (params)=>postAction("/order/add",params);
const editOrder = (params)=>putAction("/order/edit",params);
const queryOrderList = (params)=>getAction("/order/list",params);
const queryOrderById = (params)=>getAction("/order/queryById",params);
const deleteOrder = (params)=>deleteAction("/order/delete",params);
const deleteBatchOrder = (params)=>deleteAction("/order/deleteBatch",params);
const getOrderSteps = (params)=>getAction("/order/getOrderSteps",params);
const getPaymentList = (params)=>getAction("/order/getPaymentList",params);
const getExpenseList = (params)=>getAction("/order/getExpenseList",params);

// 客户管理
const addCustomer = (params)=>postAction("/customer/add",params);
const editCustomer = (params)=>putAction("/customer/edit",params);
const queryCustomerList = (params)=>getAction("/customer/list",params);
const queryCustomerById = (params)=>getAction("/customer/queryById",params);
const deleteCustomer = (params)=>deleteAction("/customer/delete",params);
const deleteBatchCustomer = (params)=>deleteAction("/customer/deleteBatch",params);
const exportCustomerXls = (params)=>downFile("/customer/exportXls",params);
const importCustomerExcel = (params)=>uploadFile("/customer/importExcel",params);
const queryCustomerByLegalPersonPhone = (params)=>getAction("/customer/queryByLegalPersonPhone",params);

//审核流程管理
const addAuditProcess = (params)=>postAction("/sys/auditProcess/add",params);
const editAuditProcess = (params)=>putAction("/sys/auditProcess/edit",params);
const queryAuditProcessList = (params)=>getAction("/sys/auditProcess/list",params);
const queryAuditProcessById = (params)=>getAction("/sys/auditProcess/queryById",params);
const deleteAuditProcess = (params)=>deleteAction("/sys/auditProcess/delete",params);
const deleteBatchAuditProcess = (params)=>deleteAction("/sys/auditProcess/deleteBatch",params);
const queryAllAuditProcess = (params)=>getAction("/sys/auditProcess/queryAll",params);
const saveStepForm = (params)=>postAction("/sys/auditProcess/saveStepForm",params);
const getStepForm = (params)=>getAction("/sys/auditProcess/getStepForm",params);
const getProcessByBusinessType = (params)=>getAction("/sys/auditProcess/getProcessByBusinessType",params);

//审核任务管理
const queryAuditTaskById = (params)=>getAction("/sys/auditTask/queryById",params);
const getPendingTasks = (params)=>getAction("/sys/auditTask/getPendingTasks",params);
const getApprovedTasks = (params)=>getAction("/sys/auditTask/getApprovedTasks",params);
const getRejectedTasks = (params)=>getAction("/sys/auditTask/getRejectedTasks",params);
const executeAudit = (params)=>postAction("/sys/auditTask/executeAudit",params);
const resubmitTask = (params)=>postAction("/sys/auditTask/resubmit",params);
const getOrderAuditProgress = (params)=>getAction("/sys/auditTask/getOrderAuditProgress",params);

//动态表单管理
const addDynamicForm = (params)=>postAction("/dynamicform/form/add",params);
const editDynamicForm = (params)=>putAction("/dynamicform/form/edit",params);
const queryDynamicFormList = (params)=>getAction("/dynamicform/form/list",params);
const queryDynamicFormById = (params)=>getAction("/dynamicform/form/queryById",params);
const deleteDynamicForm = (params)=>deleteAction("/dynamicform/form/delete",params);
const deleteBatchDynamicForm = (params)=>deleteAction("/dynamicform/form/deleteBatch",params);
const createFormVersion = (params)=>postAction("/dynamicform/form/createVersion",params);
const getFormVersions = (params)=>getAction("/dynamicform/form/getVersions",params);
const getFormVersionByBusinessType = (params)=>getAction("/dynamicform/form/getFormVersionByBusinessType",params);
const bindBusinessType = (params)=>postAction("/dynamicform/form/bindBusinessType",params);
const getBindingList = (params)=>getAction("/dynamicform/form/getBindingList",params);
const queryVersionById = (params)=>getAction("/dynamicform/form/queryVersionById",params);
const copyDynamicForm = (params)=>postAction("/dynamicform/form/copyForm",params);

// 商机管理
const addOpportunity = (params)=>postAction("/opportunity/ghOpportunity/add",params);
const editOpportunity = (params)=>putAction("/opportunity/ghOpportunity/edit",params);
const queryOpportunityList = (params)=>getAction("/opportunity/ghOpportunity/list",params);
const queryOpportunityById = (params)=>getAction("/opportunity/ghOpportunity/queryById",params);
const deleteOpportunity = (params)=>deleteAction("/opportunity/ghOpportunity/delete",params);
const deleteBatchOpportunity = (params)=>deleteAction("/opportunity/ghOpportunity/deleteBatch",params);
const exportOpportunityXls = (params)=>downFile("/opportunity/ghOpportunity/exportXls",params);
const importOpportunityExcel = (params)=>uploadFile("/opportunity/ghOpportunity/importExcel",params);

// 跟进记录
const addFollowupDetail = (params)=>postAction("/followup/ghFollowupDetail/add",params);
const editFollowupDetail = (params)=>putAction("/followup/ghFollowupDetail/edit",params);
const queryFollowupDetailList = (params)=>getAction("/followup/ghFollowupDetail/list",params);
const queryFollowupDetailById = (params)=>getAction("/followup/ghFollowupDetail/queryById",params);
const deleteFollowupDetail = (params)=>deleteAction("/followup/ghFollowupDetail/delete",params);
const deleteBatchFollowupDetail = (params)=>deleteAction("/followup/ghFollowupDetail/deleteBatch",params);
