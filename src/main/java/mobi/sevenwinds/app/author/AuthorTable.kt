package mobi.sevenwinds.app.author

import mobi.sevenwinds.Const
import mobi.sevenwinds.app.budget.BudgetTable.defaultExpression
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime

object AuthorTable : IntIdTable("author") {
    val fio = varchar("fio", Const.FIO_MAX_LENGTH)
    val date = datetime("date")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var date by AuthorTable.date.defaultExpression(CurrentDateTime())

    fun toResponse(): AuthorRecordToResponse {
        return AuthorRecordToResponse(id.value, fio, date.millis)
    }
}