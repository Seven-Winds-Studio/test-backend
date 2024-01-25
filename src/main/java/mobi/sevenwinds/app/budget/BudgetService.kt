package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId?.let { AuthorEntity[it].id }
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {

            val query: Query = if(param.authorName != null){
                (BudgetTable innerJoin AuthorTable)
                    .select {
                        (BudgetTable.year eq param.year) and
                                (AuthorTable.fullName.lowerCase() like "%${param.authorName.toLowerCase()}%")
                    }
            } else {
                BudgetTable.select { BudgetTable.year eq param.year }
            }

            val total = query.count()

            val sumByType = BudgetEntity
                .wrapRows(query)
                .map { it.toResponse() }
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            val paginatedAndSortedQuery = query.limit(param.limit, param.offset)
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)

            val allByYearPaginated = BudgetEntity
                .wrapRows(paginatedAndSortedQuery)
                .map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = allByYearPaginated
            )
        }
    }
}