export type Id<T extends Entity<T>> = string & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}

// class auth.EmailCodeRequest
export interface EmailCodeRequest {code: string; email: Email}
// class auth.EmailRequest
export interface EmailRequest {email: Email}
// class todos.Todo
export interface Todo {completedAt?: Instant; id: Id<Todo>; item: string}
// class users.Role
export enum Role {ADMIN = 'ADMIN', USER = 'USER'}
// class users.User
export interface User {avatarUrl?: URI; createdAt: Instant; email: Email; firstName: string; id: Id<User>; lang: string; lastLoginAt?: Instant; lastName: string; name: string; refreshToken?: string; role: Role; updatedAt?: Instant}

// klite.Id
export type Id<T> = string & {_of?: T}
// java.time.Instant
export type Instant = `${number}-${number}-${number}T${number}:${number}:${number}Z`
// java.net.URI
export type URI = `${string}://${string}`
// klite.Email
export type Email = `${string}@${string}`
