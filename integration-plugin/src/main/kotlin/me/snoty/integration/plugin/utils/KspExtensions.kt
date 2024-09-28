package me.snoty.integration.plugin.utils

import com.squareup.kotlinpoet.MemberName
import kotlin.reflect.KFunction

inline fun <reified R : Any> KFunction<*>.getMemberName()
	= MemberName(R::class.qualifiedName!!, this.name)
