--changeset email_codes
create table email_codes(
  ${id},
  email text not null,
  codeHash text not null,
  ip text not null,
  attempts int not null default 0,
  sentAt timestamptz not null,
  usedAt timestamptz
);

--changeset email_codes_email_idx
create index email_codes_email_idx on email_codes(email, sentAt);

--changeset email_codes_ip_idx
create index email_codes_ip_idx on email_codes(ip, sentAt);

--changeset email_codes_app-grant
grant delete on email_codes to app;
