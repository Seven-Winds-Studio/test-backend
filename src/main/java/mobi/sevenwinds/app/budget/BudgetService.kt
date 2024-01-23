package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
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
            val budgetQuery = (BudgetTable innerJoin AuthorTable)
                .slice(BudgetTable.columns + AuthorTable.fullName)
                .select {
                    (BudgetTable.year eq param.year) and
                            (AuthorTable.fullName.lowerCase() like "%${param.authorFilter?.toLowerCase()}%")
                }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)

            val total = BudgetTable.select { BudgetTable.year eq param.year }.count()
            val budgetEntities = BudgetEntity.wrapRows(budgetQuery)
            val data = budgetEntities.map { it.toResponse() }

            val allBudgetEntities = BudgetEntity.wrapRows(
                BudgetTable.select { BudgetTable.year eq param.year }
            )
            val totalByType = allBudgetEntities
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = totalByType,
                items = data
            )
        }
    }

}