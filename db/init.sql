create table if not exists seats (
  id bigint primary key,
  section varchar(50),
  seat_number varchar(10),
  created_at timestamp default now()
);

create table if not exists reservations (
  id bigserial primary key,
  seat_id bigint not null,
  user_id varchar(50) not null,
  reserved_at timestamp default now(),
  constraint uk_seat unique (seat_id)
);
