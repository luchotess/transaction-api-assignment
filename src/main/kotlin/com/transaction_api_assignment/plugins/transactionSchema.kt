package com.transaction_api_assignment.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.sql.Date
import java.math.BigDecimal

@Serializable
data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val date: String,
    val category: String,
    val description: String?,
    val type: String
)

class TransactionService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_TRANSACTIONS =
            """
            CREATE TABLE TRANSACTIONS (
                ID SERIAL PRIMARY KEY,
                AMOUNT NUMERIC(10, 2) NOT NULL,
                DATE DATE NOT NULL,
                CATEGORY VARCHAR(255) NOT NULL,
                DESCRIPTION TEXT,
                TYPE VARCHAR(50) NOT NULL
            );
            """
        private const val SELECT_TRANSACTION_BY_ID = "SELECT * FROM transactions WHERE id = ?"
        private const val INSERT_TRANSACTION = "INSERT INTO transactions (amount, date, category, description, type) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_TRANSACTION = "UPDATE transactions SET amount = ?, date = ?, category = ?, description = ?, type = ? WHERE id = ?"
        private const val DELETE_TRANSACTION = "DELETE FROM transactions WHERE id = ?"
        private const val SELECT_ALL_TRANSACTIONS = "SELECT * FROM transactions"
        private const val SELECT_TRANSACTION_SUMMARY = """
            SELECT 
                SUM(amount) as total_amount,
                COUNT(*) as total_transactions,
                category, 
                COUNT(category) as count_per_category 
            FROM transactions 
            GROUP BY category
        """
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_TRANSACTIONS)
    }

    // Create a new transaction
    suspend fun create(transaction: Transaction): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_TRANSACTION, Statement.RETURN_GENERATED_KEYS)
        statement.setDouble(1, transaction.amount)
        statement.setDate(2, Date.valueOf(transaction.date))
        statement.setString(3, transaction.category)
        statement.setString(4, transaction.description)
        statement.setString(5, transaction.type)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted transaction")
        }
    }

    // Read a transaction by ID
    suspend fun read(id: Int): Transaction = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TRANSACTION_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val amount = resultSet.getDouble("amount")
            val date = resultSet.getDate("date").toString()
            val category = resultSet.getString("category")
            val description = resultSet.getString("description")
            val type = resultSet.getString("type")
            return@withContext Transaction(id, amount, date, category, description, type)
        } else {
            throw Exception("Transaction not found")
        }
    }

    // Update a transaction
    suspend fun update(id: Int, transaction: Transaction) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_TRANSACTION)
        statement.setDouble(1, transaction.amount)
        statement.setDate(2, Date.valueOf(transaction.date))
        statement.setString(3, transaction.category)
        statement.setString(4, transaction.description)
        statement.setString(5, transaction.type)
        statement.setInt(6, id)
        statement.executeUpdate()
    }

    // Delete a transaction
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_TRANSACTION)
        statement.setInt(1, id)
        statement.executeUpdate()
    }

    // Fetch all transactions
    suspend fun getAll(): List<Transaction> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_TRANSACTIONS)
        val resultSet = statement.executeQuery()
        val transactions = mutableListOf<Transaction>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val amount = resultSet.getDouble("amount")
            val date = resultSet.getDate("date").toString()
            val category = resultSet.getString("category")
            val description = resultSet.getString("description")
            val type = resultSet.getString("type")
            transactions.add(Transaction(id, amount, date, category, description, type))
        }
        transactions
    }

    // Fetch transaction summary
    suspend fun getSummary(): Map<String, Any> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TRANSACTION_SUMMARY)
        val resultSet = statement.executeQuery()
        val summary = mutableMapOf<String, Any>()

        var totalAmount = BigDecimal.ZERO
        val transactionsPerCategory = mutableMapOf<String, Int>()

        while (resultSet.next()) {
            totalAmount = resultSet.getBigDecimal("total_amount")
            val category = resultSet.getString("category")
            val countPerCategory = resultSet.getInt("count_per_category")
            transactionsPerCategory[category] = countPerCategory
        }

        summary["totalAmount"] = totalAmount
        summary["transactionsPerCategory"] = transactionsPerCategory
        summary
    }
}
