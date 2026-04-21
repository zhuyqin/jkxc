import Vue from 'vue'
import router from './router'
import store from './store'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import notification from 'ant-design-vue/es/notification'
import { ACCESS_TOKEN,INDEX_MAIN_PAGE_PATH, OAUTH2_LOGIN_PAGE_PATH } from '@/store/mutation-types'
import { generateIndexRouter, isOAuth2AppEnv } from '@/utils/util'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/user/login', '/user/register', '/user/register-result','/user/alteration'] // no redirect whitelist
whiteList.push(OAUTH2_LOGIN_PAGE_PATH)

router.beforeEach((to, from, next) => {
  NProgress.start() // start progress bar

  if (Vue.ls.get(ACCESS_TOKEN)) {
    /* has token */
    if (to.path === '/user/login' || to.path === OAUTH2_LOGIN_PAGE_PATH) {
      const homepagePath = Vue.ls.get(INDEX_MAIN_PAGE_PATH) || '/dashboard/analysis'
      next({ path: homepagePath })
      NProgress.done()
    } else {
      if (store.getters.permissionList.length === 0) {
        store.dispatch('GetPermissionList').then(res => {
              const menuData = res.result.menu;
              //console.log(res.message)
              if (menuData === null || menuData === "" || menuData === undefined) {
                return;
              }
              let constRoutes = [];
              constRoutes = generateIndexRouter(menuData);
              // 添加主界面路由
              store.dispatch('UpdateAppRouter',  { constRoutes }).then(() => {
                // 根据roles权限生成可访问的路由表
                // 动态添加可访问路由表
                router.addRoutes(store.getters.addRouters)
                // 跳转到目标路由，如果目标路由就是当前路由，则使用replace
                const targetPath = to.path
                if (to.matched.length === 0) {
                  // 如果路由未匹配（可能是动态路由还未注册），跳转到首页
                  const homepagePath = Vue.ls.get(INDEX_MAIN_PAGE_PATH) || '/dashboard/analysis'
                  next({ path: homepagePath, replace: true })
                } else {
                  next({ ...to, replace: true })
                }
              })
            })
          .catch(() => {
           /* notification.error({
              message: '系统提示',
              description: '请求用户信息失败，请重试！'
            })*/
            store.dispatch('Logout').then(() => {
              next({ path: '/user/login', query: { redirect: to.fullPath } })
            })
          })
      } else {
        // 如果路由未匹配（可能是动态路由还未注册），跳转到首页
        if (to.matched.length === 0 && to.path !== '/user/login') {
          const homepagePath = Vue.ls.get(INDEX_MAIN_PAGE_PATH) || '/dashboard/analysis'
          next({ path: homepagePath, replace: true })
        } else {
          next()
        }
      }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      // 在免登录白名单，如果进入的页面是login页面并且当前是OAuth2app环境，就进入OAuth2登录页面
      if (to.path === '/user/login' && isOAuth2AppEnv()) {
        next({path: OAUTH2_LOGIN_PAGE_PATH})
      } else {
        // 在免登录白名单，直接进入
        next()
      }
      NProgress.done()
    } else {
      // 如果当前是在OAuth2APP环境，就跳转到OAuth2登录页面
      let path = isOAuth2AppEnv() ? OAUTH2_LOGIN_PAGE_PATH : '/user/login'
      next({ path: path, query: { redirect: to.fullPath } })
      NProgress.done() // if current page is login will not trigger afterEach hook, so manually handle it
    }
  }
})

router.afterEach(() => {
  NProgress.done() // finish progress bar
})
