fetch('/monitoring/databases')
    .then(response => {

        if (!response.ok) {
            throw new Error("Failed to load database metadata");
        }

        return response.json();
    })
    .then(data => {

        loadDatabase(data);

    })
    .catch(error => {

        document.getElementById("database-summary").innerHTML =
        `
        <div class="alert alert-danger">
            Error loading database metadata
        </div>
        `;

        console.error(error);

    });


function loadDatabase(database) {

    document.getElementById("database-name").innerHTML =
        `
        🗄 ${database.databaseName}
        `;


    document.getElementById("database-version").innerHTML =
        database.databaseVersion || "-";


    document.getElementById("database-driver").innerHTML =
        database.driverName || "-";


    document.getElementById("database-url").innerHTML =
        database.url || "-";


    document.getElementById("schema-count").innerHTML =
        database.schemas
            ? database.schemas.length
            : 0;


    let schemaHTML = "";


    if (!database.schemas || database.schemas.length === 0) {

        schemaHTML =
        `
        <div class="alert alert-warning">
            No schema information found
        </div>
        `;

    }
    else {

        database.schemas.forEach(schema => {

            schemaHTML +=
            `
            <div class="schema-card">

                <h3>
                    📂 ${schema.schemaName}
                </h3>

                <p>
                    Tables:
                    <b>${schema.tables.length}</b>
                </p>

                <table class="table table-striped table-bordered">

                    <thead>
                        <tr>
                            <th>Table</th>
                            <th>Type</th>
                            <th>Columns</th>
                            <th>Primary Key</th>
                            <th>Foreign Key</th>
                            <th>Entity</th>
                        </tr>
                    </thead>

                    <tbody>
            `;


            schema.tables.forEach(table => {

                schemaHTML +=
                `
                    <tr>

                        <td>
                            <button
                                type="button"
                                class="btn btn-link table-link"
                                onclick="showTableDetails(
                                    '${escapeHtml(schema.schemaName)}',
                                    '${escapeHtml(table.tableName)}'
                                )"
                            >
                                📄 ${escapeHtml(table.tableName)}
                            </button>
                        </td>

                        <td>
                            ${escapeHtml(table.tableType)}
                        </td>

                        <td>
                            ${table.columnCount}
                        </td>

                        <td>
                            🔑 ${table.primaryKeyCount}
                        </td>

                        <td>
                            🔗 ${table.foreignKeyCount}
                        </td>

                        <td class="entity">
                            ${escapeHtml(table.entityName || "-")}
                        </td>

                    </tr>
                `;

            });


            schemaHTML +=
            `
                    </tbody>

                </table>

            </div>
            `;

        });

    }


    document.getElementById("schema-container")
        .innerHTML = schemaHTML;
}


/*
 * Load details for selected table
 */
function showTableDetails(schema, table) {

    console.log("Table clicked:", schema, table);


    const section =
        document.getElementById(
            "table-details-section"
        );


    const container =
        document.getElementById(
            "table-details-container"
        );


    section.style.display = "block";


    container.innerHTML =
    `
        <div class="alert alert-info">
            Loading table information...
        </div>
    `;


    const params = new URLSearchParams();


    if (schema) {
        params.append("schema", schema);
    }


    params.append("table", table);


    const url =
        `/monitoring/databases/table?${params.toString()}`;


    console.log("Calling:", url);


    fetch(url)

        .then(response => {

            if (!response.ok) {

                throw new Error(
                    `Failed to load table details. HTTP ${response.status}`
                );

            }

            return response.json();

        })

        .then(columns => {

            console.log("Table columns:", columns);

            renderTableDetails(
                table,
                columns
            );

        })

        .catch(error => {

            container.innerHTML =
            `
                <div class="alert alert-danger">
                    Error loading table information:
                    ${escapeHtml(error.message)}
                </div>
            `;

            console.error(error);

        });
}


/*
 * Render table column information
 */
function renderTableDetails(
    tableName,
    columns
) {

    const container =
        document.getElementById(
            "table-details-container"
        );


    if (!columns || columns.length === 0) {

        container.innerHTML =
        `
            <div class="alert alert-warning">
                No column information found.
            </div>
        `;

        return;
    }


    let html =
    `
        <div class="schema-card">

            <h3>
                📄 ${escapeHtml(tableName)}
            </h3>

            <p>
                Columns:
                <b>${columns.length}</b>
            </p>

            <table class="table table-striped table-bordered">

                <thead>

                    <tr>
                        <th>Column</th>
                        <th>Data Type</th>
                        <th>SQL Type</th>
                        <th>Size</th>
                        <th>Decimal Digits</th>
                        <th>Nullable</th>
                        <th>Default</th>
                        <th>Auto Increment</th>
                        <th>Primary Key</th>
                        <th>Foreign Key</th>
                    </tr>

                </thead>

                <tbody>
    `;


    columns.forEach(column => {

        html +=
        `
            <tr>

                <td>
                    ${escapeHtml(column.columnName)}
                </td>

                <td>
                    ${escapeHtml(column.dataType || "-")}
                </td>

                <td>
                    ${escapeHtml(column.sqlType || "-")}
                </td>

                <td>
                    ${column.size ?? "-"}
                </td>

                <td>
                    ${column.decimalDigits ?? "-"}
                </td>

                <td>
                    ${column.nullable ? "YES" : "NO"}
                </td>

                <td>
                    ${escapeHtml(column.defaultValue || "-")}
                </td>

                <td>
                    ${column.autoIncrement ? "YES" : "NO"}
                </td>

                <td>
                    ${column.primaryKey
                        ? "🔑 YES"
                        : "NO"}
                </td>

                <td>
                    ${column.foreignKey
                        ? "🔗 YES"
                        : "NO"}
                </td>

            </tr>
        `;

    });


    html +=
    `
                </tbody>

            </table>

        </div>
    `;


    container.innerHTML = html;


    document
        .getElementById("table-details-section")
        .scrollIntoView({
            behavior: "smooth"
        });
}


/*
 * Prevent HTML injection when displaying
 * database identifiers.
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