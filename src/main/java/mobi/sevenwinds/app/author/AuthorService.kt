package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addAuthor(body: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val now = DateTime.now()
            val entity = AuthorEntity.new {
                this.fullName = body.fullName
                this.creationDate = now
            }

            return@transaction entity.toResponse()
        }
    }
}