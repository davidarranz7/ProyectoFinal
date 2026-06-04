package com.david.mailrelay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.david.mailrelay",
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
public class LocalMailRelayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalMailRelayApplication.class, args);
    }
}
