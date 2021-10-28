<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Address Book</title>
        <link rel="stylesheet" href="assets/bootstrap/node_modules/bootstrap/dist/css/bootstrap.min.css"/>
    </head>
    <body>
        <div>
            
        </div>
        <?php
        // put your code here
        ?>
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
//                             console.log(JSON.stringify(response.status));
                             console.log(response);
                            if(response.status === 1){
//                                window.location.href = '../';
                            }else{
                                console.log('invalid data');

                            }
                        },
                        error: function (error) {
                            console.log("error: " + JSON.stringify(error));
                        }
                    });
                }
            };
        </script>
    </body>
</html>
