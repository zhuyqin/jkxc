package org.jeecg.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.order.entity.GhOrderPayment;
import org.jeecg.modules.order.mapper.GhOrderPaymentMapper;
import org.jeecg.modules.order.service.IGhOrderPaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单收费记录表 服务实现类
 * </p>
 *
 * @author jeecg-boot
 * @since 2025-01-01
 */
@Service
public class GhOrderPaymentServiceImpl extends ServiceImpl<GhOrderPaymentMapper, GhOrderPayment> implements IGhOrderPaymentService {

    @Override
    public List<GhOrderPayment> getPaymentsByOrderId(String orderId) {
        return baseMapper.getPaymentsByOrderId(orderId);
    }
}

