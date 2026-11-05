CREATE TABLE public.last_patronymic_used
(
    last_id_used        bigint not null references public.patronymic(id),
    establishment_id    uuid not null references public.establishment(id),
    created_at 			timestamp(6) with time zone not null default now()
);

ALTER TABLE public.last_patronymic_used OWNER TO "user";

