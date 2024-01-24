package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
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
        val total = getTotalCount(param)
        val data = getBudgetData(param)
        val totalByType = getTotalByType(param)

        return@withContext BudgetYearStatsResponse(
            total = total,
            totalByType = totalByType,
            items = data
        )
    }

    private suspend fun getTotalCount(param: BudgetYearParam): Int = withContext(Dispatchers.IO) {
        transaction {
            if (param.authorFilter != null) {
                BudgetTable.innerJoin(AuthorTable)
                    .select {
                        (BudgetTable.year eq param.year) and
                                (AuthorTable.fullName.lowerCase() like "%${param.authorFilter.toLowerCase()}%")
                    }.count()
            } else {
                BudgetTable.select { BudgetTable.year eq param.year }.count()
            }
        }
    }

    private suspend fun getBudgetData(param: BudgetYearParam): List<BudgetRecord> = withContext(Dispatchers.IO) {
        transaction {
            if (param.authorFilter != null) {
                val budgetQuery: Query = (BudgetTable innerJoin AuthorTable)
                    .select {
                        (BudgetTable.year eq param.year) and
                                (AuthorTable.fullName.lowerCase() like "%${param.authorFilter.toLowerCase()}%")
                    }
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                    .limit(param.limit, param.offset)
                BudgetEntity.wrapRows(budgetQuery).map { it.toResponse() }
            } else {
                val budgetQuery: Query = BudgetTable
                    .select { BudgetTable.year eq param.year }
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                    .limit(param.limit, param.offset)
                BudgetEntity.wrapRows(budgetQuery).map { it.toResponse() }
            }
        }
    }

    private suspend fun getTotalByType(param: BudgetYearParam): Map<String, Int> = withContext(Dispatchers.IO) {
        transaction {
            val budgetEntities = if (param.authorFilter != null) {
                BudgetEntity.wrapRows(
                    (BudgetTable innerJoin AuthorTable)
                        .select {
                            (BudgetTable.year eq param.year) and
                                    (AuthorTable.fullName.lowerCase() like "%${param.authorFilter.toLowerCase()}%")
                        }
                )
            } else {
                BudgetEntity.wrapRows(
                    BudgetTable.select { BudgetTable.year eq param.year }
                )
            }
            budgetEntities.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }
        }
    }


}