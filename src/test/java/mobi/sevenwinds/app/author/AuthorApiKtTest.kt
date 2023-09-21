package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.app.budget.BudgetRecord
import mobi.sevenwinds.app.budget.BudgetTable
import mobi.sevenwinds.app.budget.BudgetType
import mobi.sevenwinds.app.budget.BudgetYearStatsResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.deleteAllTables
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        deleteAllTables()
    }

    @Test
    fun testBudgetPagination() {
        val authorRecord = AuthorRecord("Ivanov Ivan Ivanovich")
        RestAssured.given()
            .jsonBody(authorRecord)
            .post("/author/add")
            .toResponse<AuthorRecordToResponse>().let { response ->
                println("${response.id} / ${response.fio} / ${response.date}")

                Assert.assertNotNull(response.id)
                Assert.assertEquals(authorRecord.fio, response.fio)
                Assert.assertNotNull(response.date)
            }
    }
    @Test
    fun testInvalidFioAuthors() {
        RestAssured.given()
            .jsonBody(AuthorRecord(""))
            .post("/author/add")
            .then().statusCode(400)
    }

}