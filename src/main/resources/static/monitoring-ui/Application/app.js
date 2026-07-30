document.addEventListener("DOMContentLoaded", () => {
    loadApplication();
    // Start live log stream
    loadLogs();
    // Refresh application information every 5 seconds
    setInterval(loadApplication, 5000);
    // Log filter
    document
        .getElementById("log-level-filter")
        .addEventListener("change", filterLogs);
    // Clear logs
    document
        .getElementById("clear-logs")
        .addEventListener("click", clearLogs);
});
// =========================================================
// APPLICATION INFORMATION
// =========================================================
function loadApplication() {
    fetch("/monitoring/applications/info")
        .then(response => {
            if (!response.ok) {
                throw new Error("Application information request failed: "+ response.status);
            }
            return response.json();
        })
        .then(data => {
            updateApplicationInfo(data);
            updateLastUpdated();
            setLiveStatus(true);

        })
        .catch(error => {
            console.error("Unable to load application information:",error);
            setLiveStatus(false);
        });
}
// =========================================================
// UPDATE APPLICATION INFORMATION
// =========================================================
function updateApplicationInfo(data) {
    // =====================================================
    // STATUS
    // =====================================================
    const status =String(data.status || "UNKNOWN").toUpperCase();

    document.getElementById("status").innerHTML =
        status === "UP"
            ? "🟢 UP"
            : "🔴 " + escapeHtml(status);

    document.getElementById("health-summary").innerHTML =
        status === "UP"
            ? "Application is healthy"
            : "Application health requires attention";

    document.getElementById("health-status-badge").innerHTML = escapeHtml(status);

    // =====================================================
    // APPLICATION
    // =====================================================

    document.getElementById("app-name").innerHTML =
        escapeHtml(data.applicationName || "-");

    document.getElementById("active-profiles").innerHTML =
        "Profile: " +
        escapeHtml(formatProfiles(data.activeProfiles));


    // =====================================================
    // SPRING BOOT
    // =====================================================

    document.getElementById("spring-version").innerHTML =
        escapeHtml(data.springBootVersion || "-");


    // =====================================================
    // JAVA
    // =====================================================

    document.getElementById("java-version").innerHTML =
        escapeHtml(data.javaVersion || "-");

    document.getElementById("java-vendor").innerHTML =
        escapeHtml(data.javaVendor || "-");

    document.getElementById("jvm-vendor").innerHTML =
        escapeHtml(data.javaVendor || "-");


    // =====================================================
    // MEMORY
    // =====================================================

    const usedMemory =
        Number(data.usedMemory || 0);

    const maxMemory =
        Number(data.maxMemory || 0);

    const totalMemory =
        Number(data.totalMemory || 0);

    const freeMemory =
        Number(data.freeMemory || 0);


    document.getElementById("memory").innerHTML =
        formatBytes(usedMemory)
        + " / "
        + formatBytes(maxMemory);


    document.getElementById("total-memory").innerHTML =
        formatBytes(totalMemory);

    document.getElementById("free-memory").innerHTML =
        formatBytes(freeMemory);

    document.getElementById("max-memory").innerHTML =
        formatBytes(maxMemory);


    // =====================================================
    // MEMORY PERCENTAGE
    // =====================================================

    let memoryPercent = 0;

    if (maxMemory > 0) {

        memoryPercent =
            (usedMemory / maxMemory) * 100;

    }

    document.getElementById("memory-percent").innerHTML =
        memoryPercent.toFixed(2) + "% used";

    document.getElementById("memory-progress").style.width =
        Math.min(memoryPercent, 100) + "%";


    // =====================================================
    // CPU
    // =====================================================

    document.getElementById("processors").innerHTML =
        data.availableProcessors ?? "-";


    // =====================================================
    // THREADS
    // =====================================================

    document.getElementById("threads").innerHTML =
        data.threadCount ?? "-";

    document.getElementById("jvm-thread-count").innerHTML =
        data.threadCount ?? "-";


    // =====================================================
    // UPTIME
    // =====================================================

    document.getElementById("uptime").innerHTML =
        formatUptime(data.uptime);


    // =====================================================
    // SYSTEM
    // =====================================================

    document.getElementById("os-name").innerHTML =
        escapeHtml(data.operatingSystem || "-");

    document.getElementById("os-version").innerHTML =
        escapeHtml(data.osVersion || "-");

    document.getElementById("architecture").innerHTML =
        escapeHtml(data.architecture || "-");

    document.getElementById("process-id").innerHTML =
        escapeHtml(data.processId || "-");

    document.getElementById("bean-count").innerHTML =
        data.beanCount ?? "-";


    // =====================================================
    // DATABASE
    // =====================================================

    document.getElementById("database-product").innerHTML =
        escapeHtml(data.databaseProduct || "Unknown");

    const databaseStatus =
        String(data.databaseStatus || "UNKNOWN").toUpperCase();

    document.getElementById("database-status").innerHTML =
        databaseStatus === "CONNECTED"
            ? "🟢 CONNECTED"
            : "🔴 " + escapeHtml(databaseStatus);

    document.getElementById("database-status-badge").innerHTML =
        escapeHtml(databaseStatus);


    // =====================================================
    // ENVIRONMENT
    // =====================================================

    document.getElementById("environment-app-name").innerHTML =
        escapeHtml(data.applicationName || "-");

    document.getElementById("environment-profiles").innerHTML =
        escapeHtml(formatProfiles(data.activeProfiles));

    document.getElementById("server-port").innerHTML =
        escapeHtml(data.serverPort || "-");

    document.getElementById("context-path").innerHTML =
        escapeHtml(data.contextPath || "/");


    // =====================================================
    // ENVIRONMENT PROPERTIES
    // =====================================================

    renderEnvironmentProperties(data.environment);


    // =====================================================
    // APPLICATION HEALTH
    // =====================================================

    renderHealth(data);


    // =====================================================
    // ACTUATOR METRICS
    // =====================================================

    document.getElementById("http-request-count").innerHTML =
        formatNumber(data.httpRequests);

    document.getElementById("http-error-rate").innerHTML =
        calculateHttpErrorRate(
            data.httpRequests,
            data.httpErrors
        );

    document.getElementById("jvm-memory-used").innerHTML =
        formatBytes(
            data.jvmMemoryUsed || usedMemory
        );

    document.getElementById("jvm-thread-count").innerHTML =
        formatNumber(
            data.jvmThreads || data.threadCount
        );
}
function renderHealth(data) {

    const container =
        document.getElementById("health-container");

    if (!container) {
        return;
    }

    const healthDetails =
        data.healthDetails || {};

    const entries =
        Object.entries(healthDetails);


    if (entries.length === 0) {

        container.innerHTML = `
            <div class="col-12">

                <div class="loading-box">

                    <strong>
                        ${escapeHtml(data.status || "UNKNOWN")}
                    </strong>

                    <div class="small-text mt-1">
                        No individual health indicators available.
                    </div>

                </div>

            </div>
        `;

        return;
    }


    container.innerHTML =
        entries.map(([name, health]) => {

            const componentStatus =
                String(
                    health?.status || "UNKNOWN"
                ).toUpperCase();

            const isUp =
                componentStatus === "UP";

            const statusIcon =
                isUp ? "🟢" : "🔴";


            return `
                <div class="col-xl-3 col-lg-4 col-md-6">

                    <div class="metric-card health-card">

                        <div class="metric-title">

                            ${statusIcon}

                            ${escapeHtml(
                                formatHealthName(name)
                            )}

                        </div>

                        <h3 class="
                            ${isUp
                                ? "health-up"
                                : "health-down"}
                        ">

                            ${escapeHtml(componentStatus)}

                        </h3>

                        <div class="metric-subtitle">

                            ${isUp
                                ? "Health indicator is healthy"
                                : "Health indicator requires attention"}

                        </div>

                    </div>

                </div>
            `;

        }).join("");
}
function formatHealthName(name) {

    if (!name) {
        return "Unknown";
    }

    return String(name)
        .replace(/([a-z])([A-Z])/g, "$1 $2")
        .replace(/[-_]/g, " ")
        .replace(/\b\w/g, char => char.toUpperCase());
}
// =========================================================
// LIVE LOG STREAM
// =========================================================

