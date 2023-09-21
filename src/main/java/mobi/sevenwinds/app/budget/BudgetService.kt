package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.utils.CountAll
import mobi.sevenwinds.utils.ilike
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecordToResponse = withContext(Dispatchers.IO) {
        transaction {
            val author = if (body.authorId != null) {
                AuthorEntity.findById(body.authorId)
            } else {
                null
            }

            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = author
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val countExp = CountAll()
            val sumExp = Sum(BudgetTable.amount, BudgetTable.amount.columnType)

            val totalByType = HashMap<String, Int>()
            var count = 0

            // Counting total
            BudgetTable
                .run {
                    if (param.fio != null) {
                        joinAuthorsToBudget()
                            .slice(type, sumExp, countExp)
                            .selectBudget(param.year, param.fio)
                    } else {
                        slice(type, sumExp, countExp)
                            .selectBudget(param.year)
                    }
                }
                .groupBy(BudgetTable.type)
                .map { row ->
                    val type = row[BudgetTable.type]
                    val amount = row[sumExp] ?: 0

                    count += row[countExp]
                    totalByType[type.name] = amount
                }

            // Extract paging
            val queryItems = BudgetTable
                .run {
                    if (param.fio != null) {
                        joinAuthorsToBudget()
                            .selectBudget(param.year, param.fio)
                    } else {
                        selectBudget(param.year)
                    }
                }
                .limit(param.limit, param.offset)
                .orderBy(
                    BudgetTable.month to SortOrder.ASC,
                    BudgetTable.amount to SortOrder.DESC,
                )

            val items = BudgetEntity.wrapRows(queryItems)
                .map { it.toResponseWithAuthor() }

            return@transaction BudgetYearStatsResponse(
                total = count,
                totalByType = totalByType,
                items = items,
            )
        }
    }

    private fun Table.joinAuthorsToBudget(): Join {
        return join(AuthorTable, JoinType.LEFT, onColumn = BudgetTable.author, otherColumn = AuthorTable.id)
    }

    private fun FieldSet.selectBudget(year: Int, fio: String? = null): Query {
        return if (fio == null) {
            select { BudgetTable.year eq year }
        } else {
            select { (BudgetTable.year eq year) and (AuthorTable.fio ilike "%$fio%") }
        }
    }
}