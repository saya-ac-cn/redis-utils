package ac.cn.saya.redisstandard.jedis;

import ac.cn.saya.redisstandard.utils.JedisPoolUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Title: PoolTest
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-11-12 22:15
 * @Description:
 */

public class PoolTest {

    @Test
    public void testPool(){
        JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set("k5","v5");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
            JedisPoolUtil.releas(jedisPool);
        }
    }

}
