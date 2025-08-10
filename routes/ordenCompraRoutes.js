const express = require('express');
const router = express.Router();
const { crearOrdenCompra, validarOrdenCompra, obtenerOrdenes } = require('../controllers/ordenCompraController');
const { crearOrdenDesdeEntrada } = require('../controllers/ordenCompraController');

// Crear una orden basada en una entrada
router.post('/crear-desde-entrada', crearOrdenDesdeEntrada);

// Rutas existentes
router.post('/', crearOrdenCompra);
router.put('/validar/:id', validarOrdenCompra);
router.get('/', obtenerOrdenes);

module.exports = router;
