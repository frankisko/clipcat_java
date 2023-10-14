package com.frankisko.clipcat.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MyBatisConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisConfiguration.class);

    @Autowired
    DataSource dataSource;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {

        LOGGER.info(">>>>>>>>>>>sqlSessionFactory");
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();

        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliasesPackage("com.frankisko.clipcat.dao");

        SqlSessionFactory sessionFactoryObject = sessionFactory.getObject();

        sessionFactoryObject.getConfiguration().setJdbcTypeForNull(JdbcType.NULL);

        return sessionFactoryObject;
    }

}
