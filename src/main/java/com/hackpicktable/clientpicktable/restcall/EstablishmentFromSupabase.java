package com.hackpicktable.clientpicktable.restcall;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hackpicktable.clientpicktable.config.SupabaseProperties;
import com.hackpicktable.clientpicktable.entities.Tisch;

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

	public List<Tisch> getEstablishment() {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/rest/v1/establishments")
						.queryParam("select", "*")
						.queryParam("slug", "eq.zao-cafe")
						.queryParam("is_active", "eq.true")
						.build())
				.header("apikey", supabaseProperties.apiKey())
				.retrieve()
				.body(new ParameterizedTypeReference<>() {});
	}

}
