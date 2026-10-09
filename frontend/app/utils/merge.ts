/* eslint-disable array-element-newline */

export const mergeUi = (override: object, defaults: object) => {
	const merged: Record<string, unknown> = { ...defaults }
	for (const [slot, cls] of Object.entries(override)) {
		merged[slot] = [defaults[slot as keyof typeof defaults], cls]
	}
	return merged
}
