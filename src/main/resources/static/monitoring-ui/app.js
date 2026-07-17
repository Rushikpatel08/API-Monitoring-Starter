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
            if(api.parameters && api.parameters.length > 0){
                parametersHtml = `
                <p><strong>Parameters:</strong></p>
                <ul>
                    ${api.parameters.map(param => `
                        <li>
                            <strong>${param.name}</strong>(${param.dataType}) - ${param.type}
                            ${param.required ? "(Required)" : "(Optional)"}
                        </li>
                    `).join("")}
                </ul>`;
            }
            else{
                parametersHtml = `<p><strong>Parameters:</strong> None</p>`;
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
                <p><strong>Media type</strong></p>
                <p>${api.response.mediaType}</p>
                <p><strong>Response Body</strong></p>
                <div class="response-toggle">
                  <button id="exampleBtn${index}${j}" class="btn btn-primary btn-sm active"  onclick="showExample('${index}${j}')">Example</button>
                  <button id="schemaBtn${index}${j}" class="btn btn-outline-primary btn-sm"  onclick="showSchema('${index}${j}')">Schema</button>
                </div>
                <div id="responseBox${index}${j}" class="response-box">
                    ${JSON.stringify(api.response.example,null,2)}
                </div>

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

    const box =document.getElementById("responseBox"+id);
    box.innerHTML = JSON.stringify(window["example"+id],null,2);
    document.getElementById("exampleBtn"+id).classList.add("active");
    document.getElementById("exampleBtn"+id).classList.remove("btn-outline-primary");
    document.getElementById("schemaBtn"+id).classList.remove("active");
}
// =================================================
// SHOW SCHEMA
// =================================================
function showSchema(id){
    const box =document.getElementById("responseBox"+id);
    box.innerHTML =generateSchemaHtml(window["schema"+id]);
    document.getElementById("schemaBtn"+id).classList.add("active");
    document.getElementById("exampleBtn"+id).classList.remove("active");
}
// =================================================
// GENERATE SCHEMA HTML
// =================================================
function generateSchemaHtml(schema){
    if(!schema){
        return "No schema available";
    }
    let html = ` <h5>${schema.name}</h5><ul>`;
    Object.entries(schema.fields)
    .forEach(([field,type])=>{
        html += `<li><strong>${field} :</strong>${type}</li>`;
    });
    html += `</ul>`;
    return html;
}