package pt.guedes.m3u_transformer

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import pt.guedes.m3u_transformer.web.controller.PlaylistController

fun main() {

    Javalin.create()
        .routes {
            path("playlist") {
                post(PlaylistController::create)
                path(":id") {
                    get(PlaylistController::render)
                    put(PlaylistController::update)
                }
            }
        }
        .start(12345)
}