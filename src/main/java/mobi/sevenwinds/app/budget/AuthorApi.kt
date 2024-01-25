package mobi.sevenwinds.app.budget

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, AuthorCreateRecord>(info("Добавить автора")) { param, body ->
            respond(AuthorService.addRecord(body))
        }
    }
}

data class AuthorRecord(
    val fullName: String,
    val createdAt: DateTime
)

data class AuthorCreateRecord(
    val fullName: String,
)