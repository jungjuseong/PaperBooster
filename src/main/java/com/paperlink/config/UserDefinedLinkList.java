package com.paperlink.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "userdict")
public class UserDefinedLinkList {
    private List<String> words = new ArrayList<String>();
    private List<String> medias = new ArrayList<String>();
    
    public List<String> getWords() {
        return this.words;
    }
    public List<String> getMedias() {
        return this.words;
    }
}
