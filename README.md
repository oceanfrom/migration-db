# Database Migration Tool

This project provides a tool for handling database migrations, supporting both applying and rolling back migrations, as well as tracking migration statuses.

## Features

- Apply migrations to the database.
- Rollback migrations to a specific version or a number of previous migrations.
- Check the status of applied migrations.
- Generate reports in JSON format.

## Technologies Used

- **Java**: Core language
- **JDBC**: For interacting with the database
- **PostgreSQL**: (Assumed based on `pg_advisory_lock` SQL query)
- **Lombok**: For boilerplate code reduction
- **Jackson**: For generating migration reports in JSON
- **Log4j / Custom Logger**: For logging migration activities

## How it Works

### Migration Process

- **MigrationService** is responsible for applying the migration files to the database.
- **MigrationRollbackService** handles the rollback of migrations.
- **MigrationManager** manages the migration process, including creating tables, acquiring locks, and marking migrations as applied or rolled back.
- **MigrationFileReader** loads migration SQL files from the `src/main/resources/db.changelog/versions/` directory.

### Locking Mechanism

The tool uses a **MigrationLockManager** to acquire a database lock (`pg_advisory_lock`) during migration and rollback operations, ensuring that only one migration or rollback operation can run at a time.

### Report Generation

The **MigrationReport** class generates a JSON report after each migration, recording whether each migration was applied or failed.


## Setup

### Prerequisites

- Java 11 or later
- A database with PostgreSQL or a compatible SQL dialect.
- `application.properties` configuration file for database connection.

### Configuration

1. Clone the repository.
2. Set up the database connection in the `src/main/resources/application.properties` file. Example:

    ```properties
    datasource.url=jdbc:postgresql://localhost:5432/mydatabase
    datasource.username=myuser
    datasource.password=mypassword
    ```

3. Ensure the SQL migration scripts are placed in `src/main/resources/db.changelog/versions/0001-insert.sql`.
4. Ensure the SQL rollback migration scripts are placed in `src/main/resources/db.changelog/rollback/0001-insert-rollback.sql`.


### Build and Run

For Maven:
```bash
mvn clean install
Run the command to apply all migrations:
java -cp target/migration-db-1.0-SNAPSHOT.jar org.example.MigrationTool migrate
To rollback a migration to a specific version:
java -cp target/migration-db-1.0-SNAPSHOT.jar org.example.MigrationTool rollback-version "0002-insert.sql"
To rollback the last n migrations:
java -cp target/migration-db-1.0-SNAPSHOT.jar org.example.MigrationTool rollback 2
To check the status of the migrations:
java -cp target/migration-db-1.0-SNAPSHOT.jar org.example.MigrationTool status