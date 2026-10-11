--substitute id=id bigint primary key
--substitute createdBy=createdBy bigint default get_app_user()
--substitute createdAt=createdAt timestamptz not null default now()
--substitute updatedAt=updatedAt timestamptz not null default now()

--include app_user.sql
--include change_history.sql
--include users.sql
--include todos.sql
