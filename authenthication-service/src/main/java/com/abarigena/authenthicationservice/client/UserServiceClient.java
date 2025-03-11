package com.abarigena.authenthicationservice.client;

import com.abarigena.authenthicationservice.config.ServiceAuthConfig;
import com.abarigena.dto.AuthRequest;
import com.abarigena.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service", configuration = ServiceAuthConfig.class)
public interface UserServiceClient {
    @PostMapping("/users")
    UserDto registerUser(AuthRequest request);

    @GetMapping("/users/{email}")
    UserDto findByEmail(@PathVariable("email") String email);
}