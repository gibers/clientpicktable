package com.hackpicktable.clientpicktable.restcall;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hackpicktable.clientpicktable.config.SupabaseProperties;
import com.hackpicktable.clientpicktable.dtos.EstablishmentDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EstablishmentFromSupabase {

	private final RestClient restClient;
	private final SupabaseProperties supabaseProperties;

	public EstablishmentFromSupabase(RestClient.Builder restClientBuilder, SupabaseProperties supabaseProperties) {
		this.supabaseProperties = supabaseProperties;
		this.restClient = restClientBuilder.baseUrl(supabaseProperties.url()).build();
	}

	public EstablishmentDTO getEstablishment(String establishmentName) {
		List<EstablishmentDTO> establishments = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/rest/v1/establishments")
						.queryParam("select", "*")
						.queryParam("slug", "eq." + establishmentName)
						.queryParam("is_active", "eq.true")
						.queryParam("limit", 1)
						.build())
				.header("apikey", supabaseProperties.apiKey())
				.retrieve()
				.body(new ParameterizedTypeReference<List<EstablishmentDTO>>() {});

		return establishments == null ? null : establishments.stream().findFirst().orElse(null);
	}

}
