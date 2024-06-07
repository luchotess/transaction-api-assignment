package com.transaction_api_assignment.plugins

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.javatime.date
import java.math.BigDecimal
import java.time.LocalDate

object Transactions : Table() {
    val id = integer("id").autoIncrement()
    val amount = decimal("amount", 10, 2)
    val date = date("date")
    val category = varchar("category", 255)
    val description = text("description").nullable()
    val type = varchar("type", 50)
}

@Serializable
data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val date: String,
    val category: String,
    val description: String?,
    val type: String
)

@Serializable
data class TransactionSummary(
    val category: String,
    val total_amount: Double?,
    val total_transactions: Long
)

class TransactionService(database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Transactions)
        }
    }

    suspend fun create(transaction: Transaction): Int = newSuspendedTransaction(Dispatchers.IO) {
        val result = Transactions.insert {
            it[amount] = BigDecimal.valueOf(transaction.amount)
            it[date] = LocalDate.parse(transaction.date)
            it[category] = transaction.category
            it[description] = transaction.description
            it[type] = transaction.type
        }

        result[Transactions.id]
    }

    suspend fun read(id: Int): Transaction = newSuspendedTransaction(Dispatchers.IO) {
        Transactions.select { Transactions.id eq id }
            .map {
                Transaction(
                    id = it[Transactions.id],
                    amount = it[Transactions.amount].toDouble(),
                    date = it[Transactions.date].toString(), // Convierte Date a String
                    category = it[Transactions.category],
                    description = it[Transactions.description],
                    type = it[Transactions.type]
                )
            }.singleOrNull() ?: throw Exception("Transaction not found")
    }

    suspend fun update(id: Int, transaction: Transaction) = newSuspendedTransaction(Dispatchers.IO) {
        Transactions.update({ Transactions.id eq id }) {
            it[amount] = BigDecimal.valueOf(transaction.amount)
            it[date] =  LocalDate.parse(transaction.date)
            it[category] = transaction.category
            it[description] = transaction.description
            it[type] = transaction.type
        }
    }

    suspend fun delete(id: Int) = newSuspendedTransaction(Dispatchers.IO) {
        Transactions.deleteWhere { Transactions.id eq id }
    }

    suspend fun getAll(): List<Transaction> = newSuspendedTransaction(Dispatchers.IO) {
        Transactions.selectAll().map {
            Transaction(
                id = it[Transactions.id],
                amount = it[Transactions.amount].toDouble(),
                date = it[Transactions.date].toString(), // Convierte Date a String
                category = it[Transactions.category],
                description = it[Transactions.description],
                type = it[Transactions.type]
            )
        }
    }

    suspend fun getSummary(): List<TransactionSummary> = newSuspendedTransaction(Dispatchers.IO) {
        Transactions
            .slice(Transactions.category, Transactions.amount.sum(), Transactions.id.count())
            .selectAll()
            .groupBy(Transactions.category)
            .map {
                TransactionSummary(
                    category = it[Transactions.category],
                    total_amount = it[Transactions.amount.sum()]?.toDouble(),
                    total_transactions = it[Transactions.id.count()]
                )
            }
    }
}
