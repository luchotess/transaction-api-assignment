package com.transaction_api_assignment.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres(embedded = true)
    val transactionService = TransactionService(dbConnection)

    routing {

        // Fetch all transactions
        get("/transactions") {
            val transactions = transactionService.getAll()
            call.respond(HttpStatusCode.OK, transactions)
        }

        // Fetch a single transaction by ID
        get("/transactions/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val transaction = transactionService.read(id)
                call.respond(HttpStatusCode.OK, transaction)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Create a new transaction
        post("/transactions") {
            val transaction = call.receive<Transaction>()
            val id = transactionService.create(transaction)
            call.respond(HttpStatusCode.Created, id)
        }

        // Update an existing transaction
        put("/transactions/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val updatedTransaction = call.receive<Transaction>()
            transactionService.update(id, updatedTransaction)
            call.respond(HttpStatusCode.OK)
        }

        // Delete a transaction
        delete("/transactions/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            transactionService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        // Fetch a summary of transactions
        get("/transactions/summary") {
            val summary = transactionService.getSummary()
            call.respond(HttpStatusCode.OK, summary)
        }
    }
}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}
