package com.hackpicktable.clientpicktable.restcall;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hackpicktable.clientpicktable.config.SupabaseProperties;
import com.hackpicktable.clientpicktable.dtos.TischDto;

@Service
public class AllTablesFromSupabase {

    private final RestClient restClient;
    private final SupabaseProperties supabaseProperties;

    public AllTablesFromSupabase(RestClient.Builder restClientBuilder, SupabaseProperties supabaseProperties) {
        this.supabaseProperties = supabaseProperties;
        this.restClient = restClientBuilder.baseUrl(supabaseProperties.url()).build();
    }

    public List<TischDto> getAllTables(UUID establishmentId) {
        String establishmentIdQuery = "eq." + establishmentId.toString();
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/tables")
                        .queryParam("select", "*")
                        .queryParam("order", "name.asc")
                        .queryParam("establishment_id", establishmentIdQuery)
                        .queryParam("is_bookable", "eq.true")
                        .build())
                .header("apikey", supabaseProperties.apiKey())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

}
