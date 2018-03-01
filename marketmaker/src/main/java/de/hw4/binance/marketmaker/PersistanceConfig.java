package de.hw4.binance.marketmaker;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"de.hw4.binance.marketmaker.persistence"})
@EnableJpaRepositories(basePackages = {"de.hw4.binance.marketmaker.persistence"})
@EnableTransactionManagement
public class PersistanceConfig {
}
