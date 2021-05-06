package ac.cn.saya.redislock.service;

import ac.cn.saya.redislock.utils.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Title: GoodsService
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 4/5/21 10:55
 * @Description:
 */
@Service
public class GoodsService {

    public static final String REDIS_LOCK = "redis_lock";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private Redisson redisson;

    @Value("${server.port}")
    private String port;

    /**
     * 不使用lua和redisson实现分布式锁
     * @return
     */
    public String buyGoodsByMethod1(){
        String uuid = UUID.randomUUID().toString() + Thread.currentThread().getName();
        String result = "商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + port;

        try {
            // 使用原子操作，超时和锁的操作放在一起，要么全部成功，要么全部失败
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, uuid, 10L, TimeUnit.SECONDS);
            if (!flag){
                return "抢锁失败";
            }

            String cacheNum = stringRedisTemplate.opsForValue().get("goods:10001");
            int goodsNum = (StringUtils.isEmpty(cacheNum))?0:Integer.parseInt(cacheNum);
            if (goodsNum > 0){
                // 从缓存中取出商品的数量，若还存在库存，则进行扣减
                int realNum = goodsNum - 1;
                stringRedisTemplate.opsForValue().set("goods:10001",String.valueOf(realNum));
                System.out.println("成功买到商品，库存还剩下: "+ realNum + " 件" + "\t服务提供端口" + port);
                result = "成功买到商品，库存还剩下:" + realNum + " 件" + "\t服务提供端口" + port;
            }
            System.out.println("商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + port);
        } finally {
            // 不使用lua脚本，实现对锁的判断和删除是原子操作
            while (true){
                // 监视一个(或多个) key ，如果在事务执行之前这个(或这些) key 被其他命令所改动，那么事务将被打断。
                stringRedisTemplate.watch(REDIS_LOCK);
                // 判断这个锁是不是自己的速
                if (stringRedisTemplate.opsForValue().get(REDIS_LOCK).equalsIgnoreCase(uuid)){
                    // 开启事务
                    stringRedisTemplate.setEnableTransactionSupport(true);
                    stringRedisTemplate.multi();
                    // 删除锁
                    stringRedisTemplate.delete(REDIS_LOCK);
                    // 提交执行
                    List<Object> list = stringRedisTemplate.exec();
                    // 判断是否执行成功
                    if (CollectionUtils.isEmpty(list)){
                        continue;
                    }
                }
                stringRedisTemplate.unwatch();
                break;
            }
        }
        return result;
    }

    /**
     * 使用lua
     * @return
     */
    public String buyGoodsByMethod2(){
        String uuid = UUID.randomUUID().toString() + Thread.currentThread().getName();
        String result = "商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + port;

        try {
            // 使用原子操作，超时和锁的操作放在一起，要么全部成功，要么全部失败
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, uuid, 10L, TimeUnit.SECONDS);
            if (!flag){
                return "抢锁失败";
            }

            String cacheNum = stringRedisTemplate.opsForValue().get("goods:10001");
            int goodsNum = (StringUtils.isEmpty(cacheNum))?0:Integer.parseInt(cacheNum);
            if (goodsNum > 0){
                // 从缓存中取出商品的数量，若还存在库存，则进行扣减
                int realNum = goodsNum - 1;
                stringRedisTemplate.opsForValue().set("goods:10001",String.valueOf(realNum));
                System.out.println("成功买到商品，库存还剩下: "+ realNum + " 件" + "\t服务提供端口" + port);
                result = "成功买到商品，库存还剩下:" + realNum + " 件" + "\t服务提供端口" + port;
            }
            System.out.println("商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + port);
        } finally {
            // 使用lua脚本，实现对锁的判断和删除是原子操作
            JedisPool pool = RedisUtils.getPool();
            Jedis jedis = pool.getResource();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] "
                    + "then "
                    + "    return redis.call('del', KEYS[1]) "
                    + "else "
                    + "    return 0 "
                    + "end";
            try {
                Object o = jedis.eval(script, Collections.singletonList(REDIS_LOCK), Collections.singletonList(uuid));
                if (null != o && "1".equals(o.toString())){
                    System.out.println("---del redis lock ok.");
                }else {
                    System.out.println("---del redis lock error.");
                }
            } finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
        return result;
    }

    /**
     * 不使用lua和redisson实现分布式锁
     * @return
     */
    public String buyGoodsByMethod3(){
        RLock lock = redisson.getLock(REDIS_LOCK);
        lock.lock();
        String result = "商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + port;
        try {

            String cacheNum = stringRedisTemplate.opsForValue().get("goods:10001");
            int goodsNum = (StringUtils.isEmpty(cacheNum))?0:Integer.parseInt(cacheNum);
            if (goodsNum > 0){
                // 从缓存中取出商品的数量，若还存在库存，则进行扣减
                int realNum = goodsNum - 1;
                stringRedisTemplate.opsForValue().set("goods:10001",String.valueOf(realNum));
                System.out.println("成功买到商品，库存还剩下: "+ realNum + " 件" + "\t服务提供端口" + port);
                result = "成功买到商品，库存还剩下:" + realNum + " 件" + "\t服务提供端口" + port;
            }
            System.out.println("商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + port);
        } finally {
            // 删除锁之前做一次判断，处于锁定中，并且是当前的线程持有
            if (lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        return result;
    }

}
