create table public.categories
(
    id          varchar(255) not null primary key,
    created_at  timestamp(6) not null,
    expiry_date timestamp(6),
    updated_at  timestamp(6) not null,
    name        varchar(255)
        constraint category_name_unique_constraint
            unique
);



create table public.products
(
    id             varchar(255) not null primary key,
    created_at     timestamp(6) not null,
    expiry_date    timestamp(6),
    updated_at     timestamp(6) not null,
    description    varchar(255),
    image_url      varchar(255),
    name           varchar(255),
    price          numeric(38, 2),
    sku            varchar(255)
        constraint products_sku_unique_constraint unique,
    stock_quantity integer,
    category_id    varchar(255) not null
        constraint fk_category_id
            references public.categories
        references public.categories
);



create table public.suppliers
(
    id          varchar(255) not null primary key,
    created_at  timestamp(6) not null,
    expiry_date timestamp(6),
    updated_at  timestamp(6) not null,
    address     varchar(255),
    name        varchar(255) not null
);



create table public.users
(
    id           varchar(255) not null primary key,
    created_at   timestamp(6) not null,
    expiry_date  timestamp(6),
    updated_at   timestamp(6) not null,
    email        varchar(255) not null
        constraint users_email_unique unique,
    name         varchar(255) not null,
    password     varchar(255) not null,
    phone_number varchar(255),
    role         varchar(255) not null
        constraint users_role_check
            check ((role)::text = ANY ((ARRAY ['ADMIN'::character varying, 'MANAGER'::character varying])::text[]))
);

create table public.transactions
(
    id               varchar(255) not null
        primary key,
    created_at       timestamp(6) not null,
    expiry_date      timestamp(6),
    updated_at       timestamp(6) not null,
    description      text         not null,
    status           varchar(255) not null
        constraint transactions_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'CANCELED'::character varying])::text[])),
    total_price      numeric(38, 2),
    total_products   integer,
    transaction_type varchar(255) not null
        constraint transactions_transaction_type_check
            check ((transaction_type)::text = ANY
                   ((ARRAY ['PURCHASE'::character varying, 'SALE'::character varying, 'RETURN_TO_SUPPLIER'::character varying])::text[])),
    product_id       varchar(255)
        constraint fk_product_id references public.products,
    supplier_id      varchar(255)
        constraint fk_supplier_id references public.suppliers,
    user_id          varchar(255)
        constraint fk_user_id references public.users
);

