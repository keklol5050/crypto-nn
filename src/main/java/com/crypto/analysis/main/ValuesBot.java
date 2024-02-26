package com.crypto.analysis.main;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;



    @Configuration
    @Data
    @PropertySource("application.properties")
    public class ValuesBot {
        @Value("${Botname}")
        String BotName;
        @Value("${Botkey}")
        String BotKey;
    }