function loadLogs() {

    console.log(
        "Connecting to live application logs..."
    );


    const eventSource =
        new EventSource(
            "/monitoring/application/logs/stream"
        );


    eventSource.onopen = function() {

        console.log(
            "Live log stream connected"
        );

        setLiveStatus(true);

    };


    eventSource.onmessage = function(event) {

        try {

            const log =
                JSON.parse(event.data);


            console.log(
                "Live log:",
                log
            );


            addLogToTable(log);

        }
        catch(error) {

            console.error(
                "Unable to parse live log:",
                error
            );

        }

    };


    eventSource.onerror = function(error) {

        console.error(
            "Live log connection error:",
            error
        );


        setLiveStatus(false);

        /*
         EventSource automatically attempts
         to reconnect when the connection drops.
        */

    };

}


// =========================================================
// ADD LIVE LOG TO TABLE
// =========================================================

function addLogToTable(log) {

    const tbody =
        document.getElementById("logs");


    // Remove "Waiting for application logs..."
    const emptyRow =
        tbody.querySelector(".empty-state");

    if (emptyRow) {

        tbody.innerHTML = "";

    }


    const row =
        document.createElement("tr");


    const level =
        String(
            log.level || "INFO"
        ).toUpperCase();


    row.dataset.level = level;


    row.innerHTML = `

        <td>

            <span class="badge bg-${levelColor(level)}">

                ${escapeHtml(level)}

            </span>

        </td>


        <td>

            ${escapeHtml(
                log.message || "-"
            )}

        </td>


        <td>

            ${escapeHtml(
                log.logger || "-"
            )}

        </td>


        <td>

            ${escapeHtml(
                log.thread || "-"
            )}

        </td>


        <td>

            ${formatTimestamp(
                log.timestamp
            )}

        </td>

    `;


    // Newest log goes on top
    tbody.prepend(row);


    // Keep latest 100 logs
    while (
        tbody.children.length > 100
    ) {

        tbody.removeChild(
            tbody.lastElementChild
        );

    }


    // Apply currently selected filter
    filterLogs();

}


