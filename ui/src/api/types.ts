export type Id<T extends Entity<T>> = string & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}

// class todos.Todo
export interface Todo {completedAt?: Instant; id: Id<Todo>; item: string}
// class users.Role
export enum Role {ADMIN = 'ADMIN', USER = 'USER'}
// class users.User
export interface User {firstName: string; id: Id<User>; lastName: string; role: Role}

// java.time.Instant
export type Instant = `${number}-${number}-${number}T${number}:${number}:${number}Z`
