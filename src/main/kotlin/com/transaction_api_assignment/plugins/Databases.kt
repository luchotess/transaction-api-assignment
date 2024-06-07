package com.transaction_api_assignment.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val dbConnection: Database = connectToPostgres(embedded = true)
    val transactionService = TransactionService(dbConnection)

    routing {

        get("/transactions") {
            val transactions = transactionService.getAll()
            call.respond(HttpStatusCode.OK, transactions)
        }

        get("/transactions/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val transaction = transactionService.read(id)
                call.respond(HttpStatusCode.OK, transaction)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/transactions") {
            val transaction = call.receive<Transaction>()
            val id = transactionService.create(transaction)
            call.respond(HttpStatusCode.Created, id)
        }

        put("/transactions/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val updatedTransaction = call.receive<Transaction>()
            transactionService.update(id, updatedTransaction)
            call.respond(HttpStatusCode.OK)
        }

        delete("/transactions/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            transactionService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        get("/transactions/summary") {
            val summary = transactionService.getSummary()
            call.respond(HttpStatusCode.OK, summary)
        }
    }
}

fun Application.connectToPostgres(embedded: Boolean): Database {
    Class.forName("org.h2.Driver")
    if (embedded) {
        return Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.postgresql.Driver","root", "")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return Database.connect(url, driver = "org.postgresql.Driver", user = user, password = password)
    }
}
