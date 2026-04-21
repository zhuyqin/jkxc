package org.jeecg.modules.salary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.salary.entity.GhSalaryPayment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 工资发放记录
 * @Author: jeecg-boot
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Mapper
public interface GhSalaryPaymentMapper extends BaseMapper<GhSalaryPayment> {

}

