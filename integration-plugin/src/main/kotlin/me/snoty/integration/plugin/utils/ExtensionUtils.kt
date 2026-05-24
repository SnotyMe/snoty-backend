package me.snoty.integration.plugin.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver

@OptIn(KspExperimental::class)
fun Resolver.getExtensionName() = getModuleName().getShortName()
    .replaceFirstChar(Char::uppercase)
    .replace("-(\\w)".toRegex()) { matchResult ->
        matchResult.groupValues[1].uppercase()
    }
