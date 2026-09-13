-- Spring Modulith's JPA event publication registry (spring-modulith-starter-jpa).
-- Hibernate only validates this table (ddl-auto: validate); it never creates it,
-- so it must be provisioned through Flyway. Columns/types mirror exactly what
-- Hibernate 7 (PostgreSQLDialect) expects for
-- org.springframework.modulith.events.jpa.updating.DefaultJpaEventPublication,
-- mapped through Spring Boot's default naming strategy.
create table public.event_publication
(
    id                     uuid                        not null primary key,
    listener_id            varchar(255)                not null,
    event_type             varchar(255)                not null,
    serialized_event       varchar(255)                not null,
    publication_date       timestamp(6) with time zone not null,
    completion_date        timestamp(6) with time zone,
    last_resubmission_date timestamp(6) with time zone,
    completion_attempts    integer                     not null,
    status                 varchar(255)
        constraint event_publication_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PUBLISHED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'RESUBMITTED'::character varying])::text[]))
);

-- Speeds up Spring Modulith's lookup of incomplete publications (completion_date IS NULL).
create index event_publication_by_completion_date_idx
    on public.event_publication (completion_date);
