package com.aktimetrix.service.processor;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {})
public class CiQDatabaseConfig {
}
