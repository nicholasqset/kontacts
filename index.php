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
                    <th>Edit</th>
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
                                    let table = '<tr>'; 
                                    for (let [key2, value2] of Object.entries(value)) {
//                                        console.log('key='+key);
//                                        console.log('key2='+key2);
//                                        console.log('value2='+value2);
                                        
                                        table += '<td>' + value2 + '</td>'
                                    }
                                    let editOpt = "<a href='#'>edit</a>";
                                    let delOpt = "<a href='#'>delete</a>";
                                    table += '<td>' + editOpt + '</td>'
                                    table += '<td>' + delOpt + '</td>'
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
                    console.log('adding...');
                    $('#dv_kontacts').html('zzzzzzz');
                }
            };
        </script>
    </body>
</html>
