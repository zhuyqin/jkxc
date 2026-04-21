package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.order.entity.GhOrderStep;
import org.jeecg.modules.order.mapper.GhOrderStepMapper;
import org.jeecg.modules.order.service.IGhOrderStepService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单流程步骤表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
public class GhOrderStepServiceImpl extends ServiceImpl<GhOrderStepMapper, GhOrderStep> implements IGhOrderStepService {

    @Override
    public void deleteByOrderId(String orderId) {
        LambdaQueryWrapper<GhOrderStep> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GhOrderStep::getOrderId, orderId);
        this.remove(queryWrapper);
    }

    @Override
    public List<GhOrderStep> getStepsByOrderId(String orderId) {
        return baseMapper.getStepsByOrderId(orderId);
    }
}

