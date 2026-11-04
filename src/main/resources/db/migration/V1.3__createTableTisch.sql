CREATE TABLE public.tisch
(
    id                  uuid primary key,
    name                varchar(70) not null,
    capacity            integer not null,
    is_active           boolean not null,
    notes               text,
    created_at          timestamp(6) with time zone not null,
    establishment_id    uuid not null references public.establishment(id),
    is_bookable         boolean not null,
    allows_children     boolean not null,
    allows_stroller     boolean not null,
    is_terrace          boolean not null,
    min_capacity        integer not null,
    assignment_priority integer not null,
    updated_at          timestamp(6) with time zone not null,
    zone_id             uuid,
    display_order       integer not null
);

ALTER TABLE public.tisch OWNER TO "user";
