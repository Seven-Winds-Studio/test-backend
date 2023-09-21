package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorRecord
import mobi.sevenwinds.app.author.AuthorRecordToResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.deleteAllTables
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        deleteAllTables()
    }

    @Test
    fun testBudgetPagination() {
        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testBudgetPaginationWithNameParam() {
        val one = addAuthor(AuthorRecord("Ivanov Ivan Ivanovich"))
        val two = addAuthor(AuthorRecord("Ivanov Ivan Ivanovich"))
        val three = addAuthor(AuthorRecord("Petrov Petr Petrovich"))
        val four = addAuthor(AuthorRecord("Petrov Petr Petrovich"))

        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход, one.id))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход, two.id))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход, three.id))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход, four.id))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .queryParam("fio", "petr pet")
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(2, response.total)
                Assert.assertEquals(1, response.items.size)
                Assert.assertEquals(50, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testEmptyBudgetByName() {
        val one = addAuthor(AuthorRecord("Ivanov Ivan Ivanovich"))
        val two = addAuthor(AuthorRecord("Ivanov Ivan Ivanovich"))
        val three = addAuthor(AuthorRecord("Petrov Petr Petrovich"))
        val four = addAuthor(AuthorRecord("Petrov Petr Petrovich"))

        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход, one.id))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход, two.id))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход, three.id))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход, four.id))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("fio", "Strange Name")
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                Assert.assertEquals(0, response.items.size)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecordWithAuthor>().let { responce ->
                Assert.assertEquals(record.year, responce.year)
                Assert.assertEquals(record.month, responce.month)
                Assert.assertEquals(record.amount, responce.amount)
            }
    }

    private fun addAuthor(record: AuthorRecord): AuthorRecordToResponse {
        RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorRecordToResponse>().let { response ->
                Assert.assertEquals(record.fio, response.fio)
                Assert.assertNotNull(response.date)
                return response
            }
    }
}