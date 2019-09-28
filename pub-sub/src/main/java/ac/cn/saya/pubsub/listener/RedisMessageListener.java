package ac.cn.saya.pubsub.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * redis消息监听器
 * @Title: RedisMessageListener
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-09-28 17:05
 * @Description:
 */
@Component("redisMessageListener")
public class RedisMessageListener implements MessageListener {

    /**
     * 得到消息后的处理方法
     * @描述
     * @参数  message redis发过来的消息
     * @参数  bytes 取到名称
     * @返回值
     * @创建人  saya.ac.cn-刘能凯
     * @创建时间  2019-09-28
     * @修改人和其它信息
     */
    @Override
    public void onMessage(Message message, byte[] bytes) {
        // 消息体
        String body = new String(message.getBody());
        // 渠道名称
        String topic = new String(bytes);
        System.out.println("body:"+body);
        System.out.println("topic:"+topic);
    }
}
