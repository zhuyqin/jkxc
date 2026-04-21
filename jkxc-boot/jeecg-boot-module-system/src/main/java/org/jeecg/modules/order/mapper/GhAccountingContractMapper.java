package org.jeecg.modules.order.mapper;

import org.jeecg.modules.order.entity.GhAccountingContract;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 合同管理表
 * @Author: jeecg-boot
 * @Date: 2025-01-03
 * @Version: V1.0
 */
public interface GhAccountingContractMapper extends BaseMapper<GhAccountingContract> {

    /**
     * 优化后的分页查询：一次性关联查询客户信息和订单统计
     * @param page 分页对象
     * @param ew 查询条件
     * @return 分页结果
     */
    IPage<GhAccountingContract> selectPageWithCustomerInfo(Page<GhAccountingContract> page, @Param("ew") Wrapper<GhAccountingContract> ew);

}

