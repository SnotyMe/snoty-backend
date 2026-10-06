import type {
	SchemaField,
	SchemaFieldDetailsEnumDetails,
	SchemaFieldDetailsEnumDetailsEnumConstant,
	SchemaFieldDetailsObjectDetails,
	SchemaFieldDetailsPlaintextDetails,
} from "~/api/backend/generated"

export function getDefaultValue(field: SchemaField): unknown {
	const defaultValue = field.defaultValue ?? undefined
	switch (field.type) {
		case "Enum": {
			const constants = (field.details as SchemaFieldDetailsEnumDetails).values
			return tryParseEnum(defaultValue, constants) ?? constants[0]?.value
		}
		case "Boolean": {
			const result = tryParseBoolean(defaultValue)
			return result ?? false
		}
		case "Plaintext": {
			const deprecatedDefaultValue = (field.details as SchemaFieldDetailsPlaintextDetails)?.defaultValue
			if (deprecatedDefaultValue !== undefined && deprecatedDefaultValue !== "") return deprecatedDefaultValue
			return defaultValue ?? ""
		}
		case "Map":
			return {}
		case "Object": {
			const details = (field.details as SchemaFieldDetailsObjectDetails)?.schema
			return details === undefined
				? {}
				: Object.fromEntries(details.map(f => [f.name,
						getDefaultValue(f)]))
		}
		case "List":
		case "Collection":
			return []
		case "Credential":
			return undefined
		default:
			return defaultValue
	}
}

function tryParseEnum(value: string | undefined, constants: SchemaFieldDetailsEnumDetailsEnumConstant[]): string | undefined {
	if (value === undefined) return undefined
	return constants.find(c => c.value === value)?.value
}

function tryParseBoolean(value: string | undefined): boolean | undefined {
	switch (value?.toLowerCase()) {
		case "true":
			return true
		case "false":
			return false
		default:
			return undefined
	}
}

export function defaultRecordFromSchema(fields: SchemaField[]): Record<string, unknown> {
	const record: Record<string, unknown> = {}
	for (const field of fields) {
		record[field.name] = getDefaultValue(field)
	}
	return record
}
