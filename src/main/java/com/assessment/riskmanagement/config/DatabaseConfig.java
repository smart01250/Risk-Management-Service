package com.assessment.riskmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.assessment.riskmanagement.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {

}
