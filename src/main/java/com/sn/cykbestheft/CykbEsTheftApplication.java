package com.sn.cykbestheft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CykbEsTheftApplication {

    public static void main(String[] args) {
        SpringApplication.run(CykbEsTheftApplication.class, args);
    }

}
