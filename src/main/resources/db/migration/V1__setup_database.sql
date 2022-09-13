create table person
(
    id                     uuid primary key not null,
    first_name             varchar not null,
    last_name              varchar not null,
    date_of_birth          date not null,
    street                 varchar not null,
    house_number           integer not null,
    house_number_additions varchar,
    postal_code            varchar not null,
    city                   varchar not null,
    country                varchar not null
);

create table pet
(
    id        uuid primary key not null,
    name      varchar not null,
    age       integer not null,
    person_id uuid
        constraint person_id_fk references person(id)
            on delete set null
);

create table "user"
(
    id       uuid primary key not null,
    username varchar not null,
    password varchar not null,
    roles    varchar[],
    enabled  boolean not null,
    locked   boolean not null
);

insert into "user" (id, username, password, roles, enabled, locked)
values (gen_random_uuid(), 'user', '$2a$12$9RY/1po134WtqcURPc9l1.P4jGH/jvEIfK4Hfr.5ZTlemeAr.a6Ea', '{ROLE_USER}', true, false), -- user:user
       (gen_random_uuid(), 'admin', '$2a$12$5xP4JvgNaeF7UkPvSUD5xOO6dQACesKmAbolv51vi4dStwJEQmMwy', '{ROLE_ADMIN}',  true, false); -- admin:admin