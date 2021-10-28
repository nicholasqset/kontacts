<?php
require_once '../conn.php';
var_dump($conn);
class AddressBook{
    
    public function listContacts(){
        $data = array();
        
        return $data;
    }
}

$addressBook = new AddressBook();

$function = filter_input(INPUT_POST, 'function');

if($function == 'listContacts'){
    
    echo $addressBook->listContacts();
    
}

