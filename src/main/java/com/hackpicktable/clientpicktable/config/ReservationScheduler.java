package com.hackpicktable.clientpicktable.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
import com.hackpicktable.clientpicktable.impl.ReservationAddImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private static final long INTERVAL_MILLIS = 8 * 60 * 60 * 1000L + 12 * 60 * 1000L;
    private static final long INITIAL_DELAY_MILLIS = 2 * 60 * 60 * 1000L;

    private final ReservationAddImpl reservationAdd;

    @Scheduled(fixedDelay = INTERVAL_MILLIS, initialDelay = INITIAL_DELAY_MILLIS)
    public void bookReservation() {
        LocalTime startTime1 = LocalTime.of(11, 0);
        LocalTime startTime2 = LocalTime.of(11, 30);
        LocalTime startTime3 = LocalTime.of(12, 0);
        LocalTime startTime4 = LocalTime.of(12, 30);
        LocalTime selectedStartTime = getRandomStartTime(startTime1, startTime2, startTime3, startTime4);

        String establishmentSlug = "zao-cafe";
        BookATableRequestDto request = BookATableRequestDto.builder()
                .numberOfPeople(getRandomNumberOfPeople())
                .startTime(selectedStartTime)
                .dateReservation(getRandomReservationDate())
                .build();

        log.info("Running scheduled reservation for establishment {}", establishmentSlug);
        reservationAdd.bookAReservation(establishmentSlug, request);
    }

    private LocalTime getRandomStartTime(LocalTime... startTimes) {
        return startTimes[ThreadLocalRandom.current().nextInt(startTimes.length)];
    }

    private int getRandomNumberOfPeople() {
        return ThreadLocalRandom.current().nextInt(2, 5);
    }

    private LocalDate getRandomReservationDate() {
        int daysFromToday = ThreadLocalRandom.current().nextInt(1, 4);
        return LocalDate.now().plusDays(daysFromToday);
    }
}
