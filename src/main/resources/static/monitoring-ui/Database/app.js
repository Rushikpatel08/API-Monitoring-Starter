document.addEventListener("DOMContentLoaded", () => {
    loadDatabase();

    // Event delegation for table clicks
    document
        .getElementById("schema-container")
        .addEventListener("click", function (event) {

            const tableButton =
                event.target.closest(".table-link");

            if (!tableButton) {
                return;
            }

            const schema =
                tableButton.dataset.schema;

            const table =
                tableButton.dataset.table;

            showTableDetails(schema, table);
        });
});


/*
 * Load database information
 */
function loadDatabase() {

    fetch("/monitoring/databases")
        .then(response => {

            if (!response.ok) {
                throw new Error(
                    "Failed to load database metadata"
                );
            }

            return response.json();
        })
        .then(database => {

            renderDatabaseSummary(database);
            renderSchemas(database);

        })
        .catch(error => {

            document.getElementById(
                "database-summary"
            ).innerHTML = `
                <div class="alert alert-danger">
                    <strong>Error:</strong>
                    Unable to load database metadata.
                </div>
            `;

            console.error(error);
        });
}


/*
 * Database summary
 */
function renderDatabaseSummary(database) {

    document.getElementById("database-name").textContent =
        database.databaseName || "-";

    document.getElementById("database-version").textContent =
        database.databaseVersion || "-";

    document.getElementById("database-driver").textContent =
        database.driverName || "-";

    document.getElementById("driver-version").textContent =
        database.driverVersion || "-";

    document.getElementById("jdbc-version").textContent =
        database.jdbcVersion || "-";

    document.getElementById("database-username").textContent =
        database.username || "-";

    document.getElementById("database-url").textContent =
        database.url || "-";

    document.getElementById("database-catalog").textContent =
        database.catalog || "-";



    document.getElementById("schema-count").textContent =
        database.schemaCount ?? (database.schemas ? database.schemas.length : 0);

    document.getElementById("table-count").textContent =
        database.tableCount ?? "-";

    document.getElementById("view-count").textContent =
        database.viewCount ?? "-";

    document.getElementById("database-size").textContent =
        formatBytes(database.totalSizeBytes);


    document.getElementById("read-only").textContent =
        database.readOnly ? "Yes" : "No";


}

/*
 * Render schemas and tables
 */
