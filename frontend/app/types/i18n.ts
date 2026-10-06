import type { locales } from "#shared/i18n"

export type Locale = typeof locales[number]["code"]