// =========================================================
// LOG FILTER
// =========================================================

function filterLogs() {

    const filter =
        document.getElementById(
            "log-level-filter"
        ).value;


    const rows =
        document.querySelectorAll(
            "#logs tr"
        );


    rows.forEach(row => {

        const level =
            row.dataset.level;


        if (
            filter === "ALL"
            || level === filter
        ) {

            row.style.display = "";

        }
        else {

            row.style.display = "none";

        }

    });

}


// =========================================================
// CLEAR LOGS
// =========================================================

function clearLogs() {

    const tbody =
        document.getElementById("logs");


    tbody.innerHTML = `

        <tr>

            <td
                colspan="5"
                class="empty-state">

                Waiting for application logs...

            </td>

        </tr>

    `;

}


// =========================================================
// LIVE INDICATOR
// =========================================================

function setLiveStatus(isLive) {

    const indicator =
        document.getElementById(
            "live-indicator"
        );


    const status =
        document.getElementById(
            "refresh-status"
        );


    if (isLive) {

        indicator.classList.add(
            "live-active"
        );

        status.innerHTML =
            "Live";

    }
    else {

        indicator.classList.remove(
            "live-active"
        );

        status.innerHTML =
            "Reconnecting...";

    }

}


// =========================================================
// LAST UPDATED
// =========================================================

function updateLastUpdated() {

    const element =
        document.getElementById(
            "last-updated"
        );


    if (!element) {
        return;
    }


    element.innerHTML =
        new Date().toLocaleTimeString();

}


// =========================================================
// FORMAT BYTES
// =========================================================

