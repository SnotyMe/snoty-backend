import type { NodeType } from "~/types/node.ts"
import type { Locale } from "~/types/i18n.ts"

export type NodeDocs = Record<NodeType, Record<Locale, string>>
