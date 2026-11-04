package com.hackpicktable.clientpicktable.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(String url, String apiKey) {
}
