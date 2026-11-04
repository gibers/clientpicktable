CREATE TABLE public.establishment
(
    id                          uuid primary key,
    name                        text,
    email                       text,
    phone                       text,
    address                     text,
    city                        text,
    postal_code                 varchar(20),
    country                     text,
    google_maps_url             text,
    google_business_url         text,
    website_url                 text,
    logo_url                    text,
    created_at                  timestamp(6) with time zone,
    updated_at                  timestamp(6) with time zone,
    slug                        varchar(255),
    is_active                   boolean,
    plan_type                   varchar(50),
    trial_ends_at               timestamp(6) with time zone,
    tripadvisor_review_url      text,
    yelp_review_url             text,
    facebook_review_url         text,
    trustpilot_review_url       text,
    thefork_review_url          text,
    preferred_review_platform   varchar(50),
    booking_notice              text,
    onboarding_completed        boolean,
    latitude                    double precision,
    longitude                   double precision,
    floor_plan_viewport         jsonb,
    floor_plan_v2_enabled       boolean
);

ALTER TABLE public.establishment OWNER TO "user";

