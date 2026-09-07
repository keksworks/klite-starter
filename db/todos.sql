--changeset todos
create table todos(
  ${id},
  item text not null,
  completedAt timestamptz
);

--changeset todo_history_trigger
create trigger todo_history_trigger after update on todos for each row execute procedure add_change_history();
