package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.app.budget.BudgetTable
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AuthorServiceTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testAddAuthorRecord() {
        addRecord(AuthorRecord("Vorobev Oleg Nikolaevich"))
    }

    private fun addRecord(record: AuthorRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorRecord>().let { response ->
                println(response)
            }
    }
}