package com.app.web;

import com.app.repository.AlertRepository;
import com.app.repository.ClientRepository;
import com.app.repository.LogRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * HttpApiServer — serves styled HTML pages from Java.
 *
 * CONCEPT: Server-Side Rendering (SSR)
 * The server fetches data from the database, builds a complete
 * HTML page as a String, and sends it to the browser.
 * The browser just displays what it receives — no React, no Vue,
 * no framework. This is how the web originally worked and is
 * still valid for many use cases today.
 */
public class HttpApiServer {

    private static final int PORT = 3500;

    private HttpServer  server;
    private final LogRepository logRepository  = new LogRepository();
    private final AlertRepository alertRepository = new AlertRepository();
    private final ClientRepository clientRepository = new ClientRepository();

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/",        new RootHandler());
            server.createContext("/logs",    new LogsHandler());
            server.createContext("/alerts",  new AlertsHandler());
            server.createContext("/clients", new ClientsHandler());

            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();

            System.out.println("[WebAPI] Started at http://localhost:" + PORT);

        } catch (IOException e) {
            System.err.println("[WebAPI] Failed to start: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ══════════════════════════════════════════════
    // SHARED HTML HELPERS
    // ══════════════════════════════════════════════

    /**
     * htmlShell() — wraps any page content in a full HTML document.
     * Every page shares the same CSS styling and navigation bar.
     *
     * CONCEPT: String.format()
     * %s is a placeholder — replaced by the corresponding argument.
     * This lets us build HTML templates cleanly without concatenation.
     */
    private String htmlShell(String title, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s — Log Monitor</title>
                <!-- Auto-refresh every 10 seconds -->
                <meta http-equiv="refresh" content="10">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }

                    body {
                        font-family: 'Segoe UI', sans-serif;
                        background: #1e1e2e;
                        color: #cdd6f4;
                        min-height: 100vh;
                    }

                    /* ── Navigation Bar ── */
                    nav {
                        background: #313244;
                        padding: 14px 30px;
                        display: flex;
                        align-items: center;
                        gap: 30px;
                        border-bottom: 2px solid #45475a;
                        position: sticky;
                        top: 0;
                        z-index: 100;
                    }
                    nav .brand {
                        font-size: 18px;
                        font-weight: bold;
                        color: #89b4fa;
                        text-decoration: none;
                        margin-right: auto;
                    }
                    nav a {
                        color: #a6adc8;
                        text-decoration: none;
                        font-size: 14px;
                        padding: 6px 14px;
                        border-radius: 6px;
                        transition: background 0.2s;
                    }
                    nav a:hover, nav a.active {
                        background: #45475a;
                        color: #cdd6f4;
                    }

                    /* ── Page Container ── */
                    .container {
                        max-width: 1100px;
                        margin: 30px auto;
                        padding: 0 20px;
                    }

                    h1 {
                        font-size: 22px;
                        margin-bottom: 20px;
                        color: #cdd6f4;
                        border-left: 4px solid #89b4fa;
                        padding-left: 12px;
                    }

                    /* ── Stats Cards ── */
                    .stats {
                        display: flex;
                        gap: 16px;
                        margin-bottom: 24px;
                        flex-wrap: wrap;
                    }
                    .card {
                        background: #313244;
                        border-radius: 10px;
                        padding: 16px 24px;
                        min-width: 150px;
                        text-align: center;
                    }
                    .card .num {
                        font-size: 32px;
                        font-weight: bold;
                        color: #89b4fa;
                    }
                    .card .label {
                        font-size: 12px;
                        color: #a6adc8;
                        margin-top: 4px;
                    }

                    /* ── Table ── */
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        background: #313244;
                        border-radius: 10px;
                        overflow: hidden;
                        font-size: 13px;
                    }
                    thead {
                        background: #45475a;
                    }
                    th {
                        padding: 12px 14px;
                        text-align: left;
                        font-weight: 600;
                        color: #cdd6f4;
                        letter-spacing: 0.5px;
                        font-size: 12px;
                        text-transform: uppercase;
                    }
                    td {
                        padding: 10px 14px;
                        border-bottom: 1px solid #45475a;
                        color: #cdd6f4;
                    }
                    tr:last-child td { border-bottom: none; }
                    tr:hover td { background: #3c3f52; }

                    /* ── Level Badges ── */
                    .badge {
                        display: inline-block;
                        padding: 3px 10px;
                        border-radius: 20px;
                        font-size: 11px;
                        font-weight: bold;
                        letter-spacing: 0.5px;
                    }
                    .INFO     { background: #1e3a5f; color: #89b4fa; }
                    .WARN     { background: #3d2e00; color: #f9e2af; }
                    .ERROR    { background: #3c0000; color: #f38ba8; }
                    .CRITICAL { background: #45001e; color: #ff79c6; }
                    .ONLINE   { background: #1a3a1a; color: #a6e3a1; }
                    .OFFLINE  { background: #3a1a1a; color: #f38ba8; }

                    /* ── Footer ── */
                    .footer {
                        text-align: center;
                        color: #585b70;
                        font-size: 12px;
                        margin-top: 30px;
                        padding: 20px;
                    }

                    /* ── Empty State ── */
                    .empty {
                        text-align: center;
                        padding: 40px;
                        color: #585b70;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <nav>
                    <a class="brand" href="/">📡 Log Monitor</a>
                    <a href="/">Home</a>
                    <a href="/logs">Logs</a>
                    <a href="/alerts">Alerts</a>
                    <a href="/clients">Clients</a>
                </nav>
                <div class="container">
                    %s
                </div>
                <div class="footer">
                    Auto-refreshes every 10 seconds &nbsp;|&nbsp;
                    Log Monitor System &nbsp;|&nbsp;
                    Port 8080
                </div>
            </body>
            </html>
            """, title, content);
    }

    /**
     * badge() — generates a colored HTML span for a level value.
     * The CSS class matches the level name (INFO, WARN, ERROR etc.)
     */
    private String badge(String value) {
        return String.format(
                "<span class=\"badge %s\">%s</span>", value, value
        );
    }

    /**
     * sendHtml() — writes the HTML response to the browser.
     */
    private void sendHtml(HttpExchange ex, int status, String html) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = html.getBytes();
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ══════════════════════════════════════════════
    // HANDLERS
    // ══════════════════════════════════════════════

    /** GET / — Home page with summary stats */
    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            List<String[]> logs    = logRepository.getRecentLogs(1000);
            List<String[]> alerts  = alertRepository.getActiveAlerts();
            List<String[]> clients = clientRepository.getAllClients();

            long online = clients.stream()
                    .filter(c -> "ONLINE".equals(c[3])).count();

            // Count logs by level
            long errors   = logs.stream().filter(l -> "ERROR".equals(l[2])).count();
            long criticals = logs.stream().filter(l -> "CRITICAL".equals(l[2])).count();
            long warns    = logs.stream().filter(l -> "WARN".equals(l[2])).count();

            String content = String.format("""
                <h1>System Overview</h1>
                <div class="stats">
                    <div class="card">
                        <div class="num">%d</div>
                        <div class="label">Total Logs</div>
                    </div>
                    <div class="card">
                        <div class="num" style="color:#f38ba8">%d</div>
                        <div class="label">Active Alerts</div>
                    </div>
                    <div class="card">
                        <div class="num" style="color:#a6e3a1">%d</div>
                        <div class="label">Clients Online</div>
                    </div>
                    <div class="card">
                        <div class="num" style="color:#ff79c6">%d</div>
                        <div class="label">Critical Logs</div>
                    </div>
                    <div class="card">
                        <div class="num" style="color:#f38ba8">%d</div>
                        <div class="label">Error Logs</div>
                    </div>
                    <div class="card">
                        <div class="num" style="color:#f9e2af">%d</div>
                        <div class="label">Warnings</div>
                    </div>
                </div>
                <p style="color:#a6adc8; font-size:13px;">
                    Navigate using the menu above to view detailed logs, alerts, and clients.
                    Pages auto-refresh every 10 seconds.
                </p>
                """,
                    logs.size(), alerts.size(), online,
                    criticals, errors, warns
            );

            sendHtml(ex, 200, htmlShell("Home", content));
        }
    }

    /** GET /logs — Recent logs as a styled HTML table */
    private class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            List<String[]> logs = logRepository.getRecentLogs(100);

            StringBuilder rows = new StringBuilder();
            if (logs.isEmpty()) {
                rows.append("<tr><td colspan='5' class='empty'>No logs found.</td></tr>");
            } else {
                for (String[] row : logs) {
                    rows.append(String.format("""
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td style="color:#585b70; font-size:12px;">%s</td>
                        </tr>
                        """,
                            row[0],          // ID
                            row[1],          // Client ID
                            badge(row[2]),   // Level as colored badge
                            row[3],          // Message
                            row[5]           // Timestamp
                    ));
                }
            }

            String content = String.format("""
                <h1>📋 Recent Logs <span style="font-size:14px;
                    color:#585b70; font-weight:normal;">
                    (last 100)</span></h1>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Client</th>
                            <th>Level</th>
                            <th>Message</th>
                            <th>Time</th>
                        </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>
                """, rows
            );

            sendHtml(ex, 200, htmlShell("Logs", content));
        }
    }

    /** GET /alerts — Active alerts as a styled HTML table */
    private class AlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            List<String[]> alerts = alertRepository.getActiveAlerts();

            StringBuilder rows = new StringBuilder();
            if (alerts.isEmpty()) {
                rows.append(
                        "<tr><td colspan='5' class='empty'>" +
                                "✅ No active alerts — system is healthy.</td></tr>"
                );
            } else {
                for (String[] row : alerts) {
                    rows.append(String.format("""
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td style="color:#585b70; font-size:12px;">%s</td>
                        </tr>
                        """,
                            row[0],          // ID
                            row[1],          // Client
                            badge(row[2]),   // Alert level badge
                            row[3],          // Message
                            row[4]           // Triggered at
                    ));
                }
            }

            String content = String.format("""
                <h1>🚨 Active Alerts</h1>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Client</th>
                            <th>Level</th>
                            <th>Message</th>
                            <th>Triggered At</th>
                        </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>
                """, rows
            );

            sendHtml(ex, 200, htmlShell("Alerts", content));
        }
    }

    /** GET /clients — All clients as a styled HTML table */
    private class ClientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            List<String[]> clients = clientRepository.getAllClients();

            StringBuilder rows = new StringBuilder();
            if (clients.isEmpty()) {
                rows.append(
                        "<tr><td colspan='5' class='empty'>No clients registered.</td></tr>"
                );
            } else {
                for (String[] row : clients) {
                    rows.append(String.format("""
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td style="color:#585b70; font-size:12px;">%s</td>
                        </tr>
                        """,
                            row[0],          // Client ID
                            row[1],          // Hostname
                            row[2],          // IP Address
                            badge(row[3]),   // Status badge (ONLINE/OFFLINE)
                            row[4]           // Last seen
                    ));
                }
            }

            String content = String.format("""
                <h1>🖥 Connected Clients</h1>
                <table>
                    <thead>
                        <tr>
                            <th>Client ID</th>
                            <th>Hostname</th>
                            <th>IP Address</th>
                            <th>Status</th>
                            <th>Last Seen</th>
                        </tr>
                    </thead>
                    <tbody>%s</tbody>
                </table>
                """, rows
            );

            sendHtml(ex, 200, htmlShell("Clients", content));
        }
    }
    public static  void main(String[] args) {
        HttpApiServer webServer = new HttpApiServer();
        webServer.start();
    }
}
