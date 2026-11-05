package com.hackpicktable.clientpicktable.restcall;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.hackpicktable.clientpicktable.config.SupabaseProperties;
import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
import com.hackpicktable.clientpicktable.dtos.ReservationResponseDto;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookATableFromSupabase {

	private final RestClient restClient;
	private final SupabaseProperties supabaseProperties;

	public BookATableFromSupabase(RestClient.Builder restClientBuilder, SupabaseProperties supabaseProperties) {
		this.supabaseProperties = supabaseProperties;
		this.restClient = restClientBuilder.baseUrl(supabaseProperties.url()).build();
	}

	public ReservationResponseDto addAReservation(BookATableRequestDto request) {
		ResponseEntity<ReservationResponseDto> response = restClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("/functions/v1/create-reservation")
						.build())
				.header("apikey", supabaseProperties.apiKey())
				.body(request)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (httpRequest, clientResponse) -> {})
				.toEntity(ReservationResponseDto.class);

		ReservationResponseDto responseDto = response.getBody();
		if (responseDto == null) {
			responseDto = new ReservationResponseDto();
		}
		responseDto.setHttpStatus(response.getStatusCode().value());
		if (response.getStatusCode().isError()) {
			responseDto.setSuccess(false);
			if (request.getReservation() != null) {
				BookATableRequestDto.Reservation requestReservation = request.getReservation();
				responseDto.setReservation(ReservationResponseDto.Reservation.builder()
						.date(requestReservation.getDate())
						.startTime(requestReservation.getStartTime())
						.numberOfPeople(requestReservation.getNumberOfPeople())
						.build());
			}
		}
		return responseDto;
	}
}
