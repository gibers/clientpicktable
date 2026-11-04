package com.hackpicktable.clientpicktable.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "establishment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Establishment {

    @Id
    @Column(name = "id", nullable = false)
    @JsonProperty("id")
    private UUID id;

    @Column(name = "name", columnDefinition = "text")
    @JsonProperty("name")
    private String name;

    @Column(name = "email", columnDefinition = "text")
    @JsonProperty("email")
    private String email;

    @Column(name = "phone", columnDefinition = "text")
    @JsonProperty("phone")
    private String phone;

    @Column(name = "address", columnDefinition = "text")
    @JsonProperty("address")
    private String address;

    @Column(name = "city", columnDefinition = "text")
    @JsonProperty("city")
    private String city;

    @Column(name = "postal_code", length = 20)
    @JsonProperty("postal_code")
    private String postalCode;

    @Column(name = "country", columnDefinition = "text")
    @JsonProperty("country")
    private String country;

    @Column(name = "google_maps_url", columnDefinition = "text")
    @JsonProperty("google_maps_url")
    private String googleMapsUrl;

    @Column(name = "google_business_url", columnDefinition = "text")
    @JsonProperty("google_business_url")
    private String googleBusinessUrl;

    @Column(name = "website_url", columnDefinition = "text")
    @JsonProperty("website_url")
    private String websiteUrl;

    @Column(name = "logo_url", columnDefinition = "text")
    @JsonProperty("logo_url")
    private String logoUrl;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "slug", length = 255)
    @JsonProperty("slug")
    private String slug;

    @Column(name = "is_active")
    @JsonProperty("is_active")
    private Boolean active;

    @Column(name = "plan_type", length = 50)
    @JsonProperty("plan_type")
    private String planType;

    @Column(name = "trial_ends_at")
    @JsonProperty("trial_ends_at")
    private OffsetDateTime trialEndsAt;

    @Column(name = "tripadvisor_review_url", columnDefinition = "text")
    @JsonProperty("tripadvisor_review_url")
    private String tripadvisorReviewUrl;

    @Column(name = "yelp_review_url", columnDefinition = "text")
    @JsonProperty("yelp_review_url")
    private String yelpReviewUrl;

    @Column(name = "facebook_review_url", columnDefinition = "text")
    @JsonProperty("facebook_review_url")
    private String facebookReviewUrl;

    @Column(name = "trustpilot_review_url", columnDefinition = "text")
    @JsonProperty("trustpilot_review_url")
    private String trustpilotReviewUrl;

    @Column(name = "thefork_review_url", columnDefinition = "text")
    @JsonProperty("thefork_review_url")
    private String theforkReviewUrl;

    @Column(name = "preferred_review_platform", length = 50)
    @JsonProperty("preferred_review_platform")
    private String preferredReviewPlatform;

    @Column(name = "booking_notice", columnDefinition = "text")
    @JsonProperty("booking_notice")
    private String bookingNotice;

    @Column(name = "onboarding_completed")
    @JsonProperty("onboarding_completed")
    private Boolean onboardingCompleted;

    @Column(name = "latitude")
    @JsonProperty("latitude")
    private Double latitude;

    @Column(name = "longitude")
    @JsonProperty("longitude")
    private Double longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floor_plan_viewport", columnDefinition = "jsonb")
    @JsonProperty("floor_plan_viewport")
    private JsonNode floorPlanViewport;

    @Column(name = "floor_plan_v2_enabled")
    @JsonProperty("floor_plan_v2_enabled")
    private Boolean floorPlanV2Enabled;
}
