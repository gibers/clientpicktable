package com.hackpicktable.clientpicktable.restcall;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hackpicktable.clientpicktable.config.SupabaseProperties;
import com.hackpicktable.clientpicktable.dtos.TimeSlotDto;

@Service
public class AllTimeSlotFromSupabase {

    private final RestClient restClient;
    private final SupabaseProperties supabaseProperties;

    public AllTimeSlotFromSupabase(RestClient.Builder restClientBuilder, SupabaseProperties supabaseProperties) {
        this.supabaseProperties = supabaseProperties;
        this.restClient = restClientBuilder.baseUrl(supabaseProperties.url()).build();
    }

    public List<TimeSlotDto> getAllTimeSlot() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/time_slots")
                        .queryParam("select", "*")
                        .queryParam("is_active", "eq.true")
                        .queryParam("order", "start_time.asc")
                        .build())
                .header("apikey", supabaseProperties.apiKey())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

}
