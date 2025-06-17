package de.bertelsmann.eudr;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:login.properties")
public class LoginProperties {

    private String username;
    private String password;

}
