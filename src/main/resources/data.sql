-- Insertar usuarios de prueba
INSERT INTO Usuario (username, email, password, esta_autenticado) VALUES 
('admin', 'admin@recetas.com', 'password', true),
('user', 'user@recetas.com', 'password', true),
('chef', 'chef@recetas.com', 'password', true);

-- Insertar recetas de prueba
INSERT INTO Receta (titulo, tipo_cocina, pais_origen, dificultad, tiempo_coccion, instrucciones, popularidad, fecha_publicacion) VALUES 
(
    'Tacos al Pastor', 
    'Mexicana', 
    'México', 
    'MEDIA', 
    45, 
    'Marinar la carne de cerdo con especias y piña. Asar en trompo. Servir en tortillas con cebolla, cilantro y salsa.',
    4.5,
    '2024-03-15'
),
(
    'Paella Valenciana', 
    'Española', 
    'España', 
    'ALTA', 
    60, 
    'Preparar sofrito con verduras. Añadir arroz, caldo y azafrán. Incorporar pollo, conejo y judías verdes. Cocinar sin revolver.',
    4.8,
    '2024-03-10'
),
(
    'Pasta Carbonara', 
    'Italiana', 
    'Italia', 
    'BAJA', 
    20, 
    'Cocinar pasta al dente. En sartén, dorar panceta. Mezclar huevos con queso. Combinar todo fuera del fuego.',
    4.6,
    '2024-03-20'
),
(
    'Sushi California Roll', 
    'Japonesa', 
    'Japón', 
    'ALTA', 
    90, 
    'Preparar arroz de sushi. Colocar nori, arroz, aguacate y pepino. Enrollar con cangrejo. Cubrir con sésamo.',
    4.7,
    '2024-03-12'
),
(
    'Ceviche Peruano', 
    'Peruana', 
    'Perú', 
    'MEDIA', 
    30, 
    'Cortar pescado en cubos. Marinar con limón, sal y ají. Añadir cebolla roja, cilantro y camote.',
    4.9,
    '2024-03-18'
);

-- Insertar ingredientes para Tacos al Pastor
INSERT INTO receta_ingredientes (receta_id_receta, nombre, cantidad, unidad_medida) VALUES
(1, 'Carne de cerdo', 1.0, 'kg'),
(1, 'Piña', 0.5, 'pza'),
(1, 'Tortillas', 20.0, 'pzas'),
(1, 'Cebolla', 1.0, 'pza'),
(1, 'Cilantro', 0.1, 'kg');

-- Insertar ingredientes para Paella Valenciana
INSERT INTO receta_ingredientes (receta_id_receta, nombre, cantidad, unidad_medida) VALUES
(2, 'Arroz bomba', 300.0, 'g'),
(2, 'Pollo', 1.0, 'pza'),
(2, 'Conejo', 0.5, 'pza'),
(2, 'Judías verdes', 200.0, 'g'),
(2, 'Azafrán', 0.5, 'g');

-- Insertar ingredientes para Pasta Carbonara
INSERT INTO receta_ingredientes (receta_id_receta, nombre, cantidad, unidad_medida) VALUES
(3, 'Pasta', 400.0, 'g'),
(3, 'Panceta', 200.0, 'g'),
(3, 'Huevos', 4.0, 'pzas'),
(3, 'Queso pecorino', 100.0, 'g'),
(3, 'Pimienta negra', 1.0, 'cdta');

-- Insertar imágenes de ejemplo
INSERT INTO receta_imagenes (receta_id_receta, imagen_url) VALUES
(1, 'https://example.com/tacos1.jpg'),
(1, 'https://example.com/tacos2.jpg'),
(2, 'https://example.com/paella1.jpg'),
(3, 'https://example.com/carbonara1.jpg'),
(4, 'https://example.com/sushi1.jpg'),
(5, 'https://example.com/ceviche1.jpg');