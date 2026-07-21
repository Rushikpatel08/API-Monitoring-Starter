fetch('/api/v1/monitoring/apis')

.then(response => response.json())

.then(data => {
    const container = document.getElementById("container");
    data.forEach((controller,index)=>{
        let apisHtml = "";
        controller.apis.forEach((api,j)=>{
            let parametersHtml = "";
            // =============================
            // PARAMETERS
            // =============================
            // =============================
            // PARAMETERS
            // =============================
            if(api.parameters && api.parameters.length > 0){
                parametersHtml = `
                <p><strong>Parameters</strong></p>

                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>In</th>
                            <th>Type</th>
                            <th>Required</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${api.parameters.map(param => `
                            <tr>
                                <td>${param.name}</td>
                                <td>${param.type}</td>
                                <td>${param.dataType}</td>
                                <td>
                                    ${param.required ? "✅" : "❌"}
                                </td>
                            </tr>
                        `).join("")}
                    </tbody>
                </table>`;
            }
            else{
                parametersHtml = `
                <p><strong>Parameters:</strong> None</p>`;
            }
            let responseHtml = "";
            // =============================
            // RESPONSE
            // =============================
            if(api.response){
                window["example"+index+j] =api.response.example;
                window["schema"+index+j] =api.response.schema;
                responseHtml = `
                <h4>Responses</h4>
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                           <td>${api.response.code}</td>
                            <td>${api.response.description}</td>
                        </tr>
                    </tbody>
                </table>
                <p><strong>Response Body</strong></p>
                <div class="response-toggle">
                    <button id="exampleBtn${index}${j}"
                            class="btn btn-sm btn-primary"
                            onclick="showExample('${index}${j}')">
                        Example Value
                    </button>

                    <button id="schemaBtn${index}${j}"
                            class="btn btn-sm btn-outline-primary"
                            onclick="showSchema('${index}${j}')">
                        Schema
                    </button>
                </div>
                <div id="responseBox${index}${j}" class="response-box"><pre>${JSON.stringify(api.response.example,null,2)}</pre></div>
                `;
            }
            apisHtml += `
            <div class="api-item">
                <div class="api-header ${api.httpMethod.toLowerCase()}" data-bs-toggle="collapse" data-bs-target="#api${index}${j}">
                    <span class="method">${api.httpMethod}</span>
                    <span class="endpoint">${api.endpoint}</span>
                </div>
                <div id="api${index}${j}" class="collapse">
                    <div class="api-body">
                        <p class="method-name"><strong>Method Name : </strong>${api.javaMethod}()</p>
                        ${parametersHtml}
                        ${responseHtml}
                    </div>
                </div>
            </div>
            `;
        });
        container.innerHTML += `
        <div class="accordion-item">
            <h2 class="accordion-header">
                <button class="accordion-button collapsed api-controller sub-title" type="button" data-bs-toggle="collapse" data-bs-target="#controller${index}">${controller.controller}</button>
            </h2>

            <div id="controller${index}" class="accordion-collapse collapse">
                <div class="accordion-body">${apisHtml}</div>
            </div>
        </div>
        `;
    });
})


.catch(error=>{
    console.error("Unable to load APIs",error);
});
// =================================================
// SHOW EXAMPLE
// =================================================
function showExample(id){

    const box = document.getElementById("responseBox"+id);

    box.innerHTML = `<pre>${JSON.stringify(window["example"+id],null,2)}</pre>`;

    const exampleBtn = document.getElementById("exampleBtn"+id);
    const schemaBtn = document.getElementById("schemaBtn"+id);

    // Example active
    exampleBtn.classList.remove("btn-outline-primary");
    exampleBtn.classList.add("btn-primary");

    // Schema inactive
    schemaBtn.classList.remove("btn-primary");
    schemaBtn.classList.add("btn-outline-primary");
}


function showSchema(id){

    const box = document.getElementById("responseBox"+id);

    box.innerHTML = generateSchemaHtml(window["schema"+id]);

    const exampleBtn = document.getElementById("exampleBtn"+id);
    const schemaBtn = document.getElementById("schemaBtn"+id);

    // Schema active
    schemaBtn.classList.remove("btn-outline-primary");
    schemaBtn.classList.add("btn-primary");

    // Example inactive
    exampleBtn.classList.remove("btn-primary");
    exampleBtn.classList.add("btn-outline-primary");
}
// =================================================
// GENERATE SCHEMA HTML
// =================================================
function generateSchemaHtml(schema){

    if(!schema){
        return "No schema available";
    }

    let html = `<h5>${schema.name}</h5><ul class="schema-list">`;

    Object.entries(schema.fields)
    .forEach(([field,type])=>{

        let displayType = type;

        if(type.includes("int64")){
            displayType = `integer <button class="type-badge">int64</button>`;
        }
        else if(type.includes("int32")){
            displayType = `integer <button class="type-badge">int32</button>`;
        }
        else if(type.includes("date-time")){
            displayType = `string <button class="type-badge">date-time</button>`;
        }
        else if(type.includes("double")){
            displayType = `number <button class="type-badge">double</button>`;
        }

        html += `<li><strong>${field} :</strong> ${displayType}</li>`;
    });

    html += `</ul>`;

    return html;
}