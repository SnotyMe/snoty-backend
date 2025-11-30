package me.snoty.backend.utils.http

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
private val userConfigField: KProperty1<HttpClient, HttpClientConfig<OkHttpConfig>> = HttpClient::class.declaredMemberProperties
	.single { it.name == "userConfig" }
	.apply {
		isAccessible = true
	} as KProperty1<HttpClient, HttpClientConfig<OkHttpConfig>>

fun HttpClient.clone(block: HttpClientConfig<OkHttpConfig>.() -> Unit = {}): HttpClient {
	val oldConfig = userConfigField.get(this)

	return HttpClient(OkHttp) {
		this.plusAssign(oldConfig)
		block()
	}
}
