<template>
  <div class="renewal-order-analysis">
    <a-card :bordered="false">
      <!-- 查询条件 -->
      <a-form layout="inline" @keyup.enter.native="handleSearch" style="margin-bottom: 16px">
        <a-form-item label="年份">
          <a-select
            v-model="queryParam.year"
            placeholder="请选择年份"
            style="width: 150px"
            @change="handleSearch"
          >
            <a-select-option v-for="i in 11" :key="i" :value="currentYear - 5 + i - 1">
              {{ currentYear - 5 + i - 1 }}年
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="月份">
          <a-select
            v-model="queryParam.month"
            placeholder="请选择月份"
            style="width: 150px"
            @change="handleSearch"
            allowClear
          >
            <a-select-option v-for="i in 12" :key="i" :value="i">
              {{ i }}月
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="业务类型">
          <j-category-select
            v-model="queryParam.businessType"
            placeholder="请选择业务类型"
            pcode="A01"
            back="value"
            style="width: 200px"
            @change="handleSearch"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch" icon="search">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset" icon="reload">重置</a-button>
        </a-form-item>
      </a-form>

      <!-- 标签页：代账续费和地址续费 -->
      <a-tabs v-model="activeTab" @change="handleTabChange" type="card">
        <a-tab-pane key="accounting" tab="代账续费">
          <renewal-tab-content
            :query-param="queryParam"
            :renewal-type="'accounting'"
            ref="accountingTab"
          />
        </a-tab-pane>
        <a-tab-pane key="address" tab="地址续费">
          <renewal-tab-content
            :query-param="queryParam"
            :renewal-type="'address'"
            ref="addressTab"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script>
import RenewalTabContent from './modules/RenewalTabContent'

export default {
  name: 'RenewalOrderAnalysis',
  components: {
    RenewalTabContent,
  },
  data() {
    const currentYear = new Date().getFullYear()
    return {
      description: '续费订单分析',
      currentYear,
      activeTab: 'accounting',
      queryParam: {
        year: currentYear,
        month: null,
        businessType: null,
      },
    }
  },
  methods: {
    handleSearch() {
      this.loadCurrentTabData()
    },
    handleReset() {
      this.queryParam = {
        year: this.currentYear,
        month: null,
        businessType: null,
      }
      this.loadCurrentTabData()
    },
    handleTabChange(key) {
      this.activeTab = key
      this.$nextTick(() => {
        this.loadCurrentTabData()
      })
    },
    loadCurrentTabData() {
      const tabRef = this.activeTab === 'accounting' ? this.$refs.accountingTab : this.$refs.addressTab
      if (tabRef && tabRef.loadData) {
        tabRef.loadData()
      }
    },
  },
}
</script>

<style scoped lang="less">
.renewal-order-analysis {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
  overflow-x: hidden;
  box-sizing: border-box;

  .ant-card {
    margin-bottom: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    overflow: hidden;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    /deep/ .ant-card-head {
      border-bottom: 2px solid #f0f0f0;
      padding: 12px 16px;

      .ant-card-head-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    /deep/ .ant-card-body {
      padding: 16px;
    }
  }

  // 查询表单样式
  /deep/ .ant-form {
    background: #fff;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    box-sizing: border-box;

    .ant-form-item {
      margin-bottom: 12px;
    }
  }

  // 标签页样式
  /deep/ .ant-tabs {
    .ant-tabs-card {
      .ant-tabs-tab {
        border-radius: 4px 4px 0 0;
      }
    }
  }
}
</style>

