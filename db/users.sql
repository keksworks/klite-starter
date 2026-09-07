--changeset users
create table users(
  ${id},
  name text not null,
  email text unique not null,
  avatarUrl text,
  lang text not null default 'en',
  role text not null,
  lastLoginAt timestamptz,
  updatedAt timestamptz not null default now(),
  createdAt timestamptz not null default now(),
  ${createdBy}
);
