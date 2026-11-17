package com.hackpicktable.clientpicktable.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.BookATableForSupabaseRequestDto;
import com.hackpicktable.clientpicktable.dtos.ReservationResponseDto;
import com.hackpicktable.clientpicktable.dtos.mappers.ReservationResponseMapper;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.LastPatronymicUsed;
import com.hackpicktable.clientpicktable.entities.Patronymic;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.repositories.LastPatronymicUsedRepository;
import com.hackpicktable.clientpicktable.repositories.ReservationResponseRepository;
import com.hackpicktable.clientpicktable.repositories.TischRepository;
import com.hackpicktable.clientpicktable.restcall.BookATableFromSupabase;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ReservationParamRequest {

	public static final List<String> BLOCKING_MESSAGES_TIME = List.of(
			"Cet horaire n'est plus disponible en ligne", "Créneau horaire invalide ou inactif", "Cet horaire a atteint sa limite de 6 personnes");
	public static final List<String> BLOCKING_MESSAGES_TOOMANYRESERVATION = List.of(
			"Trop de réservations ont été faites depuis cet appareil. Merci de contacter directement le restaurant.");

	private final TischRepository tischRepository;
	private final BookATableFromSupabase bookATableFromSupabase;
	private final ReservationResponseMapper reservationResponseMapper;
	private final ReservationResponseRepository reservationResponseRepository;
	private final LastPatronymicUsedRepository lastPatronymicUsedRepository;

	private Establishment establishmentToBook;
	private Patronymic patronymicToUse;

	private LocalDate dateReservation;
	private LocalTime startTime;
	private int numberOfPeople;
	private List<Tisch> tablesListForThatCapacity;

	public ReservationParamRequest (TischRepository tischRepository, BookATableFromSupabase bookATableFromSupabase,
			ReservationResponseMapper reservationResponseMapper, ReservationResponseRepository reservationResponseRepository,
			LastPatronymicUsedRepository lastPatronymicUsedRepository) {
		this.tischRepository = tischRepository;
		this.bookATableFromSupabase = bookATableFromSupabase;
		this.reservationResponseMapper = reservationResponseMapper;
		this.reservationResponseRepository = reservationResponseRepository;
		this.lastPatronymicUsedRepository = lastPatronymicUsedRepository;
	}

	@Transactional
	public ReservationResponseDto setParamAndLaunch(Establishment establishmentFromDB, Patronymic patronymicToUse, int numberOfPeople, LocalTime startTime,
			LocalDate dateReservation, List<Tisch> tablesListForThatCapacity) {
		this.establishmentToBook = establishmentFromDB;
		this.patronymicToUse = patronymicToUse;
		this.numberOfPeople = numberOfPeople;
		this.startTime = startTime;
		this.dateReservation = dateReservation;
		this.tablesListForThatCapacity = tablesListForThatCapacity;
		return this.loopOnAllTablesUntilSuccess();
	}

	private ReservationResponseDto loopOnAllTablesUntilSuccess() {
		ReservationResponseDto reservationResponseDto;
		Iterator<Tisch> iterator = tablesListForThatCapacity.iterator();
		do {
			Tisch tableWithCapacityGte = iterator.next();
			BookATableForSupabaseRequestDto.Reservation reservation = prepareReservation(tableWithCapacityGte.getId());
			BookATableForSupabaseRequestDto request = buildRequest(reservation);
			reservationResponseDto = bookATableFromSupabase.addAReservation(request);
			saveReservationResponseInDB(reservationResponseDto, tableWithCapacityGte);
		} while (iterator.hasNext() && !Objects.requireNonNull(reservationResponseDto).getSuccess() && !blockingMessage(reservationResponseDto.getErrorMessage()));
		if(Objects.requireNonNull(reservationResponseDto).getSuccess()) {
			incrementPatronymicId();
		}
		return reservationResponseDto;
	}

	// return true if message is blocking. With this message, we don't need to retry with another table
	private boolean blockingMessage(String errorMessage) {
		return BLOCKING_MESSAGES_TIME.contains(errorMessage) || BLOCKING_MESSAGES_TOOMANYRESERVATION.contains(errorMessage);
	}

	private void incrementPatronymicId() {
		Optional<LastPatronymicUsed> lastPatronymicUsed = this.lastPatronymicUsedRepository.findById(this.establishmentToBook.getId());
		// update table lastPatronymicUsed if present, insert if not.
		lastPatronymicUsed.ifPresentOrElse(x -> x.setLastPatronymicUsed(this.patronymicToUse), () -> {
			LastPatronymicUsed.LastPatronymicUsedBuilder lastPatronymicUsedBuilder = LastPatronymicUsed.builder();
			lastPatronymicUsedBuilder.establishment(this.establishmentToBook);
			lastPatronymicUsedBuilder.lastPatronymicUsed(this.patronymicToUse);
			lastPatronymicUsedRepository.save(lastPatronymicUsedBuilder.build());
		});
	}

	private void saveReservationResponseInDB(ReservationResponseDto reservationResponseDto, Tisch tableWithCapacityGte) {
		ReservationResponse reservationResponse = reservationResponseMapper.toEntity(reservationResponseDto);
		reservationResponse.setEstablishment(this.establishmentToBook);
		reservationResponse.setTisch(tableWithCapacityGte);
		reservationResponse.setPatronymic(this.patronymicToUse);
		reservationResponseRepository.save(reservationResponse);
	}

	private BookATableForSupabaseRequestDto.Reservation prepareReservation(UUID tableId) {
		BookATableForSupabaseRequestDto.Reservation.ReservationBuilder reservationBuilder = BookATableForSupabaseRequestDto.Reservation.builder();
		reservationBuilder.date(this.dateReservation);
		reservationBuilder.startTime(this.startTime);
		reservationBuilder.tableId(tableId);
		reservationBuilder.numberOfPeople(this.numberOfPeople);
		reservationBuilder.hasStroller(false);
		reservationBuilder.terracePreference("indoor");

		BookATableForSupabaseRequestDto.CustomAnswers.CustomAnswersBuilder customAnswersBuilder = BookATableForSupabaseRequestDto.CustomAnswers.builder();
		if (this.establishmentToBook.getSlug().equals("le-pont-de-brent")) {
			customAnswersBuilder.question("Quel menu souhaitez-vous ?");
			customAnswersBuilder.answers(List.of("Le menu de midi"));
		} else if (this.establishmentToBook.getSlug().equals("chez-seb")) {
			customAnswersBuilder.question("Avez-vous une allergie ou un régime particulier ?");
			customAnswersBuilder.answers(List.of("Non"));
		}
		reservationBuilder.customAnswers(customAnswersBuilder.build());

		return reservationBuilder.build();
	}

	private BookATableForSupabaseRequestDto buildRequest(BookATableForSupabaseRequestDto.Reservation reservation) {
		BookATableForSupabaseRequestDto.BookATableForSupabaseRequestDtoBuilder builder = BookATableForSupabaseRequestDto.builder();
		builder.slug(establishmentToBook.getSlug());
		builder.establishmentId(establishmentToBook.getId());

		BookATableForSupabaseRequestDto.Customer.CustomerBuilder customerBuilder = BookATableForSupabaseRequestDto.Customer.builder();
		customerBuilder.email(patronymicToUse.getEmail());
		customerBuilder.firstName(patronymicToUse.getFirstname());
		customerBuilder.lastName(patronymicToUse.getName());
		customerBuilder.phone(generateRandomPhoneNumber());

		builder.customer(customerBuilder.build());
		builder.reservation(reservation);
		return builder.build();
	}

	private String generateRandomPhoneNumber() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return String.format(Locale.ROOT, "+41 76 %03d %02d %02d",
				random.nextInt(1000), random.nextInt(100), random.nextInt(100));
	}
}
