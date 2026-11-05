package com.hackpicktable.clientpicktable.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
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

import lombok.extern.slf4j.Slf4j;

@Component
@RequestScope
@Slf4j
public class ReservationParamRequest {

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

	public ReservationParamRequest (TischRepository tischRepository, BookATableFromSupabase bookATableFromSupabase,
			ReservationResponseMapper reservationResponseMapper, ReservationResponseRepository reservationResponseRepository,
			LastPatronymicUsedRepository lastPatronymicUsedRepository) {
		this.tischRepository = tischRepository;
		this.bookATableFromSupabase = bookATableFromSupabase;
		this.reservationResponseMapper = reservationResponseMapper;
		this.reservationResponseRepository = reservationResponseRepository;
		this.lastPatronymicUsedRepository = lastPatronymicUsedRepository;
	}

	public ReservationResponseDto setParam(Establishment establishmentFromDB, Patronymic patronymicToUse, int numberOfPeople, LocalTime startTime,
			LocalDate dateReservation) {
		this.establishmentToBook = establishmentFromDB;
		this.patronymicToUse = patronymicToUse;
		this.numberOfPeople = numberOfPeople;
		this.startTime = startTime;
		this.dateReservation = dateReservation;
		return this.loopOnAllTablesUntilSuccess();
	}

	private ReservationResponseDto loopOnAllTablesUntilSuccess() {
		List<Tisch> tablesWithCapacityGte = tischRepository.findAllByCapacityGreaterThanEqualAndBookableTrue(this.numberOfPeople)
				.stream().sorted(Comparator.comparing(Tisch::getCapacity)).toList();
		ReservationResponseDto reservationResponseDto;
		Iterator<Tisch> iterator = tablesWithCapacityGte.iterator();
		do {
			Tisch tableWithCapacityGte = iterator.next();
			BookATableRequestDto.Reservation reservation = prepareReservation(tableWithCapacityGte.getId());
			BookATableRequestDto request = buildRequest(reservation);
			reservationResponseDto = bookATableFromSupabase.addAReservation(request);
			saveReservationResponseInDB(reservationResponseDto, tableWithCapacityGte);
		} while (iterator.hasNext() && !Objects.requireNonNull(reservationResponseDto).getSuccess());
		if(Objects.requireNonNull(reservationResponseDto).getSuccess()) {
			incrementPatronymicId();
		}
		return reservationResponseDto;
	}

	private void incrementPatronymicId() {
		LastPatronymicUsed.LastPatronymicUsedBuilder lastPatronymicUsedBuilder = LastPatronymicUsed.builder();
		lastPatronymicUsedBuilder.establishment(this.establishmentToBook);
		lastPatronymicUsedBuilder.lastPatronymicUsed(this.patronymicToUse);
		LastPatronymicUsed lastPatronymicUsed = lastPatronymicUsedBuilder.build();
		lastPatronymicUsedRepository.save(lastPatronymicUsed);
	}

	private void saveReservationResponseInDB(ReservationResponseDto reservationResponseDto, Tisch tableWithCapacityGte) {
		ReservationResponse reservationResponse = reservationResponseMapper.toEntity(reservationResponseDto);
		reservationResponse.setEstablishment(this.establishmentToBook);
		reservationResponse.setTisch(tableWithCapacityGte);
		reservationResponse.setPatronymic(this.patronymicToUse);
		reservationResponseRepository.save(reservationResponse);
	}

	private BookATableRequestDto.Reservation prepareReservation(UUID tableId) {
		BookATableRequestDto.Reservation.ReservationBuilder reservationBuilder = BookATableRequestDto.Reservation.builder();
		reservationBuilder.date(this.dateReservation);
		reservationBuilder.startTime(this.startTime);
		reservationBuilder.tableId(tableId);
		reservationBuilder.numberOfPeople(this.numberOfPeople);
		reservationBuilder.hasStroller(false);
		reservationBuilder.terracePreference("indoor");
		return reservationBuilder.build();
	}

	private BookATableRequestDto buildRequest(BookATableRequestDto.Reservation reservation) {
		BookATableRequestDto.BookATableRequestDtoBuilder builder = BookATableRequestDto.builder();
		builder.slug(establishmentToBook.getSlug());
		builder.establishmentId(establishmentToBook.getId());

		BookATableRequestDto.Customer.CustomerBuilder customerBuilder = BookATableRequestDto.Customer.builder();
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
