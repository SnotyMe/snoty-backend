import type { User } from "./user.ts"

export type AuthComposable = () => {
	user: User | undefined
}
