export function tOrUndefined(t: (key: string) => string, key: string) {
	const translated = t(key)
	if (translated === key) return undefined
	return translated
}