function renderSchemas(database) {

    const container =
        document.getElementById(
            "schema-container"
        );

    if (
        !database.schemas ||
        database.schemas.length === 0
    ) {

        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📂</div>
                <h3>No schemas found</h3>
                <p>
                    No database schemas were returned
                    by the database connection.
                </p>
            </div>
        `;

        return;
    }


    let html = "";


    database.schemas.forEach((schema, schemaIndex) => {

        const tables =
            schema.tables || [];


        html += `
            <div class="schema-card">

                <div class="schema-header">

                    <div>
                        <div class="schema-title">
                            📂 ${escapeHtml(
                                schema.schemaName
                            )}
                        </div>

                        <div class="schema-subtitle">
                            ${tables.length}
                            ${tables.length === 1
                                ? "table"
                                : "tables"}
                        </div>
                    </div>

                    <span class="schema-badge">
                        Schema ${schemaIndex + 1}
                    </span>

                </div>


                ${
                    tables.length === 0

                    ? `
                        <div class="empty-table">
                            No tables found in this schema.
                        </div>
                    `

                    : `

                    <div class="table-wrapper">

                        <table class="database-table">

                            <thead>
                                <tr>
                                    <th>Table</th>
                                    <th>Type</th>
                                    <th>Columns</th>
                                    <th>Keys</th>
                                    <th>Entity</th>
                                    <th></th>
                                </tr>
                            </thead>

                            <tbody>

                                ${tables.map(table => {

                                    const keyCount =
                                        (table.primaryKeyCount || 0) +
                                        (table.foreignKeyCount || 0);

                                    return `

                                        <tr>

                                            <td>

                                                <button
                                                    type="button"
                                                    class="table-link"
                                                    data-schema="${escapeAttribute(schema.schemaName)}"
                                                    data-table="${escapeAttribute(table.tableName)}"
                                                >

                                                    <span class="table-icon">
                                                        📄
                                                    </span>

                                                    <span>
                                                        ${escapeHtml(
                                                            table.tableName
                                                        )}
                                                    </span>

                                                </button>

                                            </td>


                                            <td>
                                                <span class="type-badge">
                                                    ${escapeHtml(
                                                        table.tableType || "-"
                                                    )}
                                                </span>
                                            </td>


                                            <td>
                                                <span class="number-badge">
                                                    ${table.columnCount || 0}
                                                </span>
                                            </td>


                                            <td>

                                                <div class="key-summary">

                                                    ${
                                                        table.primaryKeyCount > 0
                                                            ? `<span title="Primary Keys">
                                                                🔑 ${table.primaryKeyCount}
                                                              </span>`
                                                            : ""
                                                    }

                                                    ${
                                                        table.foreignKeyCount > 0
                                                            ? `<span title="Foreign Keys">
                                                                🔗 ${table.foreignKeyCount}
                                                              </span>`
                                                            : ""
                                                    }

                                                    ${
                                                        keyCount === 0
                                                            ? `<span class="muted">None</span>`
                                                            : ""
                                                    }

                                                </div>

                                            </td>


                                            <td class="entity-name">

                                                ${
                                                    table.entityName
                                                        ? escapeHtml(
                                                            table.entityName
                                                          )
                                                        : `<span class="muted">-</span>`
                                                }

                                            </td>


                                            <td class="action-cell">

                                                <button
                                                    type="button"
                                                    class="view-button table-link"
                                                    data-schema="${escapeAttribute(schema.schemaName)}"
                                                    data-table="${escapeAttribute(table.tableName)}"
                                                >
                                                    View
                                                </button>

                                            </td>

                                        </tr>

                                    `;
                                }).join("")}

                            </tbody>

                        </table>

                    </div>

                    `
                }

            </div>
        `;
    });


    container.innerHTML = html;
}


/*
 * Show table details
 */
function showTableDetails(schema, table) {

    const section =
        document.getElementById(
            "table-details-section"
        );

    const container =
        document.getElementById(
            "table-details-container"
        );


    section.classList.remove("hidden");


    container.innerHTML = `
        <div class="loading-state">

            <div class="spinner-border"></div>

            <div>
                Loading table information...
            </div>

        </div>
    `;


    const params =
        new URLSearchParams();


    if (schema) {
        params.append(
            "schema",
            schema
        );
    }


    params.append(
        "table",
        table
    );


    fetch(
        `/monitoring/databases/table?${params.toString()}`
    )

        .then(response => {

            if (!response.ok) {

                throw new Error(
                    "Failed to load table details"
                );
            }

            return response.json();
        })

        .then(columns => {

            renderTableDetails(
                schema,
                table,
                columns
            );

        })

        .catch(error => {

            container.innerHTML = `
                <div class="alert alert-danger">

                    <strong>Error loading table.</strong>

                    <p class="mb-0">
                        ${escapeHtml(error.message)}
                    </p>

                </div>
            `;

            console.error(error);
        });
}


/*
 * Render columns
 */
function renderTableDetails(
    schema,
    tableName,
    columns
) {

    const container =
        document.getElementById(
            "table-details-container"
        );


    if (
        !columns ||
        columns.length === 0
    ) {

        container.innerHTML = `
            <div class="empty-state">

                <div class="empty-icon">
                    📄
                </div>

                <h3>
                    No column information
                </h3>

                <p>
                    No columns were found for
                    <strong>${escapeHtml(tableName)}</strong>.
                </p>

            </div>
        `;

        return;
    }


    const primaryKeyCount =
        columns.filter(
            column => column.primaryKey
        ).length;


    const foreignKeyCount =
        columns.filter(
            column => column.foreignKey
        ).length;


    let html = `

        <div class="details-card">

            <div class="details-header">

                <div>

                    <div class="breadcrumb-text">
                        📂 ${escapeHtml(schema || "Default")}
                        /
                    </div>

                    <h2>
                        📄 ${escapeHtml(tableName)}
                    </h2>

                    <p>
                        Table column information
                    </p>

                </div>


                <button
                    type="button"
                    class="close-details"
                    onclick="closeTableDetails()"
                >
                    ✕
                </button>

            </div>


            <div class="details-summary">

                <div class="summary-item">
                    <span class="summary-label">
                        Columns
                    </span>

                    <strong>
                        ${columns.length}
                    </strong>
                </div>


                <div class="summary-item">
                    <span class="summary-label">
                        Primary Keys
                    </span>

                    <strong>
                        🔑 ${primaryKeyCount}
                    </strong>
                </div>


                <div class="summary-item">
                    <span class="summary-label">
                        Foreign Keys
                    </span>

                    <strong>
                        🔗 ${foreignKeyCount}
                    </strong>
                </div>

            </div>


            <div class="table-wrapper">

                <table class="database-table details-table">

                    <thead>

                        <tr>
                            <th>#</th>
                            <th>Column</th>
                            <th>Data Type</th>
                            <th>Size</th>
                            <th>Nullable</th>
                            <th>Default</th>
                            <th>Auto Increment</th>
                            <th>Key</th>
                            <th>Validations</th>
                        </tr>

                    </thead>


                    <tbody>
    `;


    columns.forEach((column, index) => {

        let key = "";

        if (column.primaryKey) {

            key += `
                <span class="key-badge primary">
                    🔑 Primary Key
                </span>
            `;
        }


        if (column.foreignKey) {

            key += `
                <span class="key-badge foreign">
                    🔗 Foreign Key
                </span>
            `;
        }


        if (!key) {
            key = `<span class="muted">-</span>`;
        }


        html += `

            <tr>

                <td class="row-number">
                    ${index + 1}
                </td>


                <td class="column-name">
                    ${escapeHtml(
                        column.columnName
                    )}
                </td>


                <td>
                    <span class="data-type">
                        ${escapeHtml(
                            column.dataType || "-"
                        )}
                    </span>
                </td>


                <td>
                    ${
                        column.size !== null &&
                        column.size !== undefined
                            ? column.size
                            : "-"
                    }
                </td>


                <td>
                    ${
                        column.nullable
                            ? `<span class="yes">YES</span>`
                            : `<span class="no">NO</span>`
                    }
                </td>


                <td class="default-value">
                    ${
                        column.defaultValue
                            ? escapeHtml(
                                column.defaultValue
                              )
                            : "-"
                    }
                </td>


                <td>
                    ${
                        column.autoIncrement
                            ? `<span class="yes">YES</span>`
                            : `<span class="no">NO</span>`
                    }
                </td>


                <td>
                    ${key}
                </td>
<td>

    ${
        column.validations &&
        column.validations.length > 0

        ?

        column.validations
            .map(validation =>
                `
                <span class="validation-badge">
                    ${escapeHtml(validation)}
                </span>
                `
            )
            .join("")

        :

        `<span class="muted">-</span>`
    }

</td>
            </tr>

        `;
    });


    html += `

                    </tbody>

                </table>

            </div>

        </div>
    `;


    container.innerHTML = html;


    document
        .getElementById(
            "table-details-section"
        )
        .scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
}


/*
 * Close details
 */
function closeTableDetails() {

    const section =
        document.getElementById(
            "table-details-section"
        );

    section.classList.add("hidden");

    document
        .getElementById(
            "table-details-container"
        )
        .innerHTML = "";
}


/*
 * HTML safety
 */
function escapeHtml(value) {

    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


function escapeAttribute(value) {

    return escapeHtml(value);
}
function formatBytes(bytes) {

    if (bytes === null || bytes === undefined)
        return "-";

    if (bytes === 0)
        return "In-Memory";

    const units = ["Bytes", "KB", "MB", "GB", "TB"];

    let index = 0;

    while (bytes >= 1024 && index < units.length - 1) {
        bytes /= 1024;
        index++;
    }

    return bytes.toFixed(2) + " " + units[index];
}