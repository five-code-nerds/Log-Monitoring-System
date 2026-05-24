# 📡 Multi-Client Centralized Log Monitoring & Alert System

![Banner](banner.svg)

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-5C2D91?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![XAMPP](https://img.shields.io/badge/XAMPP-Required-FB7A24?style=for-the-badge&logo=xampp&logoColor=white)

![Status](https://img.shields.io/badge/Status-Active-2ea44f?style=for-the-badge)
![Course](https://img.shields.io/badge/Course-Advanced%20Programming-6f42c1?style=for-the-badge)
![Year](https://img.shields.io/badge/Year-3rd%20Year%20SE-0075ca?style=for-the-badge)
![License](https://img.shields.io/badge/License-Academic-red?style=for-the-badge)

</div>

> A real-time distributed log monitoring system built with Java — featuring a JavaFX dashboard, MySQL persistence, multi-threaded server architecture, file processing, TCP networking, and a live web interface.

---

## 👥 Group Members

<div align="center">

| No | Name | Student ID | Role |
|---|------|-----------|------|
| 1 | Surafel Mesfin | ETS1322/16 | |
| 2 | Surafel Sintayehu | ETS1324/16 | |
| 3 | Temesgen Belay | ETS1337/16 | |


</div>

---

## 📚 Course Information

- **Course:** Advanced Programming
- **Project Title:** Multi-Client Centralized Log Monitoring and Alert System
- **Language:** Java 17
- **Build Tool:** Maven

---

## 🗺️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     MainApp (Entry Point)               │
│  ┌─────────────┐   ┌──────────────┐   ┌─────────────┐  │
│  │  LogServer  │   │ HttpApiServer│   │  JavaFX GUI │  │
│  │  Port 9090  │   │  Port 8080   │   │  Dashboard  │  │
│  └──────┬──────┘   └──────┬───────┘   └──────┬──────┘  │
│         │                 │                  │         │
│   ClientHandler       reads MySQL        reads MySQL   │
│   LogProcessor        serves HTML        auto-refresh  │
│   AlertEngine                            every 5s      │
└─────────────────────────────────────────────────────────┘
          ▲
          │ TCP Socket (port 9090)
┌─────────┴──────────┐
│     ClientApp      │
│   LogFileReader    │  ← tails a .log file
│   LogSender        │  → streams JSON to server
└────────────────────┘
```

---

## ✅ Features & Course Requirements Coverage

<div align="center">

![GUI](https://img.shields.io/badge/✔-GUI%20%7C%20JavaFX%20Dashboard-89b4fa?style=flat-square)
![DB](https://img.shields.io/badge/✔-Database%20%7C%20MySQL%20%2B%20JDBC-a6e3a1?style=flat-square)
![Threads](https://img.shields.io/badge/✔-Multi--Threading%20%7C%20Thread%20Pool%20%2B%20BlockingQueue-cba6f7?style=flat-square)
![Files](https://img.shields.io/badge/✔-File%20Processing%20%7C%20Log%20Tailing%20%2B%20CSV%20Export-f9e2af?style=flat-square)
![Network](https://img.shields.io/badge/✔-Network%20Programming%20%7C%20TCP%20Sockets-f38ba8?style=flat-square)
![Web](https://img.shields.io/badge/✔-Web%20Programming%20%7C%20Embedded%20HTTP%20Server-ff79c6?style=flat-square)

</div>

| Requirement | Implementation |
|---|---|
| **GUI** | JavaFX dashboard — live log table, alerts panel, client status, bar chart |
| **Database** | MySQL via XAMPP — 3 normalized tables with JDBC + DAO pattern |
| **Multi-threading** | Thread pool for clients, BlockingQueue, LogProcessor background thread |
| **File Processing** | LogFileReader tails `.log` files in real time + CSV export |
| **Network Programming** | TCP ServerSocket accepting multiple concurrent clients |
| **Web Programming** | Embedded Java HTTP server serving styled HTML pages on port 8080 |

---

## 🗂️ Project Structure

```
log-monitor-system/
├── logs/
│   └── sample.log               ← sample log file for testing
├── pom.xml                      ← Maven config & dependencies
└── src/main/
       └── java/com/app/
           ├── alert/
           │   ├── AlertEngine.java     ← evaluates rules, fires alerts
           │   └── AlertRule.java       ← single rule definition
           ├── client/
           │   ├── ClientApp.java       ← client entry point
           │   ├── LogFileReader.java   ← tails log file, detects new lines
           │   └── LogSender.java       ← sends logs over TCP socket
           ├── database/
           │   ├── DatabaseConnection.java ← Singleton JDBC connection
           │   ├── schema.sql              ← Database table
           ├── repository/
           │   ├── AlertRepository.java      ← DB operations for logs table
           │   └── ClientRepository.java     ← DB operations for alerts table
           |   └── LogRepository.java        ← DB operations for alerts table
           ├── server/
           │   ├── LogServer.java          ← TCP server, accepts connections
           │   ├── ClientHandler.java      ← one thread per client
           │   ├── LogProcessor.java       ← consumes queue, saves to DB
           │   └── LogEntry.java           ← data object for one log line
           └── web/
           │   └── HttpApiServer.java      ← Web page 
           └── Main.java 
```

---

## 🛠️ Prerequisites

![JDK](https://img.shields.io/badge/JDK-17+-ED8B00?style=flat-square&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat-square&logo=apachemaven)
![XAMPP](https://img.shields.io/badge/XAMPP-Latest-FB7A24?style=flat-square&logo=xampp)
![VSCode](https://img.shields.io/badge/VS%20Code-Latest-007ACC?style=flat-square&logo=visualstudiocode)
![Git](https://img.shields.io/badge/Git-Latest-F05032?style=flat-square&logo=git)

Make sure all of these are installed before running the project:

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17 or higher | https://adoptium.net |
| Maven | 3.6+ | https://maven.apache.org |
| XAMPP | Latest | https://www.apachefriends.org |
| VS Code | Latest | https://code.visualstudio.com |
| VS Code Java Extension Pack | Latest | Search in VS Code Extensions |
| Git | Latest | https://git-scm.com |

---

## 🚀 Setup & Run Guide (Step by Step)

### Step 1 — Clone the Repository

```bash
git@github.com:five-code-nerds/Log-Monitoring-System.git
cd Log-Monitoring-System 
```

---

### Step 2 — Start XAMPP MySQL

1. Open **XAMPP Control Panel**
2. Click **Start** next to **MySQL**
3. Wait until the status turns green

---

### Step 3 — Create the Database

1. Open your browser and go to `http://localhost/phpmyadmin`
2. Click the **SQL** tab
3. Paste and run the following SQL:

```sql
CREATE DATABASE IF NOT EXISTS log_monitor;
USE log_monitor;

CREATE TABLE IF NOT EXISTS clients (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    client_id     VARCHAR(100) NOT NULL UNIQUE,
    hostname      VARCHAR(150),
    ip_address    VARCHAR(50),
    status        ENUM('ONLINE', 'OFFLINE') DEFAULT 'OFFLINE',
    connected_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS logs (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    client_id    VARCHAR(100) NOT NULL,
    log_level    ENUM('INFO', 'WARN', 'ERROR', 'CRITICAL') DEFAULT 'INFO',
    message      TEXT NOT NULL,
    source_file  VARCHAR(255),
    logged_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_client FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE,
    INDEX idx_log_level (log_level),
    INDEX idx_logged_at (logged_at),
    INDEX idx_client_id (client_id)
);

CREATE TABLE IF NOT EXISTS alerts (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    client_id     VARCHAR(100) NOT NULL,
    log_id        INT,
    alert_level   ENUM('WARN', 'ERROR', 'CRITICAL') NOT NULL,
    message       TEXT NOT NULL,
    is_resolved   TINYINT(1) DEFAULT 0,
    triggered_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at   TIMESTAMP NULL,
    CONSTRAINT fk_alerts_client FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE,
    CONSTRAINT fk_alerts_log FOREIGN KEY (log_id) REFERENCES logs(id) ON DELETE SET NULL,
    INDEX idx_alert_level (alert_level),
    INDEX idx_is_resolved (is_resolved)
);
```

4. Click **Go** — you should see the 3 tables created successfully

---

### Step 4 — Open Project in VS Code

```bash
code .
```

Make sure VS Code shows no red errors in the Problems panel (`Ctrl+Shift+M`) before running.

---

### Step 5 — Run the Main Application

Open a terminal in VS Code (`Ctrl+`` `) and run:

```bash
mvn javafx:run
```

This starts:
- ✅ The JavaFX dashboard window
- ✅ The TCP log server on **port 9090**
- ✅ The web interface on **port 8080**

---

### Step 6 — Run a Client

Click the **`+`** button in the terminal panel to open a **second terminal**, then run:

```bash
mvn exec:java -Dexec.mainClass="com.logmonitor.client.ClientApp"
```

The client will connect to the server and start watching `logs/sample.log` for new lines.

> **To simulate multiple clients**, open more terminals and run with different IDs:
> ```bash
> mvn exec:java -Dexec.mainClass="com.logmonitor.client.ClientApp" -Dexec.args="CLIENT-02 logs/sample.log"
> ```

---

### Step 7 — Send Test Logs

Open a **third terminal** and append lines to the log file:

```bash
echo "ERROR: database connection failed" >> logs/sample.log
echo "CRITICAL: system disk is full" >> logs/sample.log
echo "WARN: memory usage at 85 percent" >> logs/sample.log
echo "INFO: backup completed successfully" >> logs/sample.log
```

Watch them appear live in the JavaFX dashboard!

---

### Step 8 — View the Web Interface

Open your browser and visit:

| URL | Description |
|---|---|
| `http://localhost:8080/` | Home page — system stats overview |
| `http://localhost:8080/logs` | Recent log entries (styled table) |
| `http://localhost:8080/alerts` | Active alerts |
| `http://localhost:8080/clients` | Connected clients and status |

> Pages auto-refresh every 10 seconds.

---

## 🧠 Key Concepts Used

| Concept | Where Used |
|---|---|
| **Singleton Pattern** | `DatabaseConnection` — one shared DB connection |
| **DAO Pattern** | `ClientRepository`, `LogRepository`, `AlertRepository` — isolate DB logic |
| **Producer-Consumer Pattern** | `ClientHandler` (producer) → `BlockingQueue` → `LogProcessor` (consumer) |
| **Thread Pool** | `ExecutorService` in `LogServer` — reuses threads for clients |
| **JDBC + PreparedStatement** | All DAO classes — safe parameterized SQL queries |
| **JavaFX MVC** | View + DAO data (Model) |
| **Server-Side Rendering** | `HttpApiServer` builds HTML from DB data and serves it |
| **File Tailing** | `LogFileReader` uses `RandomAccessFile` to detect new lines |
| **Socket Programming** | `ServerSocket` / `Socket` for client-server TCP communication |

---

## 🌐 Log Line Format

The client supports two formats in `.log` files:

```
ERROR: your message here
[CRITICAL] your message here
WARN: memory is running low
INFO: service started
plain text with no prefix (defaults to INFO)
```

---

## 📦 Dependencies

| Library | Purpose |
|---|---|
| `javafx-controls` | UI components (TableView, Charts, etc.) |
| `javafx-fxml` | FXML layout loading |
| `mysql-connector-java` | JDBC driver for MySQL |
| `gson` | JSON serialization for socket protocol |

All dependencies are managed automatically by Maven — no manual `.jar` downloads needed.

## 📄 License

![License](https://img.shields.io/badge/License-Academic%20Use%20Only-red?style=for-the-badge)

This project was developed as a university course assignment for Advanced Programming.

---

<div align="center">

Made with ☕ Java &nbsp;|&nbsp; Advanced Programming Course &nbsp;|&nbsp; 3rd Year Software Engineering

![Java](https://img.shields.io/badge/Built%20with-Java-ED8B00?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/UI-JavaFX-5C2D91?style=flat-square)
![MySQL](https://img.shields.io/badge/DB-MySQL-4479A1?style=flat-square&logo=mysql)

</div>