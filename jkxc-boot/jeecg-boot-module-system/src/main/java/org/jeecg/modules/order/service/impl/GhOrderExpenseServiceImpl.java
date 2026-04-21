package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.order.entity.GhOrderExpense;
import org.jeecg.modules.order.mapper.GhOrderExpenseMapper;
import org.jeecg.modules.order.service.IGhOrderExpenseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单支出记录表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
public class GhOrderExpenseServiceImpl extends ServiceImpl<GhOrderExpenseMapper, GhOrderExpense> implements IGhOrderExpenseService {

    @Override
    public List<GhOrderExpense> getExpensesByOrderId(String orderId) {
        return baseMapper.getExpensesByOrderId(orderId);
    }
}

