<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Address Book</title>
        <link rel="stylesheet" href="assets/bootstrap/node_modules/bootstrap/dist/css/bootstrap.min.css"/>
    </head>
    <body class="container-fluid">
        <h4 class="text-center font-weight-bold">Address Book</h4>
        <button type="button" class="btn btn-primary" onclick="kontact.addKontact();">Add New Contact</button>
               
        <div id="dv_kontacts">
            <table class="table" id="tbl_kontacts">
                <tr>
                    <th>#</th>
                    <th>Name</th>
                    <th>First Name</th>
                    <th>Email</th>
                    <th>Street</th>
                    <th>Zip Code</th>
                    <th>City</th>
                    <th>Delete</th>
                </tr>
            </table>
        </div>
        <script src="assets/jquery/node_modules/jquery/dist/jquery.min.js"></script>
        <script type="text/javascript">
            $(function() {
                kontact.init();
            });
            
            let kontact = {
                init: function(){
                    $.ajax({
                        url: './parser/?function=listContacts',
                        type: "POST",
                        success: function (response) {
                            response = JSON.parse(response);
                            if(response.success === 1){
                                for (let [key, value] of Object.entries(response.data)) {
//                                    console.log(key);
//                                    console.log(value);
                                    let purgeId = 0;
                                    let table = '<tr>'; 
                                    table += '<td>' + key + '</td>';
                                    for (let [key2, value2] of Object.entries(value)) {
//                                        console.log('key='+key);
//                                        console.log('key2='+key2);
//                                        console.log('value2='+value2);
                                        
                                        if(key2 === 'id'){
                                            purgeId = value2;
                                        }else{
                                            table += '<td>' + value2 + '</td>';
                                        }
                                        
                                    }
                                    
                                    let delOpt = "<a href='javascript:void(0);' onclick=\"kontact.deleteKontact("+purgeId+")\">delete</a>";
                                    
                                    table += '<td>' + delOpt + '</td>';
                                    table += '</tr>'; 
                                    
//                                    console.log(table);
                                    
                                    $('#tbl_kontacts').append(table);                        
                                }
                            }else{
                                console.log('invalid data');
                            }
                        },
                        error: function (error) {
                            console.log("error: " + JSON.stringify(error));
                        }
                    });
                },
                addKontact: function(){
//                    console.log('adding...');
                    
                    let html = '';
                    
                    html += "<form name=\"frm_kontact\" id=\"frm_kontact\" method=\"post\" action=\"void%200\" onsubmit=\"javascript:return false;\">";
                        html += "<div class=\"form-group\">";
                            html += "<label for=\"txt_full_name\">Full Name</label>";
                            html += "<input type=\"text\" class=\"form-control\" id=\"txt_full_name\" placeholder=\"Enter Full Name\" required>";
                        html += "</div>";

                        html += "<div class=\"form-group\">";
                            html += "<label for=\"txt_first_name\">First Name</label>";
                            html += "<input type=\"text\" class=\"form-control\" id=\"txt_first_name\" placeholder=\"Enter First Name\" required>";
                        html += "</div>";

                        html += "<div class=\"form-group\">";
                            html += "<label for=\"txt_email\">Email address</label>";
                            html += "<input type=\"email\" class=\"form-control\" id=\"txt_email\" placeholder=\"Enter email\" required>";
                        html += "</div>";

                        html += "<div class=\"form-group\">";
                            html += "<label for=\"txt_street\">Street Address</label>";
                            html += "<input type=\"text\" class=\"form-control\" id=\"txt_street\" placeholder=\"Enter Street Address\" required>";
                        html += "</div>";

                        html += "<div class=\"form-group\">";
                            html += "<label for=\"txt_zip_code\">Zip Code</label>";
                            html += "<input type=\"text\" class=\"form-control\" id=\"txt_zip_code\" placeholder=\"Enter Zip Code\" required>";
                        html += "</div>";

                        html += "<div class=\"form-group\">";
                            html += "<label for=\"cmb_city\">City</label>";
                            html += "<select class=\"form-control\" id=\"cmb_city\" required>";

                            html += "</select>";
                        html += "</div>";

                        html += "<hr>";

                        html += "<button type=\"button\" class=\"btn btn-primary\" onclick=\"kontact.saveKontact();\">Submit</button>";
                        html += "&nbsp";
                        html += "<button type=\"button\" class=\"btn btn-warning\" onclick=\"kontact.reload();\">Cancel</button>";
                    html += "</form>";
                    
                    $('#dv_kontacts').html(html);
                    
                    kontact.getCities();
                },
                getCities: function(){
                    $.ajax({
                        url: './parser/?function=getCities',
                        type: "POST",
                        success: function (response) {
                            response = JSON.parse(response);
                            if(response.success === 1){
                                for (let [key, value] of Object.entries(response.data)) {
                                    let cities = '';
                                    for (let [key2, value2] of Object.entries(value)) {
//                                        console.log(value2);
                                        $('#cmb_city').append('<option>'+ value2+ '</option>');
                                    }
                                }
                            }else{
                                console.log('invalid data');
                            }
                        },
                        error: function (error) {
                            console.log("error: " + JSON.stringify(error));
                        }
                    });
                },
                saveKontact: function(){
//                    let data = $('#frm_kontact').serialize();
//                    console.log(data);
                    let name = $('#txt_full_name').val();
                    let first_name = $('#txt_first_name').val();
                    let email = $('#txt_email').val();
                    let street = $('#txt_street').val();
                    let zip_code = $('#txt_zip_code').val();
                    let city = $('#cmb_city').val();
                    
                    let data = 'name='+name+'&first_name='+first_name+'&email='+email+'&street='+street+'&zip_code='+zip_code+'&city='+city;
                    $.ajax({
                        url: './parser/?function=saveKontact&'+ data,
                        type: "POST",
                        success: function (response) {
                            response = JSON.parse(response);
                            if(response.success === 1){
                                alert('saving successfully occured');
                                kontact.reload();
                            }else{
                                console.log('error');
                                alert('error');                                
                            }
                        },
                        error: function (error) {
                            console.log("error: " + JSON.stringify(error));
                        }
                    });
                },
                deleteKontact: function(id){
                    if(confirm('Delete?')){
                        $.ajax({
                            url: './parser/?function=deleteKontact&id='+ id,
                            type: "POST",
                            success: function (response) {
                                response = JSON.parse(response);
                                if(response.success === 1){
                                    alert('deleting successfully occured');
                                    kontact.reload();
                                }else{
                                    console.log('error');
                                    alert('error');                                
                                }
                            },
                            error: function (error) {
                                console.log("error: " + JSON.stringify(error));
                            }
                        });
                    }
                },
                reload: function(){
                    console.log('reloading...');
                    window.location.reload();

                }
            };
        </script>
    </body>
</html>
