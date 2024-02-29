package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime

fun NormalOpenAPIRoute.budget() {
    route("/author") {
        route("/add").post<String, AuthorRecord, String>(info("Добавить автора")) { param, _ ->
            respond(AuthorService.addRecord(param))
        }
    }
}

data class AuthorRecord( val name: String, val dateTime: DateTime = DateTime())