function formatBytes(bytes) {

    if (
        bytes === null
        || bytes === undefined
        || isNaN(bytes)
        || bytes <= 0
    ) {

        return "-";

    }


    const units = [
        "B",
        "KB",
        "MB",
        "GB",
        "TB"
    ];


    let value =
        Number(bytes);


    let unitIndex = 0;


    while (
        value >= 1024
        && unitIndex < units.length - 1
    ) {

        value =
            value / 1024;

        unitIndex++;

    }


    return value.toFixed(2)
        + " "
        + units[unitIndex];

}


// =========================================================
// FORMAT UPTIME
// =========================================================

function formatUptime(milliseconds) {

    if (
        milliseconds === null
        || milliseconds === undefined
    ) {

        return "-";

    }


    let seconds =
        Math.floor(
            Number(milliseconds) / 1000
        );


    const days =
        Math.floor(
            seconds / 86400
        );


    seconds %= 86400;


    const hours =
        Math.floor(
            seconds / 3600
        );


    seconds %= 3600;


    const minutes =
        Math.floor(
            seconds / 60
        );


    seconds %= 60;


    if (days > 0) {

        return `${days}d ${hours}h ${minutes}m`;

    }


    if (hours > 0) {

        return `${hours}h ${minutes}m ${seconds}s`;

    }


    if (minutes > 0) {

        return `${minutes}m ${seconds}s`;

    }


    return `${seconds}s`;

}


// =========================================================
// FORMAT PROFILES
// =========================================================

function formatProfiles(profiles) {

    if (
        !profiles
        || profiles.length === 0
    ) {

        return "default";

    }


    if (Array.isArray(profiles)) {

        return profiles.join(", ");

    }


    return String(profiles);

}


// =========================================================
// FORMAT TIMESTAMP
// =========================================================

function formatTimestamp(timestamp) {

    if (!timestamp) {

        return "-";

    }


    try {

        return new Date(timestamp)
            .toLocaleTimeString();

    }
    catch(error) {

        return timestamp;

    }

}


// =========================================================
// LEVEL COLOR
// =========================================================

function levelColor(level) {

    switch (
        String(level).toUpperCase()
    ) {

        case "ERROR":
            return "danger";

        case "WARN":
        case "WARNING":
            return "warning";

        case "DEBUG":
            return "secondary";

        case "TRACE":
            return "info";

        case "INFO":
        default:
            return "success";

    }

}


// =========================================================
// HTML ESCAPE
// =========================================================

function escapeHtml(value) {

    if (
        value === null
        || value === undefined
    ) {

        return "";

    }


    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

}
function renderEnvironmentProperties(environment) {

    const tbody =
        document.getElementById(
            "environment-properties"
        );

    if (!tbody) {
        return;
    }


    if (
        !environment ||
        Object.keys(environment).length === 0
    ) {

        tbody.innerHTML = `
            <tr>

                <td
                    colspan="2"
                    class="empty-state">

                    No environment data available

                </td>

            </tr>
        `;

        return;
    }


    tbody.innerHTML =
        Object.entries(environment)
            .map(([property, value]) => {

                return `
                    <tr>

                        <td>
                            ${escapeHtml(property)}
                        </td>

                        <td>
                            ${escapeHtml(value)}
                        </td>

                    </tr>
                `;

            })
            .join("");
}
// =========================================================
// FORMAT NUMBER
// =========================================================

function formatNumber(value) {

    if (
        value === null ||
        value === undefined ||
        isNaN(value)
    ) {
        return "-";
    }

    return Number(value).toLocaleString();
}


// =========================================================
// CALCULATE HTTP ERROR RATE
// =========================================================

function calculateHttpErrorRate(
    requests,
    errors
) {

    if (
        requests === null ||
        requests === undefined ||
        isNaN(requests) ||
        Number(requests) <= 0
    ) {
        return "-";
    }

    if (
        errors === null ||
        errors === undefined ||
        isNaN(errors)
    ) {
        return "-";
    }

    const rate =
        (Number(errors) / Number(requests)) * 100;

    return rate.toFixed(2) + "%";
}