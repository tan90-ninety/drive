package com.atguigu.daijia.order.handle;

import com.atguigu.daijia.order.service.OrderInfoService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class RedisDelayHandle {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderInfoService orderInfoService;

    @PostConstruct
    public void listener(){
        new Thread(() -> {
            while (true) {
                RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue("queue_cancel");
                try {
                    String orderId = blockingQueue.take();
                    if (StringUtils.isEmpty(orderId)) {
                        orderInfoService.orderCancel(Long.parseLong(orderId));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}