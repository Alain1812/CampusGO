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

// Escapar parámetros
$usuario = mysqli_real_escape_string($con, $_POST['usuario']);
$contrasena = $_POST['contrasena'];

// Buscar usuario en la base de datos
$query = "SELECT contrasena FROM Xapedrueza002_usuarios WHERE usuario = '$usuario'";
$result = mysqli_query($con, $query);

if (!$result) {
    echo json_encode(['error' => 'Error en la consulta: ' . mysqli_error($con)]);
    exit();
}

if (mysqli_num_rows($result) === 0) {
    echo json_encode(['error' => 'Usuario no encontrado']);
    exit();
}

$fila = mysqli_fetch_assoc($result);
$hash_almacenado = $fila['contrasena'];

// Verificar contraseña
if (password_verify($contrasena, $hash_almacenado)) {
    $query_id = "SELECT id FROM Xapedrueza002_usuarios WHERE usuario = '$usuario'";
    $result_id = mysqli_query($con, $query_id);
    $row = mysqli_fetch_assoc($result_id);
    
    echo json_encode([
        'success' => true,
        'message' => 'Inicio de sesión exitoso',
        'user_id' => $row['id'] 
    ]);
} else {
    echo json_encode(['error' => 'Contraseña incorrecta']);
}

mysqli_close($con);
?>