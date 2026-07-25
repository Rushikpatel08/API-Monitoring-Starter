fetch('/monitoring/databases')
    .then(response => response.json())
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



function loadDatabase(database){


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
        database.schemas ?
        database.schemas.length :
        0;



    let schemaHTML = "";



    if(!database.schemas || database.schemas.length === 0){

        schemaHTML =
        `
        <div class="alert alert-warning">
            No schema information found
        </div>
        `;

    }
    else{


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
                        📄 ${table.tableName}
                    </td>


                    <td>
                        ${table.tableType}
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
                        ${table.entityName || '-'}
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