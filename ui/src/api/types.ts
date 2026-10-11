// class auth.EmailCodeRequest
export interface EmailCodeRequest {code: string; email: Email}
// class auth.EmailRequest
export interface EmailRequest {email: Email}
// class db.Entity
export interface Entity<T> {createdAt: Instant; id: Id<any>}
// class todos.Todo
export interface Todo {completedAt?: Instant; id: Id<Todo>; item: string; createdAt: Instant}
// class users.Role
export enum Role {ADMIN = 'ADMIN', USER = 'USER'}
// class users.User
export interface User {avatarUrl?: URI; email: Email; firstName: string; id: Id<User>; lang: string; lastLoginAt?: Instant; lastName: string; name: string; refreshToken?: string; role: Role; updatedAt?: Instant; createdAt: Instant}

// klite.Id
export type Id<T> = string & {_of?: T}
// java.time.Instant
export type Instant = `${number}-${number}-${number}T${number}:${number}:${number}Z`
// java.net.URI
export type URI = `${string}://${string}`
// klite.Email
export type Email = `${string}@${string}`
