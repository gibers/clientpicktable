package com.hackpicktable.clientpicktable.impl.secondlevel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.ReservationSettings;
import com.hackpicktable.clientpicktable.entities.TimeSlot;
import com.hackpicktable.clientpicktable.repositories.ReservationSettingsRepository;
import com.hackpicktable.clientpicktable.repositories.TimeSlotRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListStartTimeSlotForThatDate {

	private final ReservationSettingsRepository reservationSettingsRepository;
	private final TimeSlotRepository timeSlotRepository;

	public @NonNull List<LocalTime> getListStartTimeSorted(Establishment establishment, LocalDate reservationDate) {
		List<TimeSlot> timeSlotsForThatDay = getTimeSlotsForThatDay(reservationDate, establishment);
		ReservationSettings reservationSettings = reservationSettingsRepository.findByEstablishment(establishment);
		List<LocalTime> listStartTime = getListStartTime(timeSlotsForThatDay, reservationSettings);
		return sortedListStartTime(listStartTime);
	}

	private List<TimeSlot> getTimeSlotsForThatDay(LocalDate ld, Establishment establishmentsFromDB) {
		DayOfWeek dayOfWeek = ld.getDayOfWeek();
		int indexOfDayFrom0To6 = (dayOfWeek.getValue() == 7)? 0 : dayOfWeek.getValue();
		return timeSlotRepository.findAllByAvailableDay(indexOfDayFrom0To6,
				establishmentsFromDB.getId());
	}

	private @NonNull List<LocalTime> getListStartTime(List<TimeSlot> timeSlotsForThatDay, ReservationSettings reservationSettings) {
		Integer bookingIntervalMinutes = reservationSettings.getBookingIntervalMinutes(); // ex: 30
		Integer reservationDurationMinutes = reservationSettings.getReservationDurationMinutes(); // ex: 120
		List<LocalTime> timeIntervals = new ArrayList<>();
		timeSlotsForThatDay.forEach(timeSlot -> {
			LocalTime startTime = timeSlot.getStartTime();
			LocalTime endTime = timeSlot.getEndTime().minusMinutes(reservationDurationMinutes);
			timeIntervals.addAll(computeTimeIntervals(startTime, endTime, bookingIntervalMinutes));
		});
		return timeIntervals.stream()
				.distinct()
				.sorted(Comparator.naturalOrder())
				.toList();
	}

	private List<LocalTime> computeTimeIntervals(LocalTime startTime, LocalTime endTime, Integer bookingIntervalMinutes) {
		LocalTime currentTime = startTime;
		List<LocalTime> timeIntervals = new ArrayList<>();
		while (currentTime.isBefore(endTime) || currentTime.equals(endTime)) {
			LocalTime intervalEndTime = currentTime.plusMinutes(bookingIntervalMinutes);
			timeIntervals.add(currentTime);
			currentTime = intervalEndTime;
		}
		return timeIntervals;
	}

	private List<LocalTime> sortedListStartTime(List<LocalTime> listStartTime) {
		return listStartTime.stream()
				.sorted(Comparator.comparingLong(time -> Math.abs(time.toNanoOfDay() - LocalTime.NOON.toNanoOfDay())))
				.toList();
	}

}
