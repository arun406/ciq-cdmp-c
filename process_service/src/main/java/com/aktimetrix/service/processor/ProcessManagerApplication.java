package com.aktimetrix.service.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.aktimetrix.service.processor", "com.aktimetrix.core"})
@SpringBootApplication
public class ProcessManagerApplication {
    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(ProcessManagerApplication.class, args);
    }
}
