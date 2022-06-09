/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Administrator
 */
@Configuration
public class PersistenceConfiguration {

    @Value("${spring.jpa.properties.hibernate.dialect}")
    public String hibernateDialect;


    @Bean
    public PlatformTransactionManager getTransactionManager() {
        JpaTransactionManager jpaTm = new JpaTransactionManager();
        return jpaTm;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryPrimary(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(getClass().getPackage().getName().substring(0, getClass().getPackage().getName().lastIndexOf(".")));
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        em.setJpaProperties(properties);
        return em;
    }

}
