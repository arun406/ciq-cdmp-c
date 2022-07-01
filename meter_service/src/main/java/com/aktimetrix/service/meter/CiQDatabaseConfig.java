package com.aktimetrix.service.meter;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"com.aktimetrix.service.meter.ciq.encore.repository"})
public class CiQDatabaseConfig {
}
