package com.example.botfightwebserver;

import com.example.botfightwebserver.searchEngine.SearchIndexBuild;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;


@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
@RestController
@EnableScheduling
public class BotFightWebServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotFightWebServerApplication.class, args);
    }

    @Bean
    public ApplicationRunner buildIndex(SearchIndexBuild searchIndexBuild) {
        return args -> {searchIndexBuild.indexPersistedData();};
    }
}
