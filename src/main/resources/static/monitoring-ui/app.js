fetch('/api/v1/monitoring/apis')

.then(response => response.json())

.then(data => {


    const container = document.getElementById("container");


    data.forEach((controller,index)=>{


        let apisHtml = "";


        controller.apis.forEach((api,j)=>{


            apisHtml += `

            <div class="api-item">


                <div class="api-header ${api.httpMethod.toLowerCase()}"
                     data-bs-toggle="collapse"
                     data-bs-target="#api${index}${j}">


                    <span class="method">

                        ${api.httpMethod}

                    </span>


                    <span class="endpoint">

                        ${api.endpoint}

                    </span>


                </div>



                <div id="api${index}${j}"
                     class="collapse">


                    <div class="api-body">


                        <p>

                        <strong>
                        Java Method:
                        </strong>

                        ${api.javaMethod}

                        </p>


                    </div>


                </div>



            </div>

            `;


        });



        container.innerHTML += `



        <div class="accordion-item">


            <h2 class="accordion-header">


                <button class="accordion-button collapsed api-controller sub-title "

                type="button"

                data-bs-toggle="collapse"

                data-bs-target="#controller${index}">


                    ${controller.controller}


                </button>


            </h2>



            <div id="controller${index}"

                 class="accordion-collapse collapse">



                <div class="accordion-body">


                    ${apisHtml}


                </div>


            </div>



        </div>


        `;



    });



})

.catch(error=>{

    console.error(
        "Unable to load APIs",
        error
    );

});