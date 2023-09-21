package mobi.sevenwinds.utils

import org.jetbrains.exposed.sql.*

class ILikeOp(left: Expression<*>, right: Expression<*>) : ComparisonOp(left, right, "ILIKE")

infix fun<T : String?> ExpressionWithColumnType<T>.ilike(
    pattern: String
): Op<Boolean> = ILikeOp(this, QueryParameter(pattern, columnType))
