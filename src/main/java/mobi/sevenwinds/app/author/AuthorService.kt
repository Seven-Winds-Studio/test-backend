package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {

    suspend fun addRecord(params: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            return@transaction AuthorEntity.new {
                name = params.name
                dateTime = DateTime()
            }.toResponse()
        }
    }
}