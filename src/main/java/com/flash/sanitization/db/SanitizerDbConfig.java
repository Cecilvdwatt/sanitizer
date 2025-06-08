package com.flash.sanitization.db;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.flash.sanitization.db.repository"
)
@EntityScan(
    basePackages = {"com.flash.sanitization.db.entity"}
)
public class SanitizerDbConfig {
}
