package com.ruoyi.kuzi;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import com.ruoyi.common.swagger.annotation.EnableCustomSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
@SpringCloudApplication
public class KuZiApplication {
    public static void main(String[] args) {
        SpringApplication.run(KuZiApplication.class,args);
        System.out.println("--------------裤子服务启动成功---------------");
    }
}
