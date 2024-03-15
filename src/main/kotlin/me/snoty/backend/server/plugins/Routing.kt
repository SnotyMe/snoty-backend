package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.webjars.*

fun Application.configureRouting() {
	install(Webjars) {
		path = "/webjars" //defaults to /webjars
	}
	install(StatusPages) {
		exception<Throwable> { call, cause ->
			call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
		}
	}
	install(DoubleReceive)
	routing {
		get("/") {
			call.respondText("Hello World!")
		}
		get("/webjars") {
			call.respondText("<script src='/webjars/jquery/jquery.js'></script>", ContentType.Text.Html)
		}
		post("/double-receive") {
			val first = call.receiveText()
			val theSame = call.receiveText()
			call.respondText(first + " " + theSame)
		}
	}
}
