package com.aktimetrix.service.planner;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"com.aktimetrix.service.planner.repository"})
public class CiQDatabaseConfig {
}
