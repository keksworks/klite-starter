--changeset users
create table users(
  ${id},
  firstName text not null,
  lastName text not null,
  email text unique not null,
  avatarUrl text,
  lang text not null default 'en',
  role text not null,
  refreshToken text,
  lastLoginAt timestamptz,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);

--changeset user_history_trigger
create trigger user_history_trigger after update on users for each row execute procedure add_change_history();
