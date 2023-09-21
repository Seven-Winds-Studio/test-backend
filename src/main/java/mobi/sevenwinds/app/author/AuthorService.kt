package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorRecordToResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fio = body.fio
                this.date = DateTime.now()
            }

            return@transaction entity.toResponse()
        }
    }
}