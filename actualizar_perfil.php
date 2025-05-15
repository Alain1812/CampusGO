<?php
header('Content-Type: application/json');

$DB_SERVER = "localhost";
$DB_USER = "Xapedrueza002";
$DB_PASS = "evYvBefGYz";
$DB_DATABASE = "Xapedrueza002_dbgrupo";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

if (mysqli_connect_errno()) {
    echo json_encode(['error' => 'Error de conexión: ' . mysqli_connect_error()]);
    exit();
}

// Validar parámetros
if (!isset($_POST['usuario']) || !isset($_FILES['imagen'])) {
    echo json_encode(['error' => 'Datos incompletos']);
    exit();
}

$usuario = mysqli_real_escape_string($con, $_POST['usuario']);

// Subir imagen
$targetDir = "uploads/";
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

$fileName = uniqid() . '_' . basename($_FILES['imagen']['name']);
$targetPath = $targetDir . $fileName;

if (move_uploaded_file($_FILES['imagen']['tmp_name'], $targetPath)) {
    $query = "UPDATE Xapedrueza002_usuarios 
              SET imagen_perfil = '$fileName' 
              WHERE usuario = '$usuario'";
    
    if (mysqli_query($con, $query)) {
        echo json_encode([
            'success' => true,
            'imagen' => $fileName
        ]);
    } else {
        echo json_encode(['error' => 'Error en BD: ' . mysqli_error($con)]);
    }
} else {
    echo json_encode(['error' => 'Error al subir imagen']);
}

mysqli_close($con);
?>