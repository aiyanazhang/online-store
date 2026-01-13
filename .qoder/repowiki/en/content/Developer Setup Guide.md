# Developer Setup Guide

<cite>
**Referenced Files in This Document**   
- [pom.xml](file://pom.xml)
- [application.yml](file://src/main/resources/application.yml)
- [application-local.yml](file://src/main/resources/application-local.yml)
- [bootstrap.yml](file://src/main/resources/bootstrap.yml)
- [schema.sql](file://src/main/resources/db/schema.sql)
- [OnlineStoreApplication.java](file://src/main/java/com/example/onlinestore/OnlineStoreApplication.java)
- [LocalConfigProperties.java](file://src/main/java/com/example/onlinestore/config/LocalConfigProperties.java)
- [RedisConfig.java](file://src/main/java/com/example/onlinestore/config/RedisConfig.java)
- [MyBatisConfig.java](file://src/main/java/com/example/onlinestore/config/MyBatisConfig.java)
- [AuthController.java](file://src/main/java/com/example/onlinestore/controller/AuthController.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [JDK 17 Installation](#jdk-17-installation)
4. [MySQL Database Setup](#mysql-database-setup)
5. [Redis Server Configuration](#redis-server-configuration)
6. [Application Configuration](#application-configuration)
7. [Maven Build and Execution](#maven-build-and-execution)
8. [IDE Setup](#ide-setup)
9. [Verification and Testing](#verification-and-testing)
10. [Troubleshooting Common Issues](#troubleshooting-common-issues)
11. [Conclusion](#conclusion)

## Introduction
This guide provides comprehensive instructions for setting up a development environment for the online-store application. The application is a Spring Boot-based e-commerce platform that requires JDK 17, MySQL 8.0, Redis, and Maven for successful operation. The setup process includes installing required software, configuring database and caching services, setting up application properties, and verifying the installation through API testing. This beginner-friendly guide ensures developers can quickly establish a working development environment with clear steps and troubleshooting advice.

**Section sources**
- [README.md](file://README.md#L1-L55)

## Prerequisites
Before beginning the setup process, ensure your system meets the following requirements:
- Operating System: Windows 10/11, macOS, or Linux distribution
- Minimum 4GB RAM (8GB recommended)
- At least 500MB of free disk space
- Internet connection for downloading dependencies
- Administrative privileges for installing software

The application has specific version requirements for its core components:
- JDK 17 or higher (specified in pom.xml)
- Maven 3.6 or higher
- MySQL 8.0
- Redis 6.0 or higher

These requirements are enforced through the project's configuration files, particularly the pom.xml which specifies Java 17 as the target version and includes dependencies compatible with the specified Spring Boot and Spring Cloud versions.

**Section sources**
- [pom.xml](file://pom.xml#L11-L21)
- [README.md](file://README.md#L37-L42)

## JDK 17 Installation
To install JDK 17, follow these platform-specific instructions:

### For Windows:
1. Download JDK 17 from the official Oracle website or Adoptium
2. Run the installer and follow the installation wizard
3. Set environment variables:
   - JAVA_HOME: Path to JDK installation directory
   - Add %JAVA_HOME%\bin to PATH variable

### For macOS:
1. Use Homebrew: `brew install openjdk@17`
2. Or download from Oracle/Adoptium and install the package
3. Configure shell profile (~/.zshrc or ~/.bash_profile):
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   export PATH=$JAVA_HOME/bin:$PATH
   ```

### For Linux:
1. Use package manager:
   - Ubuntu/Debian: `sudo apt install openjdk-17-jdk`
   - CentOS/RHEL: `sudo yum install java-17-openjdk-devel`
2. Set JAVA_HOME in ~/.bashrc:
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   export PATH=$JAVA_HOME/bin:$PATH
   ```

Verify installation by running `java -version` and `javac -version` in the terminal. Both should display version information indicating Java 17.

**Section sources**
- [pom.xml](file://pom.xml#L12)
- [README.md](file://README.md#L7)

## MySQL Database Setup
The online-store application requires a MySQL database with specific configuration:

### Installation:
1. Download MySQL 8.0 Community Server from the official website
2. Run the installer and choose "Developer Default" configuration
3. During setup, note the root password you set

### Database Creation:
After MySQL is installed and running, create the required database:

1. Connect to MySQL as root:
   ```bash
   mysql -u root -p
   ```
2. Execute the database creation command:
   ```sql
   CREATE DATABASE online_store DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

### Schema Initialization:
The application includes a schema file that will be automatically applied on startup:
```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    token VARCHAR(100),
    token_expire_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

This schema defines the users table with fields for authentication and tracking. The application uses MyBatis for database operations, which is configured to map underscore notation in SQL to camel case in Java objects.

**Section sources**
- [schema.sql](file://src/main/resources/db/schema.sql#L1-L8)
- [application.yml](file://src/main/resources/application.yml#L17-L21)
- [MyBatisConfig.java](file://src/main/java/com/example/onlinestore/config/MyBatisConfig.java#L13-L27)

## Redis Server Configuration
Redis is used as the caching layer for the application and must be properly configured:

### Installation:
1. **Windows**: Download Redis from GitHub or use Windows Subsystem for Linux (WSL)
2. **macOS**: `brew install redis`
3. **Linux**: 
   - Ubuntu/Debian: `sudo apt install redis-server`
   - CentOS/RHEL: `sudo yum install redis`

### Starting Redis:
- **macOS/Linux**: `redis-server` or `sudo systemctl start redis`
- **Windows**: Run redis-server.exe from the installation directory

### Verification:
Test Redis connectivity:
```bash
redis-cli ping
```
Should return "PONG" if Redis is running correctly.

The application is configured to connect to Redis on localhost:6379 with default settings. The Redis configuration is handled by Spring Data Redis with Jedis as the client library, as specified in the pom.xml dependencies.

**Section sources**
- [pom.xml](file://pom.xml#L125-L135)
- [application.yml](file://src/main/resources/application.yml#L22-L34)
- [RedisConfig.java](file://src/main/java/com/example/onlinestore/config/RedisConfig.java#L1-L15)

## Application Configuration
Proper configuration is essential for the application to connect to database and caching services:

### Profile Configuration:
The application uses Spring profiles with 'local' as the default. Configuration is split across:
- **application.yml**: Base configuration with profile activation
- **application-local.yml**: Local development settings
- **bootstrap.yml**: Nacos configuration (disabled by default)

### Database Connection:
Edit `src/main/resources/application-local.yml` to configure MySQL:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_store?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
```

### Redis Connection:
In the same file, configure Redis if using non-default settings:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
```

### Configuration Loading:
The `LocalConfigProperties` class ensures that when Nacos is disabled (default for local development), the application-local.yml file is loaded. This allows developers to override settings without affecting other environments.

**Section sources**
- [application-local.yml](file://src/main/resources/application-local.yml#L1-L33)
- [bootstrap.yml](file://src/main/resources/bootstrap.yml#L1-L17)
- [LocalConfigProperties.java](file://src/main/java/com/example/onlinestore/config/LocalConfigProperties.java#L1-L12)

## Maven Build and Execution
The application uses Maven as its build tool with specific configurations:

### Build Commands:
1. Clean the project:
   ```bash
   mvn clean
   ```
2. Compile and package:
   ```bash
   mvn compile
   ```
3. Build the JAR file:
   ```bash
   mvn package
   ```

### Running the Application:
Execute the application using:
```bash
mvn spring-boot:run
```

This command will:
- Resolve all dependencies from pom.xml
- Compile the source code
- Start the Spring Boot application on port 8080
- Initialize database connections
- Set up Redis connectivity
- Register controllers and services

The pom.xml specifies Spring Boot 3.1.5 and Spring Cloud 2022.0.4 as parent dependencies, ensuring compatibility across the ecosystem. The maven-compiler-plugin is configured to use Java 17 for both source and target compatibility.

**Section sources**
- [pom.xml](file://pom.xml#L151-L168)
- [OnlineStoreApplication.java](file://src/main/java/com/example/onlinestore/OnlineStoreApplication.java#L1-L15)

## IDE Setup
The application can be configured in popular Java IDEs:

### IntelliJ IDEA:
1. Open the project directory
2. IntelliJ will detect it as a Maven project
3. Wait for Maven import to complete
4. Configure JDK 17:
   - File → Project Structure → Project Settings → Project
   - Set Project SDK to JDK 17
   - Set Project language level to 17
5. Run configuration:
   - Create new Spring Boot run configuration
   - Main class: com.example.onlinestore.OnlineStoreApplication
   - Profiles: local

### Eclipse:
1. Import as Maven project
2. Right-click project → Properties → Java Build Path
3. Add JDK 17 to the build path
4. Project → Properties → Java Compiler
5. Set compiler compliance level to 17
6. Create run configuration:
   - Main class: com.example.onlinestore.OnlineStoreApplication
   - Program arguments: --spring.profiles.active=local

Both IDEs will recognize the Spring Boot structure and provide appropriate code assistance, debugging capabilities, and run configurations.

**Section sources**
- [pom.xml](file://pom.xml#L12-L13)
- [OnlineStoreApplication.java](file://src/main/java/com/example/onlinestore/OnlineStoreApplication.java#L8-L10)

## Verification and Testing
After setup, verify the application is working correctly:

### Startup Verification:
When running `mvn spring-boot:run`, look for these success indicators:
- "Started OnlineStoreApplication in X seconds"
- No error messages related to database or Redis connections
- Tomcat started on port 8080

### API Testing:
Use curl or Postman to test endpoints:

1. Check application health:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Test authentication endpoint:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
   -H "Content-Type: application/json" \
   -d '{"username":"admin","password":"password"}'
   ```

3. List users (requires admin authentication):
   ```bash
   curl http://localhost:8080/api/users
   ```

The AuthController handles login requests and returns appropriate responses. Successful setup will return HTTP 200 responses for health checks and appropriate error messages for invalid requests.

**Section sources**
- [AuthController.java](file://src/main/java/com/example/onlinestore/controller/AuthController.java#L1-L45)
- [application.yml](file://src/main/resources/application.yml#L1-L2)

## Troubleshooting Common Issues
Address common setup problems with these solutions:

### Port Conflicts:
- **MySQL**: Default port 3306 may conflict with existing installations
  - Solution: Stop conflicting service or change port in MySQL configuration
- **Redis**: Default port 6379 may be in use
  - Solution: `lsof -i :6379` (macOS/Linux) to identify process
- **Application**: Port 8080 may be occupied
  - Solution: Change server.port in application.yml or stop conflicting service

### Dependency Resolution:
- **Maven issues**: Clear local repository cache
  ```bash
  rm -rf ~/.m2/repository
  mvn clean compile
  ```
- **Missing dependencies**: Ensure internet connectivity and correct Maven settings

### Configuration Errors:
- **Database connection**: Verify MySQL is running and credentials are correct
- **Redis connection**: Ensure Redis server is started before application
- **Profile activation**: Confirm SPRING_PROFILES_ACTIVE environment variable

### Common Error Messages:
- "Access denied for user": Check MySQL username/password
- "Connection refused": Verify database/Redis services are running
- "ClassNotFoundException": Ensure JDK 17 is properly installed and configured

The application's logging configuration will help identify issues, with detailed logs available in the console output.

**Section sources**
- [application.yml](file://src/main/resources/application.yml#L1-L2)
- [AuthController.java](file://src/main/java/com/example/onlinestore/controller/AuthController.java#L34-L43)
- [README.md](file://README.md#L46-L55)

## Conclusion
This guide has provided comprehensive instructions for setting up the online-store application development environment. By following these steps, developers can successfully install JDK 17, configure MySQL and Redis, set up application properties, and run the application using Maven. The configuration system supports local development with easy overrides through profile-specific files. With the troubleshooting tips provided, common issues can be quickly resolved. The application is now ready for development, testing, and contribution. Future enhancements could include Docker-based setup for more consistent environments across development teams.