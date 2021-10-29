<?php
ini_set('display_errors', 1);

$host       = "localhost";
$username   = "kontacts";
$password   = "kontacts";
$dbname     = "kontacts";

try {
    $dsn = "mysql:host=$host;dbname=$dbname;charset=UTF8";

    $pdo = new PDO($dsn, $username, $password);
    
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);  

//    if ($pdo) {
//            echo "Connected to the $dbname database successfully!";
//    }
 
} catch(PDOException $e) {
    echo $e->getMessage();
}

