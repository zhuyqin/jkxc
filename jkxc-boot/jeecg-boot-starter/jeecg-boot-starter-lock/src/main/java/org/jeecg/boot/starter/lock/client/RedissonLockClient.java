package org.jeecg.boot.starter.lock.client;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁实现基于Redisson
 *
 * @author zyf
 * @date 2020-11-11
 */
@Slf4j
@Component
public class RedissonLockClient {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取锁
     */
    public RLock getLock(String lockKey) {
        if (redissonClient == null) {
            log.error("RedissonClient未初始化，无法获取锁，lockKey={}", lockKey);
            return null;
        }
        return redissonClient.getLock(lockKey);
    }

    /**
     * 加锁操作
     *
     * @return boolean
     */
    public boolean tryLock(String lockName, long expireSeconds) {
        return tryLock(lockName, 0, expireSeconds);
    }


    /**
     * 加锁操作
     *
     * @return boolean
     */
    public boolean tryLock(String lockName, long waitTime, long expireSeconds) {
        RLock rLock = getLock(lockName);
        if (rLock == null) {
            log.error("获取锁对象失败，lockName={}", lockName);
            return false;
        }
        boolean getLock = false;
        try {
            getLock = rLock.tryLock(waitTime, expireSeconds, TimeUnit.SECONDS);
            if (getLock) {
                log.info("获取锁成功,lockName={}", lockName);
            } else {
                log.info("获取锁失败,lockName={}", lockName);
            }
        } catch (InterruptedException e) {
            log.error("获取式锁异常，lockName=" + lockName, e);
            Thread.currentThread().interrupt();
            getLock = false;
        }
        return getLock;
    }


    public boolean fairLock(String lockKey, TimeUnit unit, int leaseTime) {
        if (redissonClient == null) {
            log.error("RedissonClient未初始化，无法获取公平锁，lockKey={}", lockKey);
            return false;
        }
        if (redisTemplate == null) {
            log.error("RedisTemplate未初始化，无法检查锁是否存在，lockKey={}", lockKey);
            return false;
        }
        try {
        RLock fairLock = redissonClient.getFairLock(lockKey);
            if (fairLock == null) {
                log.error("获取公平锁对象失败，lockKey={}", lockKey);
                return false;
            }
            boolean existKey = existKey(lockKey);
            // 已经存在了，就直接返回
            if (existKey) {
                log.warn("锁已存在，lockKey={}", lockKey);
                return false;
            }
            return fairLock.tryLock(3, leaseTime, unit);
        } catch (InterruptedException e) {
            log.error("获取公平锁被中断，lockKey=" + lockKey, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("获取公平锁异常，lockKey=" + lockKey, e);
        }
        return false;
    }

    public boolean existKey(String key) {
        if (redisTemplate == null) {
            log.error("RedisTemplate未初始化，无法检查key是否存在，key={}", key);
            return false;
        }
        try {
        return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查key是否存在异常，key=" + key, e);
            return false;
        }
    }
    /**
     * 锁lockKey
     *
     * @param lockKey
     * @return
     */
    public RLock lock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock != null) {
        lock.lock();
        }
        return lock;
    }

    /**
     * 锁lockKey
     *
     * @param lockKey
     * @param leaseTime
     * @return
     */
    public RLock lock(String lockKey, long leaseTime) {
        RLock lock = getLock(lockKey);
        if (lock != null) {
        lock.lock(leaseTime, TimeUnit.SECONDS);
        }
        return lock;
    }


    /**
     * 解锁
     *
     * @param lockName 锁名称
     */
    public void unlock(String lockName) {
        if (redissonClient == null) {
            log.error("RedissonClient未初始化，无法解锁，lockName={}", lockName);
            return;
        }
        try {
            RLock lock = redissonClient.getLock(lockName);
            if (lock != null) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("解锁异常，lockName=" + lockName, e);
        }
    }


}
