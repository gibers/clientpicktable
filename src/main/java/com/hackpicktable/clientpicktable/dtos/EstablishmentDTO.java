package com.hackpicktable.clientpicktable.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@NoArgsConstructor
public class EstablishmentDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("google_maps_url")
    private String googleMapsUrl;

    @JsonProperty("google_business_url")
    private String googleBusinessUrl;

    @JsonProperty("website_url")
    private String websiteUrl;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("plan_type")
    private String planType;

    @JsonProperty("trial_ends_at")
    private OffsetDateTime trialEndsAt;

    @JsonProperty("tripadvisor_review_url")
    private String tripadvisorReviewUrl;

    @JsonProperty("yelp_review_url")
    private String yelpReviewUrl;

    @JsonProperty("facebook_review_url")
    private String facebookReviewUrl;

    @JsonProperty("trustpilot_review_url")
    private String trustpilotReviewUrl;

    @JsonProperty("thefork_review_url")
    private String theforkReviewUrl;

    @JsonProperty("preferred_review_platform")
    private String preferredReviewPlatform;

    @JsonProperty("booking_notice")
    private String bookingNotice;

    @JsonProperty("onboarding_completed")
    private Boolean onboardingCompleted;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("floor_plan_viewport")
    private JsonNode floorPlanViewport;

    @JsonProperty("floor_plan_v2_enabled")
    private Boolean floorPlanV2Enabled;
}
