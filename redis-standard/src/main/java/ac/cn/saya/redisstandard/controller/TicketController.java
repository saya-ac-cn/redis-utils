package ac.cn.saya.redisstandard.controller;

import ac.cn.saya.redisstandard.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Title: TicketController
 * @ProjectName redis-utils
 * @Description: TODO
 * @Author liunengkai
 * @Date: 2019-09-28 17:30
 * @Description:
 */
@RestController
@RequestMapping(value = "/ticket")
public class TicketController {

    @Autowired
    private RedisUtils redisUtils;

    @GetMapping("/sell")
    public String sellTicket(@RequestParam(value = "name") String name){
        redisUtils.convertAndSend("title",name);
        return name + ",您已经订票成功";
    }

}
