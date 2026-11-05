package com.hackpicktable.clientpicktable.restcall;

import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hackpicktable.clientpicktable.config.SupabaseProperties;
import com.hackpicktable.clientpicktable.dtos.ReservationSettingsDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReservationSettingsFromSupabase {

	private final RestClient restClient;
	private final SupabaseProperties supabaseProperties;

	public ReservationSettingsFromSupabase(RestClient.Builder restClientBuilder, SupabaseProperties supabaseProperties) {
		this.supabaseProperties = supabaseProperties;
		this.restClient = restClientBuilder.baseUrl(supabaseProperties.url()).build();
	}

	public ReservationSettingsDto getReservationSettings(UUID establishmentId) {
		List<ReservationSettingsDto> reservationSettingsDtos = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/rest/v1/reservation_settings")
						.queryParam("select", "*")
						.queryParam("establishment_id", "eq." + establishmentId.toString())
						.build())
				.header("apikey", supabaseProperties.apiKey())
				.retrieve()
				.body(new ParameterizedTypeReference<>() {});

		return reservationSettingsDtos == null ? null : reservationSettingsDtos.stream().findFirst().orElse(null);
	}

}
