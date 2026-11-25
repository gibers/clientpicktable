CREATE TABLE public.previous_reservation
(
    establishment_id        uuid PRIMARY KEY REFERENCES public.establishment(id),
    updated_at              timestamp(6) with time zone NOT NULL,
    previous_http_status    integer,
    interval_with_previous  integer NOT NULL,
    success                 boolean,
    param_reservation_state json NOT NULL,
    list_start_time         time[]
);

CREATE TABLE public.previous_tisch
(
    establishment_id 		uuid NOT NULL REFERENCES public.previous_reservation(establishment_id) ON DELETE CASCADE,
    capacity         		integer NOT NULL,
    table_id         		uuid NOT NULL,
    previous_tisch_order 	integer NOT NULL,
    PRIMARY KEY (establishment_id, previous_tisch_order)
);

ALTER TABLE public.previous_reservation OWNER TO "user";
ALTER TABLE public.previous_tisch OWNER TO "user";
