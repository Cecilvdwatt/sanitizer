package com.flash.sanitization.db;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;

import java.util.Objects;

public class SqlTestContainer {

    static final MSSQLServerContainer<?> sqlServer;

    static {
        sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-CU17-ubuntu-20.04")
            .acceptLicense();
        sqlServer.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlServer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlServer::getUsername);
        registry.add("spring.datasource.password", sqlServer::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDb() {
        if(!Objects.isNull(jdbcTemplate)) {
            jdbcTemplate.execute("EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT all'");
            jdbcTemplate.execute("EXEC sp_MSforeachtable 'DELETE FROM ?'");
            jdbcTemplate.execute("EXEC sp_MSforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all'");
        }
    }
}
