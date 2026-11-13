
CREATE TABLE public.time_slot
(
    id                          uuid primary key,
    start_time                  time,
    end_time                    time,
    is_active                   boolean,
    created_at                  timestamp(6) with time zone,
    available_days              integer[],
    establishment_id            uuid references public.establishment(id),
    duration_override_minutes   integer
);

ALTER TABLE public.time_slot OWNER TO "user";
