package org.jeecg.boot.starter.lock.config;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.boot.starter.lock.core.RedissonManager;
import org.jeecg.boot.starter.lock.prop.RedissonProperties;
import org.jeecg.boot.starter.lock.enums.RedisConnectionType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


/**
 * Redisson自动化配置
 *
 * @author zyf
 * @date 2020-11-11
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedissonProperties.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfiguration {

	@Autowired(required = false)
	private RedisProperties redisProperties;

	@Bean
	@ConditionalOnMissingBean(RedissonClient.class)
	public RedissonClient redissonClient(RedissonProperties redissonProperties) {
		// 如果jeecg.redisson.address未配置，则从spring.redis读取配置
		if (!StringUtils.hasText(redissonProperties.getAddress()) && redisProperties != null) {
			String host = redisProperties.getHost();
			int port = redisProperties.getPort();
			redissonProperties.setAddress(host + ":" + port);
			
			if (!StringUtils.hasText(redissonProperties.getPassword())) {
				redissonProperties.setPassword(redisProperties.getPassword());
			}
			
			if (redissonProperties.getDatabase() == 0 && redisProperties.getDatabase() != 0) {
				redissonProperties.setDatabase(redisProperties.getDatabase());
			}
			
			// 如果type未配置，默认使用STANDALONE
			if (redissonProperties.getType() == null) {
				redissonProperties.setType(RedisConnectionType.STANDALONE);
			}
		}
		
		RedissonManager redissonManager = new RedissonManager(redissonProperties);
		log.info("RedissonManager初始化完成,当前连接方式:" + redissonProperties.getType() + ",连接地址:" + redissonProperties.getAddress());
		return redissonManager.getRedisson();
	}

}
