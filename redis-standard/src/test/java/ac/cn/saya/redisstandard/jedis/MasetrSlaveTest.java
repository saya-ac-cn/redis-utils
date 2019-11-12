package ac.cn.saya.redisstandard.jedis;

import redis.clients.jedis.Jedis;

/**
 * @Title: MasetrSlaveTest
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-11-12 21:51
 * @Description: 主从复制
 */

public class MasetrSlaveTest {

    public void test1() {
        Jedis jedisMaster = new Jedis("127.0.0.1", 6379);
        Jedis jedisSlave1 = new Jedis("127.0.0.1", 6380);
        Jedis jedisSlave2 = new Jedis("127.0.0.1", 6381);
        // 配从，不配主
        jedisSlave1.slaveof("127.0.0.1", 6379);
        jedisSlave2.slaveof("127.0.0.1", 6379);
        // 主写
        jedisMaster.set("k3", "v3");
        //从读
        String result = jedisSlave1.get("k3");
        System.out.println("result:" + result);
    }

}
