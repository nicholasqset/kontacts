<?php
ini_set('display_errors', 1);

require_once '../conn.php';

class AddressBook{
    
    public function listContacts(){
        global $pdo;
        
        $data = array();
        
        $sql = 'SELECT `id`,`name`,`first_name`,`email`,`street`,`zip_code`,`city` FROM `address_book`';
        $statement = $pdo->query($sql);
        
        if($statement->rowCount() > 0){
            $table_data = array();
            
            $count = 1;
            while (($row = $statement->fetch(PDO::FETCH_ASSOC)) !== false) {
                $id         = $row['id'];
                $name       = $row['name'];
                $first_name = $row['first_name'];
                $email      = $row['email'];
                $street     = $row['street'];
                $zip_code   = $row['zip_code'];
                $city       = $row['city'];
                
                $table_data[$count]['id'] = $id;
                $table_data[$count]['name'] = $name;
                $table_data[$count]['first_name'] = $first_name;
                $table_data[$count]['email'] = $email;
                $table_data[$count]['street'] = $street;
                $table_data[$count]['zip_code'] = $zip_code;
                $table_data[$count]['city'] = $city;
                
                $count++;

            }
            
            $data['data'] = $table_data;
            $data['success'] = 1;
            $data['message'] = 'success';
        }else{
            $data['success'] = 0;
            $data['message'] = 'no record found';
        }
        
//        
        return json_encode($data);
    }
}

$addressBook = new AddressBook();

$function = filter_input(INPUT_GET, 'function');
//echo 'funx='.$function;

if($function == 'listContacts'){
    
   echo $addressBook->listContacts();
    
}

