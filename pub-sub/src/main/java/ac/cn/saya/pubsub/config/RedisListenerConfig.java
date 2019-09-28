package ac.cn.saya.pubsub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * redis消息监听器配置
 * @Title: RedisListenerConfig
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-09-28 17:13
 * @Description:
 */
@Configuration
public class RedisListenerConfig {

    /**
     * redis连接工厂
     */
    @Autowired
    private RedisConnectionFactory connectionFactory;

    /**
     * redis消息监听器
     */
    @Autowired
    private MessageListener redisMsgListener;

    /**
     * 线程池
     */
    private ThreadPoolTaskScheduler taskScheduler = null;

    /**
     * @描述 创建线程池，运行线程等待处理Redis消息
     * @参数
     * @返回值
     * @创建人  saya.ac.cn-刘能凯
     * @创建时间  2019-09-28
     * @修改人和其它信息
     */
    @Bean
    public ThreadPoolTaskScheduler initTaskScheduler(){
        if (null != taskScheduler){
            return taskScheduler;
        }
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(20);
        return taskScheduler;
    }

    /**
     * @描述 定义redis监听器
     * @参数
     * @返回值
     * @创建人  saya.ac.cn-刘能凯
     * @创建时间  2019-09-28
     * @修改人和其它信息
     */
    @Bean
    public RedisMessageListenerContainer initRedisContainer(){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置redis连接工厂
        container.setConnectionFactory(connectionFactory);
        // 设置连接池
        container.setTaskExecutor(initTaskScheduler());
        // 定义监听渠道，名称为title
        Topic topic = new ChannelTopic("title");
        // 使用监听器监听redis消息
        container.addMessageListener(redisMsgListener,topic);
        return container;
    }


}
