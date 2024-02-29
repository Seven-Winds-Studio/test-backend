package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val foundAuthorId = body.authorId?.let {
                AuthorEntity.find { AuthorTable.id eq it }.singleOrNull()
            }?.id

            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = foundAuthorId
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {

            // проверить чувствительность к регистру
            val foundAuthorId = param.author?.let {
                AuthorEntity.find { AuthorTable.name like it }.singleOrNull()?.id
            }

            val query = BudgetTable.select { BudgetTable.year eq param.year }

            if (foundAuthorId != null) {
                query.andWhere { BudgetTable.authorId eq foundAuthorId }
            }

            query.orderBy(BudgetTable.month to SortOrder.ASC)
                .orderBy(BudgetTable.amount to SortOrder.DESC)

            val total = query.count()

            val sumByType = BudgetEntity.wrapRows(query)
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            query.limit(param.limit, offset = param.offset)
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}