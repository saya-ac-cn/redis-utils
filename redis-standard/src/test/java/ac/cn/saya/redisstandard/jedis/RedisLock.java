package ac.cn.saya.redisstandard.jedis;

import ac.cn.saya.redisstandard.utils.JedisPoolUtil;
import cn.hutool.core.util.IdUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Title: RedisLock
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2/24/21 21:40
 * @Description:使用redis实现分布式锁
 * 参考：https://www.jianshu.com/p/47fd7f86c848
 */

public class RedisLock {

    // 锁键
    private final String lock_key = "redis_lock";
    // 锁过期时间
    private long internalLockLeaseTime = 30000;
    // 获取锁超时时间
    private long timeout = 999999;

    private Jedis jedis = null;

    //SET命令的参数
    SetParams params = SetParams.setParams().nx().px(internalLockLeaseTime);

    /**
     * 加锁
     * @param id
     * @return
     */
    public boolean lock(String id){
        try {
            long start = System.currentTimeMillis();
            // 自旋获取锁
            for (;;){
                // SET命令返回OK，则证明获取锁成功
                String lock = jedis.set(lock_key, id, params);
                if("OK".equals(lock)){
                    return true;
                }
                // 自旋等待获取锁，在timeout时间内仍未获取到锁，则获取失败
                long l = System.currentTimeMillis() - start;
                if (l>=timeout){
                    return false;
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            jedis.close();
        }
    }

    /**
     * 解锁
     * @param id
     * @return
     */
    public boolean unlock(String id){
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then" +
                "   return redis.call('del',KEYS[1])    " +
                "else" +
                "   return 0    " +
                "end";
        try {
            Object result = jedis.eval(script, Collections.singletonList(lock_key), Collections.singletonList(id));
            if ("1".equals(result.toString())){
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            jedis.close();
        }
    }

    @Test
    public void client() throws InterruptedException{
        JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
        jedis = jedisPool.getResource();
        int clientCount = 100;
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(clientCount);
        ExecutorService executorService = Executors.newFixedThreadPool(clientCount);
        long start = System.currentTimeMillis();
        for (int i = 0; i < clientCount; i++) {
            executorService.execute(()->{
                String id = IdUtil.randomUUID();
                try {
                    lock(id);
                    count.incrementAndGet();
                } finally {
                    //unlock(id);
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        System.out.println("执行线程数:"+clientCount+",总耗时:"+(end-start)+",count数为:"+count.get());
    }

}
