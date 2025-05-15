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
    echo json_encode([
        'success' => false,
        'error' => 'Error de conexión: ' . mysqli_connect_error()
    ]);
    exit();
}

// Validar parámetro usuario
if (!isset($_POST['usuario'])) {
    echo json_encode([
        'success' => false,
        'error' => 'Usuario no proporcionado'
    ]);
    exit();
}

// Escapar entrada
$usuario = mysqli_real_escape_string($con, $_POST['usuario']);

// Consulta para obtener datos del perfil
$query = "SELECT usuario, imagen_perfil 
          FROM Xapedrueza002_usuarios 
          WHERE usuario = '$usuario'";

$result = mysqli_query($con, $query);

if (!$result) {
    echo json_encode([
        'success' => false,
        'error' => 'Error en la consulta: ' . mysqli_error($con)
    ]);
    exit();
}

if (mysqli_num_rows($result) > 0) {
    $row = mysqli_fetch_assoc($result);
    
    $response = [
        'success' => true,
        'usuario' => $row['usuario'],
        'imagen' => $row['imagen_perfil'] ?? null
    ];
    
    echo json_encode($response);
} else {
    echo json_encode([
        'success' => false,
        'error' => 'Usuario no encontrado'
    ]);
}

// Cerrar conexión
mysqli_close($con);
?>