package ru.job4j.cars.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionFactoryConfig {
    @Bean(destroyMethod = "close")
    public SessionFactory createSessionFactory() {
        return new MetadataSources(new StandardServiceRegistryBuilder().configure().build())
                .buildMetadata().buildSessionFactory();
    }
}
