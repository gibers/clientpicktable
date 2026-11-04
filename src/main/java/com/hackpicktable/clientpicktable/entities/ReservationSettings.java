package com.hackpicktable.clientpicktable.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation_settings", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ReservationSettings {

    @Id
    @Column(name = "id", nullable = false)
    @JsonProperty("id")
    private UUID id;

    @Column(name = "is_paused")
    @JsonProperty("is_paused")
    private Boolean paused;

    @Column(name = "pause_message", columnDefinition = "text")
    @JsonProperty("pause_message")
    private String pauseMessage;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "min_booking_days")
    @JsonProperty("min_booking_days")
    private Integer minBookingDays;

    @Column(name = "max_booking_days")
    @JsonProperty("max_booking_days")
    private Integer maxBookingDays;

    @ManyToOne
    @JoinColumn(name = "establishment_id")
    @JsonIgnore
    private Establishment establishment;

    @Column(name = "allow_same_day_cancellation")
    @JsonProperty("allow_same_day_cancellation")
    private Boolean allowSameDayCancellation;

    @Column(name = "min_people")
    @JsonProperty("min_people")
    private Integer minPeople;

    @Column(name = "max_people")
    @JsonProperty("max_people")
    private Integer maxPeople;

    @Column(name = "min_booking_minutes")
    @JsonProperty("min_booking_minutes")
    private Integer minBookingMinutes;

    @Column(name = "booking_mode", length = 50)
    @JsonProperty("booking_mode")
    private String bookingMode;

    @Column(name = "booking_interval_minutes")
    @JsonProperty("booking_interval_minutes")
    private Integer bookingIntervalMinutes;

    @Column(name = "reservation_duration_minutes")
    @JsonProperty("reservation_duration_minutes")
    private Integer reservationDurationMinutes;

    @Column(name = "spam_alert_enabled")
    @JsonProperty("spam_alert_enabled")
    private Boolean spamAlertEnabled;

    @Column(name = "spam_alert_threshold")
    @JsonProperty("spam_alert_threshold")
    private Integer spamAlertThreshold;

    @Column(name = "spam_alert_period_minutes")
    @JsonProperty("spam_alert_period_minutes")
    private Integer spamAlertPeriodMinutes;

    @Column(name = "large_group_email_enabled")
    @JsonProperty("large_group_email_enabled")
    private Boolean largeGroupEmailEnabled;

    @Column(name = "large_group_email", columnDefinition = "text")
    @JsonProperty("large_group_email")
    private String largeGroupEmail;

    @Column(name = "max_reservations_per_time")
    @JsonProperty("max_reservations_per_time")
    private Integer maxReservationsPerTime;

    @Column(name = "time_limit_type", length = 50)
    @JsonProperty("time_limit_type")
    private String timeLimitType;

    @Column(name = "max_people_per_time")
    @JsonProperty("max_people_per_time")
    private Integer maxPeoplePerTime;

    @Column(name = "children_enabled")
    @JsonProperty("children_enabled")
    private Boolean childrenEnabled;

    @Column(name = "children_capacity_mode", length = 50)
    @JsonProperty("children_capacity_mode")
    private String childrenCapacityMode;

    @Column(name = "children_help_text", columnDefinition = "text")
    @JsonProperty("children_help_text")
    private String childrenHelpText;

    @Column(name = "stroller_enabled")
    @JsonProperty("stroller_enabled")
    private Boolean strollerEnabled;

    @Column(name = "stroller_help_text", columnDefinition = "text")
    @JsonProperty("stroller_help_text")
    private String strollerHelpText;

    @Column(name = "same_day_cutoff_time")
    @JsonProperty("same_day_cutoff_time")
    private LocalTime sameDayCutoffTime;

    @Column(name = "large_group_contact_method", length = 50)
    @JsonProperty("large_group_contact_method")
    private String largeGroupContactMethod;

    @Column(name = "large_group_phone", columnDefinition = "text")
    @JsonProperty("large_group_phone")
    private String largeGroupPhone;

    @Column(name = "large_group_message", columnDefinition = "text")
    @JsonProperty("large_group_message")
    private String largeGroupMessage;

    @Column(name = "terrace_enabled")
    @JsonProperty("terrace_enabled")
    private Boolean terraceEnabled;

    @Column(name = "terrace_help_text", columnDefinition = "text")
    @JsonProperty("terrace_help_text")
    private String terraceHelpText;

    @Column(name = "custom_assignment_order_enabled")
    @JsonProperty("custom_assignment_order_enabled")
    private Boolean customAssignmentOrderEnabled;

    @Column(name = "email_marketing_enabled")
    @JsonProperty("email_marketing_enabled")
    private Boolean emailMarketingEnabled;

    @Column(name = "sms_marketing_enabled")
    @JsonProperty("sms_marketing_enabled")
    private Boolean smsMarketingEnabled;

    @Column(name = "zones_enabled")
    @JsonProperty("zones_enabled")
    private Boolean zonesEnabled;

    @Column(name = "zones_selection_mode", length = 50)
    @JsonProperty("zones_selection_mode")
    private String zonesSelectionMode;

    @Column(name = "zones_help_text", columnDefinition = "text")
    @JsonProperty("zones_help_text")
    private String zonesHelpText;

    @Column(name = "zones_strict")
    @JsonProperty("zones_strict")
    private Boolean zonesStrict;

    @Column(name = "show_phone_on_blocked_actions")
    @JsonProperty("show_phone_on_blocked_actions")
    private Boolean showPhoneOnBlockedActions;

    @Column(name = "show_email_on_blocked_actions")
    @JsonProperty("show_email_on_blocked_actions")
    private Boolean showEmailOnBlockedActions;

    @Column(name = "blocked_actions_phone", columnDefinition = "text")
    @JsonProperty("blocked_actions_phone")
    private String blockedActionsPhone;

    @Column(name = "blocked_actions_email", columnDefinition = "text")
    @JsonProperty("blocked_actions_email")
    private String blockedActionsEmail;

    @Column(name = "require_phone")
    @JsonProperty("require_phone")
    private Boolean requirePhone;

    @Column(name = "booking_title", columnDefinition = "text")
    @JsonProperty("booking_title")
    private String bookingTitle;
}
