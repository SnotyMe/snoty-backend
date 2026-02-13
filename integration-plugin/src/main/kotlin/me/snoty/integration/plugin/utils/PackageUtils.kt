package me.snoty.integration.plugin.utils

fun groupCommonPackages(packageNames: List<String>): List<String> {
    if (packageNames.isEmpty()) return emptyList()
    if (packageNames.size == 1) return listOf(packageNames.first().substringBeforeLast('.'))

    val splitPackages = packageNames.map { it.split('.') }
    val commonPrefix = splitPackages.reduce { acc, list ->
        acc.zip(list).takeWhile { it.first == it.second }.map { it.first }
    }

    // If the common prefix is only the top-level (single segment), treat as no common parent
    if (commonPrefix.size <= 1) {
        return packageNames.map { it.substringBeforeLast('.') }
    }

    val commonPackage = commonPrefix.joinToString(".")
    val parentPackages = packageNames.map { it.substringBeforeLast('.') }

    // If all parent packages are exactly the common package, return it as single group
    return if (parentPackages.all { it == commonPackage }) {
        listOf(commonPackage)
    } else {
        // Otherwise, return each input's parent package (keeps deeper groupings)
        parentPackages
    }
}
