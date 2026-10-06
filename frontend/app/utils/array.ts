export function uniqueFilter<T, BY>(by: (value: T) => BY) {
	return (value: T, index: number, array: T[]) =>
		array.findIndex(it => by(it) === by(value)) === index
}
