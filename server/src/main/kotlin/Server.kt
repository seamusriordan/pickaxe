import io.javalin.Javalin

fun main(args: Array<String>) {
    println("GHahhhhhh")

    var app = Javalin.create().start(8080);
    app.get("/") { ctx -> ctx.result("make a pick") };
}