const express = require('express');
const router = express.Router();

// Middlewares (ajusta si tienes autenticación y roles)
const { authMiddleware } = require('../../middleware/auth');
const { requireEmpleado } = require('../../middleware/roleMiddleware');

// Importar tus subrutas actuales
const entradaRoutes = require('./entradaRoutes');
const ordenCompraRoutes = require('./ordenCompraRoutes');
const conversionRoutes = require('./conversionRoutes');
const traspasoRoutes = require('./traspasoRoutes');
const ingresoDesdeReferenciaRoutes = require('./ingresoDesdeReferenciaRoutes');
const movimientoRoutes = require('./movimientoRoutes');
const salidaRoutes = require('./salidaRoutes');
const reportesRoutes = require('./reportesRoutes');
const informeRoutes = require('./informeRoutes');

// Aplicar middlewares (si quieres proteger todas las rutas de empleado)
router.use(authMiddleware);
router.use(requireEmpleado);

// Usar subrutas agrupadas
router.use('/entradas', entradaRoutes); // /api/empleado/entradas
router.use('/ordenes', ordenCompraRoutes); // /api/empleado/ordenes
router.use('/conversion', conversionRoutes); // /api/empleado/conversion
router.use('/traspasos', traspasoRoutes); // /api/empleado/traspasos
router.use('/ingreso-desde-referencia', ingresoDesdeReferenciaRoutes); // /api/empleado/ingreso-desde-referencia
router.use('/movimientos', movimientoRoutes); // /api/empleado/movimientos
router.use('/salidas', salidaRoutes); // /api/empleado/salidas
router.use('/reportes', reportesRoutes); // /api/empleado/reportes
router.use('/informe', informeRoutes); // /api/empleado/informe

module.exports = router;
