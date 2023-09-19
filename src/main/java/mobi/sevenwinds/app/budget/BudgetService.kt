package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.ExposedExtention.ilike
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
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
                if (body.authorId != null) this.author = AuthorEntity(EntityID(body.authorId, AuthorTable))
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = (BudgetTable leftJoin AuthorTable)
                .select { (BudgetTable.year eq param.year) }
                .orderBy(BudgetTable.month)
                .orderBy(BudgetTable.amount, SortOrder.DESC)
                .limit(param.limit, param.offset)

            param.authorFio?.let {
                query.andWhere { AuthorTable.fio ilike "%" + param.authorFio + "%" }
            }

            val queryTotal = BudgetTable
                .select { BudgetTable.year eq param.year }

            val total = queryTotal.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponseStats() }
            val dataTotal = BudgetEntity.wrapRows(queryTotal).map { it.toResponse() }

            val sumByType = dataTotal.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}