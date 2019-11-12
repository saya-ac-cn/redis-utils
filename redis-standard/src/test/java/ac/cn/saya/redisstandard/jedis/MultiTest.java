package ac.cn.saya.redisstandard.jedis;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.concurrent.TimeUnit;

/**
 * @Title: MultiTest
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-11-12 21:19
 * @Description:
 * 事务
 */

public class MultiTest {

    @Test
    public void testTranscation1() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        Transaction transaction = jedis.multi();
        transaction.set("k3", "v3");
        transaction.set("k4", "v4");
        transaction.discard();
    }

    // 信用卡事务测试
    @Test
    public void testTranscation2() throws Exception{
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        // 可用额度，欠额
        int balance, dept;
        // 本次刷卡金额
        int charge = 10;
        jedis.watch("balance");
        ///jedis.set("balance","5");/// 模拟被其它人更改
        balance = Integer.parseInt(jedis.get("balance"));
        System.out.println("-----");
        TimeUnit.SECONDS.sleep(10);
        if (balance < 10) {
            jedis.unwatch();
            System.out.println("额度不足");
        } else {
            System.out.println("**********transcation***************");
            Transaction transaction = jedis.multi();
            transaction.decrBy("balance", charge);
            transaction.incrBy("dept", charge);
            transaction.exec();
            balance = Integer.parseInt(jedis.get("balance"));
            dept = Integer.parseInt(jedis.get("dept"));
            System.out.println("本次消费：" + charge);
            System.out.println("剩余额度：" + balance);
            System.out.println("欠额：" + dept);
        }
    }

}
