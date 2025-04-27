<?php
header('Content-Type: application/json');

// Configuración de la base de datos
$DB_SERVER = "localhost";
$DB_USER = "Xapedrueza002";
$DB_PASS = "evYvBefGYz";
$DB_DATABASE = "Xapedrueza002_dbgrupo";

// Establecer conexión
$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);
if (mysqli_connect_errno()) {
    echo json_encode(['error' => 'Error de conexión: ' . mysqli_connect_error()]);
    exit();
}

// Validar parámetros
if (!isset($_POST['usuario']) || !isset($_POST['contrasena'])) {
    echo json_encode(['error' => 'Datos incompletos']);
    exit();
}

// Escapar y hashear
$usuario = mysqli_real_escape_string($con, $_POST['usuario']);
$contrasena = password_hash($_POST['contrasena'], PASSWORD_DEFAULT);

// Insertar en la base de datos
$query = "INSERT INTO Xapedrueza002_usuarios (usuario, contrasena) VALUES ('$usuario', '$contrasena')";
if (mysqli_query($con, $query)) {
    echo json_encode(['success' => true, 'message' => 'Usuario registrado']);
} else {
    echo json_encode(['error' => 'Error: ' . mysqli_error($con)]);
}

mysqli_close($con);
?>