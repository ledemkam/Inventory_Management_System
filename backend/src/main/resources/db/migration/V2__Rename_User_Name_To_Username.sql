alter table public.users
    rename column name to username;

alter table public.users
    add constraint users_username_unique unique (username);
