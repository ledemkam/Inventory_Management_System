alter table public.users
    drop constraint users_role_check;

alter table public.users
    add constraint users_role_check
        check ((role)::text = ANY
               ((ARRAY ['ADMIN'::character varying, 'MANAGER'::character varying, 'USER'::character varying])::text[]));
