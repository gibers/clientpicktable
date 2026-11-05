package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationSettingsDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("is_paused")
    private Boolean paused;

    @JsonProperty("pause_message")
    private String pauseMessage;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("min_booking_days")
    private Integer minBookingDays;

    @JsonProperty("max_booking_days")
    private Integer maxBookingDays;

    @JsonProperty("establishment_id")
    @NotNull
    private UUID establishmentId;

    @JsonProperty("allow_same_day_cancellation")
    private Boolean allowSameDayCancellation;

    @JsonProperty("min_people")
    private Integer minPeople;

    @JsonProperty("max_people")
    private Integer maxPeople;

    @JsonProperty("min_booking_minutes")
    private Integer minBookingMinutes;

    @JsonProperty("booking_mode")
    private String bookingMode;

    @JsonProperty("booking_interval_minutes")
    private Integer bookingIntervalMinutes;

    @JsonProperty("reservation_duration_minutes")
    private Integer reservationDurationMinutes;

    @JsonProperty("spam_alert_enabled")
    private Boolean spamAlertEnabled;

    @JsonProperty("spam_alert_threshold")
    private Integer spamAlertThreshold;

    @JsonProperty("spam_alert_period_minutes")
    private Integer spamAlertPeriodMinutes;

    @JsonProperty("large_group_email_enabled")
    private Boolean largeGroupEmailEnabled;

    @JsonProperty("large_group_email")
    private String largeGroupEmail;

    @JsonProperty("max_reservations_per_time")
    private Integer maxReservationsPerTime;

    @JsonProperty("time_limit_type")
    private String timeLimitType;

    @JsonProperty("max_people_per_time")
    private Integer maxPeoplePerTime;

    @JsonProperty("children_enabled")
    private Boolean childrenEnabled;

    @JsonProperty("children_capacity_mode")
    private String childrenCapacityMode;

    @JsonProperty("children_help_text")
    private String childrenHelpText;

    @JsonProperty("stroller_enabled")
    private Boolean strollerEnabled;

    @JsonProperty("stroller_help_text")
    private String strollerHelpText;

    @JsonProperty("same_day_cutoff_time")
    private LocalTime sameDayCutoffTime;

    @JsonProperty("large_group_contact_method")
    private String largeGroupContactMethod;

    @JsonProperty("large_group_phone")
    private String largeGroupPhone;

    @JsonProperty("large_group_message")
    private String largeGroupMessage;

    @JsonProperty("terrace_enabled")
    private Boolean terraceEnabled;

    @JsonProperty("terrace_help_text")
    private String terraceHelpText;

    @JsonProperty("custom_assignment_order_enabled")
    private Boolean customAssignmentOrderEnabled;

    @JsonProperty("email_marketing_enabled")
    private Boolean emailMarketingEnabled;

    @JsonProperty("sms_marketing_enabled")
    private Boolean smsMarketingEnabled;

    @JsonProperty("zones_enabled")
    private Boolean zonesEnabled;

    @JsonProperty("zones_selection_mode")
    private String zonesSelectionMode;

    @JsonProperty("zones_help_text")
    private String zonesHelpText;

    @JsonProperty("zones_strict")
    private Boolean zonesStrict;

    @JsonProperty("show_phone_on_blocked_actions")
    private Boolean showPhoneOnBlockedActions;

    @JsonProperty("show_email_on_blocked_actions")
    private Boolean showEmailOnBlockedActions;

    @JsonProperty("blocked_actions_phone")
    private String blockedActionsPhone;

    @JsonProperty("blocked_actions_email")
    private String blockedActionsEmail;

    @JsonProperty("require_phone")
    private Boolean requirePhone;

    @JsonProperty("booking_title")
    private String bookingTitle;
}
