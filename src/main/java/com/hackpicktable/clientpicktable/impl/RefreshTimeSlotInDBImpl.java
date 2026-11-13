package com.hackpicktable.clientpicktable.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.TimeSlotDto;
import com.hackpicktable.clientpicktable.dtos.mappers.TimeSlotMapper;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.TimeSlot;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.TimeSlotRepository;
import com.hackpicktable.clientpicktable.restcall.AllTimeSlotFromSupabase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTimeSlotInDBImpl {

	private final AllTimeSlotFromSupabase allTimeSlotFromSupabase;
	private final EstablishmentRepository establishmentRepository;
	private final RefreshEstablishmentInDBImpl refreshEstablishmentInDBImpl;
	private final TimeSlotRepository timeSlotRepository;
	private final TimeSlotMapper timeSlotMapper;

	@Transactional
	public List<TimeSlotDto> refreshAllTimeSlotInDB() {
		List<TimeSlotDto> allTimeSlot = this.allTimeSlotFromSupabase.getAllTimeSlot();

		Map<UUID, List<TimeSlotDto>> timeSlotsByEstablishment = allTimeSlot.stream()
				.collect(Collectors.groupingBy(TimeSlotDto::getEstablishmentId));
		log.debug("Number establishment from supabase: {}", timeSlotsByEstablishment.keySet().size());

		List<UUID> missingEstablishmentIdsCall1 = getMissingEstablishmentIds(timeSlotsByEstablishment);
		log.debug("Number establishment missing in DB: {}", missingEstablishmentIdsCall1.size());

		this.refreshEstablishmentInDBImpl.refreshEstablishmentInDBForMissings(missingEstablishmentIdsCall1);

		List<UUID> missingEstablishmentIdsCall2 = getMissingEstablishmentIds(timeSlotsByEstablishment);

		missingEstablishmentIdsCall2.forEach(timeSlotsByEstablishment::remove);
		log.debug("Number establishment to insert in table time_slot: {}", timeSlotsByEstablishment.size());

		deleteAndSaveTimeSlot(timeSlotsByEstablishment);
		return allTimeSlot;
	}

	private void deleteAndSaveTimeSlot(Map<UUID, List<TimeSlotDto>> timeSlotsByEstablishment) {
		timeSlotsByEstablishment.forEach((establishmentId, timeSlots) -> {
			this.timeSlotRepository.deleteAllByEstablishmentId(establishmentId);

			Establishment establishment = this.establishmentRepository.getReferenceById(establishmentId);
			List<TimeSlot> entities = timeSlots.stream()
					.map(timeSlot -> this.timeSlotMapper.toEntity(timeSlot, establishment))
					.toList();
			this.timeSlotRepository.saveAll(entities);
		});
	}

	private List<UUID> getMissingEstablishmentIds(Map<UUID, List<TimeSlotDto>> timeSlotsByEstablishment) {
		return timeSlotsByEstablishment.keySet().stream()
				.filter(establishmentId -> !this.establishmentRepository.existsById(establishmentId))
				.toList();
	}

}
