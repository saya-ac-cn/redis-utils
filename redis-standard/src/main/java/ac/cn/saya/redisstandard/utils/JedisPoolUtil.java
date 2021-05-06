package ac.cn.saya.redisstandard.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Title: JedisPoolUtil
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-11-12 22:06
 * @Description:
 */

public class JedisPoolUtil {

    private static volatile JedisPool jedisPool = null;

    // 构造函数私有化
    private JedisPoolUtil() {
    }

    public static JedisPool getJedisPoolInstance(){
        if (null == jedisPool){
            synchronized (JedisPoolUtil.class){
                if (null == jedisPool){
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxIdle(32);
                    poolConfig.setTestOnBorrow(true);
                    poolConfig.setMaxWaitMillis(100*100);
                    jedisPool = new JedisPool(poolConfig,"127.0.0.1",6379);
                }
            }
        }
        return jedisPool;
    }

    public static void releas(JedisPool jedisPool){
        if (!jedisPool.isClosed()){
            jedisPool.close();
        }
    }

}
