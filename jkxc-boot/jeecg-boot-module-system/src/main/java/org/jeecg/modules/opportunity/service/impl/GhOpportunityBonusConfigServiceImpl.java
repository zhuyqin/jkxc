package org.jeecg.modules.opportunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.opportunity.entity.GhOpportunityBonusConfig;
import org.jeecg.modules.opportunity.mapper.GhOpportunityBonusConfigMapper;
import org.jeecg.modules.opportunity.service.IGhOpportunityBonusConfigService;
import org.springframework.stereotype.Service;

/**
 * @Description: 线索奖金配置Service实现类
 */
@Service
public class GhOpportunityBonusConfigServiceImpl extends ServiceImpl<GhOpportunityBonusConfigMapper, GhOpportunityBonusConfig>
        implements IGhOpportunityBonusConfigService {
}

