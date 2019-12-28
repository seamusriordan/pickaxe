import io.javalin.Javalin
import io.javalin.http.staticfiles.Location

fun main(args: Array<String>) {
    var app = Javalin.create().start(8080);
    app.config.addStaticFiles("html", Location.EXTERNAL)
}