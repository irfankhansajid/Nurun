package com.nurun.security;

import com.nurun.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class AuthRaceConditionTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String url = "http://localhost:8080/api/auth/register";


    @Test
    public void raceTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable task = () -> {
            restTemplate.postForObject(url, new RegisterRequestDto("race@test.com", "123456"), String.class);
        };

        for (int i = 0; i < 10; i++) {
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }


}
