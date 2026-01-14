package org.ostech.gtdcardsbackend.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(
        DataSource dataSource,
        @Value("${spring.liquibase.change-log}") String changeLog,
        @Value("${spring.liquibase.enabled:true}") boolean enabled,
        @Value("${spring.liquibase.default-schema}") String defaultSchema,
        @Value("${spring.liquibase.liquibase-schema:#{null}}") String liquibaseSchema,
        @Value("${spring.liquibase.drop-first:false}") boolean dropFirst,
        @Value("${spring.liquibase.contexts:}") String contexts) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setDefaultSchema(defaultSchema);

        if (liquibaseSchema != null && !liquibaseSchema.isEmpty()) {
            liquibase.setLiquibaseSchema(liquibaseSchema);
        }

        liquibase.setDropFirst(dropFirst);
        liquibase.setShouldRun(enabled);

        if (contexts != null && !contexts.isEmpty()) {
            liquibase.setContexts(contexts);
        }

        return liquibase;
    }
}
