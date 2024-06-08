# Transactions Dashboard Assignment API 

## Overview

Welcome to the **Transactions Dashboard Assignment API** repository. This project is a backend server built using Kotlin and Ktor, designed to provide robust and scalable API services. It leverages Exposed ORM for database interactions and uses an embedded PostgreSQL database for development. The project is deployed on Render.com for testing purposes.

Demo: https://transaction-api-assignment.onrender.com

1. **Endpoints**:
    - `GET /transactions`: Fetch all transactions.
    - `GET /transactions/{id}`: Fetch a single transaction by ID.
    - `POST /transactions`: Create a new transaction.
    - `PUT /transactions/{id}`: Update an existing transaction.
    - `DELETE /transactions/{id}`: Delete a transaction.
    - `GET /transactions/summary`: Fetch a summary of transactions (e.g., total amount, number

### Key Technologies:
- **Kotlin**: A modern, concise, and safe programming language for JVM.
- **Ktor**: A Kotlin framework for building asynchronous servers and web applications.
- **Exposed**: A lightweight SQL library for Kotlin, used as an ORM.
- **PostgreSQL**: An open-source relational database system.
- **Render.com**: A cloud platform for deploying and scaling applications.

## Getting Started

Follow these instructions to set up and run the project on your local machine.

### Prerequisites

Ensure you have the following software installed:
- **JDK 11** or higher: Required for running Kotlin applications.
- **Docker**: To run the Project Locally

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/backend-project.git
   cd backend-project
   Build and Run
