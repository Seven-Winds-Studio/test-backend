package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Count
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Sum
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
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val countExp = Count(BudgetTable.id)
            val sumExp = Sum(BudgetTable.amount, BudgetTable.amount.columnType)

            val totalByType = HashMap<String, Int>()
            var count = 0

            BudgetTable.slice(BudgetTable.type, sumExp, countExp)
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)
                .map {
                    val type = it[BudgetTable.type]
                    val amount = it[sumExp] ?: 0

                    count += it[countExp]
                    totalByType[type.name] = amount
                }

            val queryItems = BudgetTable
                .select { BudgetTable.year eq param.year }
                .limit(param.limit, param.offset)
                .orderBy(
                    BudgetTable.month to SortOrder.ASC,
                    BudgetTable.amount to SortOrder.DESC,
                )

            val items = BudgetEntity.wrapRows(queryItems).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = count,
                totalByType = totalByType,
                items = items
            )
        }
    }
}