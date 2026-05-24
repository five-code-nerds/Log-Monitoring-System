CREATE DATABASE log_monitor;
USE log_monitor;

CREATE TABLE clients (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    client_id     VARCHAR(100) NOT NULL UNIQUE,
    hostname      VARCHAR(150),
    ip_address    VARCHAR(50),
    status        ENUM('ONLINE', 'OFFLINE') DEFAULT 'OFFLINE',
    connected_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE logs (
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

CREATE TABLE alerts (
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