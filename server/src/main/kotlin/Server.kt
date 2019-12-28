import io.javalin.Javalin
import io.javalin.http.staticfiles.Location

fun main(args: Array<String>) {
    println("GHahhhhhh")

    var app = Javalin.create().start(8080);
    app.config.addStaticFiles("html", Location.EXTERNAL)
//    app.get("/") { ctx -> ctx.result("make a pick") };
}