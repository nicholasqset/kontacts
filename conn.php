<?php
ini_set('display_errors', 1);

$servername = "localhost";
$username = "kontacts";
$password = "kontacts";
$dbname = "kontacts";

try {
  $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
  // set the PDO error mode to exception
  $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
  
//  var_dump($conn);
 
} catch(PDOException $e) {
  echo $e->getMessage();
}

