--changeset set_app_user onChange:RUN separator:none
create or replace procedure set_app_user(userId bigint) language plpgsql as $$
begin
  perform set_config('app.user', userId::text, true);
end; $$

--changeset get_app_user onChange:RUN separator:none
create or replace function get_app_user() returns bigint language plpgsql as $$
declare
  userId text;
begin
  userId := current_setting('app.user', true);
  return case when userId = '' then null else userId end;
end; $$

--changeset change_history
create table change_history(
   "table" text not null,
   rowId bigint not null,
   old jsonb not null default '{}',
   new jsonb not null default '{}',
   changedAt timestamptz not null default now(),
   changedBy bigint default get_app_user()
);

--changeset change_history:revoke-app-update
revoke update on change_history from app;

--changeset change_history_idx
create index change_history_idx on change_history("table", rowId);

--changeset add_change_history onChange:RUN separator:none
create or replace function add_change_history() returns trigger language plpgsql as $$
declare
  col text;
  oldValue text;
  newValue text;
  newRec jsonb := to_jsonb(new);
  oldRec jsonb := case when tg_op = 'INSERT' then '{}'::jsonb else to_jsonb(old) end;
  oldChanges jsonb := '{}'::jsonb;
  newChanges jsonb := '{}'::jsonb;
begin
  if (tg_op = 'UPDATE') then
    for col, newValue in select * from jsonb_each_text(newRec) loop
        if (col not in ('updatedat', 'projectid', 'id')) then
          oldValue := oldRec->>col;
          if (oldValue is distinct from newValue) then
            if (oldValue is not null) then
              oldChanges := oldChanges || jsonb_build_object(col, oldValue);
            end if;
            newChanges := newChanges || jsonb_build_object(col, newValue);
          end if;
        end if;
      end loop;
  end if;

  if (tg_op = 'INSERT' or newChanges <> '{}'::jsonb) then
    insert into change_history ("table", rowId, old, new)
    values (tg_table_name, new.id, oldChanges, newChanges);
  end if;

  return new;
end; $$
