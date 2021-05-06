package ac.cn.saya.redislock.utils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Title: RedisUtils
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 4/5/21 11:30
 * @Description:
 */

public class RedisUtils {

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(10);
        jedisPool = new JedisPool(config);
    }

    public static JedisPool getPool() throws NullPointerException{
        if (null == jedisPool){
            throw new NullPointerException();
        }
        return jedisPool;
    }

}
