export function downloadText(filename: string, text: Blob | MediaSource) {
	const url = window.URL.createObjectURL(text)

	const a = document.createElement("a")
	a.style.display = "none"
	a.href = url
	a.download = filename

	document.body.appendChild(a)
	a.click()
	window.URL.revokeObjectURL(url)
}
