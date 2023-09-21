package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.Length
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import mobi.sevenwinds.Const

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add") {
            post<Unit, AuthorRecordToResponse, AuthorRecord>(info("Добавить автора")) { param, body ->
                respond(AuthorService.addRecord(body))
            }
        }
    }
}

data class AuthorRecord(
    @Length(Const.FIO_MIN_LENGTH, Const.FIO_MAX_LENGTH) val fio: String,
)

data class AuthorRecordToResponse(
    val id: Int,
    val fio: String,
    val date: Long,
)