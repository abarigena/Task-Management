package com.abarigena.taskservice.client;

import com.abarigena.taskservice.config.FeignServiceAuthConfig;
import com.abarigena.taskservice.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = FeignServiceAuthConfig.class)
public interface UserServiceClient {
    @GetMapping("/users/info/{userId}")
    UserInfoDto getUserInfo(@PathVariable("userId") String userId);
}