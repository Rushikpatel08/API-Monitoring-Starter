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
                <div class="section-card">

                    <div class="section-title">
                       Parameters
                    </div>

                    <table class="table api-table">
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
                </table></div>`;
            }
            else{
                parametersHtml = `
                <p><strong>Parameters:</strong> None</p>`;
            }
            let requestBodyHtml = "";
            if(api.request){
                window["requestExample"+index+j] = api.request.example;
                window["requestSchema"+index+j] = api.request.schema;
                requestBodyHtml = `
                <div class="section-card">

                 <div class="section-title">
                    Request Body
                 </div>
                <p><strong>${api.request.mediaType}</strong></p>
                <div class="response-toggle">
                    <button id="requestExampleBtn${index}${j}" class="btn btn-sm btn-primary" onclick="showRequestExample('${index}${j}')">Example Value</button>
                    <button id="requestSchemaBtn${index}${j}" class="btn btn-sm btn-outline-primary" onclick="showRequestSchema('${index}${j}')">Schema</button>
                </div>
                <div id="requestBox${index}${j}" class="response-box"><pre>${JSON.stringify(api.request.example,null,2)}</pre></div>
                </div>`;
            }
            let responseHtml = "";
            // =============================
            // RESPONSE
            // =============================
            if(api.response){
                window["example"+index+j] =api.response.example;
                window["schema"+index+j] =api.response.schema;
                responseHtml = `
                <div class="section-card">

                   <div class="section-title">
                       Responses
                   </div>

                   <table class="table api-table">
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                           <td><span class="status-badge success">${api.response.code}</span></td>
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
                </div>
                `;
            }
            apisHtml += `

            <div class="api-item">
                <div class="api-header ${api.httpMethod.toLowerCase()}" data-bs-toggle="collapse" data-bs-target="#api${index}${j}">
                    <span class="method">${api.httpMethod}</span>
                    <span class="endpoint">${api.endpoint}</span>
                    ${api.summary && api.summary.trim() !== ''
                            ? `<span class="summary">${api.summary}</span>`
                            : ''}
                    <span class="ms-auto d-flex flex-wrap gap-1">
                        <button class="btn btn-success btn-sm py-0 px-2" onclick="event.stopPropagation(); downloadBruno('${api.id}')">Bruno</button>
                        <button class="btn btn-warning btn-sm py-0 px-2" onclick="event.stopPropagation(); downloadInsomnia('${api.id}')">Insomnia</button>
                        <button class="btn btn-info btn-sm py-0 px-2" onclick="event.stopPropagation(); downloadPostman('${api.id}')">Postman</button>
                    </span>
                </div>
                <div id="api${index}${j}" class="collapse">
                    <div class="api-body">
                         <div class="section-card-heading">
                            <div class="section-title-heading method-name">
                                  <strong>Method Name : </strong>${api.javaMethod}()
                                  ${api.description ? `<p style="font-weight: normal;margin-top:1%;">${api.description}</p>`:''}
                            </div>
                        </div>
                        ${parametersHtml}
                        ${requestBodyHtml}
                        ${responseHtml}

                    </div>
                </div>
            </div>
            `;
        });
        container.innerHTML += `
        <div class="accordion-item">
            <h2 class="accordion-header">
                <button class="accordion-button collapsed api-controller sub-title"
                type="button" data-bs-toggle="collapse"
                data-bs-target="#controller${index}">${controller.controller}${controller.tagDescription ? `<span class="tag-desc">${controller.tagDescription}</span>`: ""  }</button>
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
function showRequestExample(id){

    const box =
        document.getElementById("requestBox"+id);


    box.innerHTML =
    `<pre>${JSON.stringify(window["requestExample"+id],null,2)}</pre>`;


    const exampleBtn =
        document.getElementById("requestExampleBtn"+id);

    const schemaBtn =
        document.getElementById("requestSchemaBtn"+id);


    exampleBtn.classList.remove("btn-outline-primary");
    exampleBtn.classList.add("btn-primary");


    schemaBtn.classList.remove("btn-primary");
    schemaBtn.classList.add("btn-outline-primary");

}function showRequestSchema(id){

     const box =
         document.getElementById("requestBox"+id);


     box.innerHTML =
         generateSchemaHtml(window["requestSchema"+id]);


     const exampleBtn =
         document.getElementById("requestExampleBtn"+id);


     const schemaBtn =
         document.getElementById("requestSchemaBtn"+id);



     schemaBtn.classList.remove("btn-outline-primary");
     schemaBtn.classList.add("btn-primary");


     exampleBtn.classList.remove("btn-primary");
     exampleBtn.classList.add("btn-outline-primary");

 }
 function downloadBruno(id){


 window.location.href =
 "/api/v1/monitoring/export/bruno/"
 +id;


 }

 function downloadInsomnia(id){

 window.location.href =
 "/api/v1/monitoring/export/insomnia/"
 +id;

 }

 function downloadPostman(id){

 window.location.href =
 "/api/v1/monitoring/export/postman/"
 +id;

 }
 function downloadBrunoCollection(){

     window.location.href =
     "/api/v1/monitoring/export/bruno/collection";

 }

 function downloadInsomniaCollection(){

     window.location.href =
     "/api/v1/monitoring/export/insomnia/collection";

 }

 function downloadPostmanCollection(){

     window.location.href =
     "/api/v1/monitoring/export/postman/collection";

 }

function setTheme(theme) {
    document.body.setAttribute('data-theme', theme);
    localStorage.setItem('monitoring-theme', theme);
}

function initializeTheme() {
    const savedTheme = localStorage.getItem('monitoring-theme');
    setTheme(savedTheme === 'dark' ? 'dark' : 'light');
}

initializeTheme();
