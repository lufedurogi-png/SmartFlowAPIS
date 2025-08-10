const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const entradaRoutes = require('./routes/entradaRoutes');
const ordenCompraRoutes = require('./routes/ordenCompraRoutes');
const conversionRoutes = require('./routes/conversionRoutes');
const traspasoRoutes = require('./routes/traspasoRoutes');
const ingresoDesdeReferenciaRoutes = require('./routes/ingresoDesdeReferenciaRoutes');
const movimientoRoutes = require('./routes/movimientoRoutes');
const salidaRoutes = require('./routes/salidaRoutes');
const reportesRoutes = require('./routes/reportesRoutes');
const informeRoutes = require('./routes/informeRoutes');

// Importar rutas
const authRoutes = require('./routes/auth');
require('dotenv').config();

// Importar rutas específicas por rol
const auditorRoutes = require('./routes/auditor/auditorRoutes');
const adminRoutes = require('./routes/admin/adminRoutes');

const app = express();

// Configuración de seguridad
app.use(helmet());

// Configuración de CORS
const corsOptions = {
  origin: process.env.ALLOWED_ORIGINS ? process.env.ALLOWED_ORIGINS.split(',') : true,
  credentials: true,
  optionsSuccessStatus: 200
};
app.use(cors(corsOptions));

// Limitador de velocidad
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutos
  max: 100, // límite de 100 solicitudes por IP
  message: {
    error: 'Demasiadas solicitudes desde esta IP, intenta de nuevo más tarde.'
  }
});
app.use('/api/', limiter);

// Middleware para parsear JSON
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Conexión a MongoDB
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
})
.then(() => {
  console.log('✅ Conectado a MongoDB exitosamente');
})
.catch((error) => {
  console.error('❌ Error conectando a MongoDB:', error);
  process.exit(1);
});

// 🔄 Registrar modelos para evitar MissingSchemaError al hacer populate()
require('./models/proveedor');
require('./models/User');
require('./models/perfume');
require('./models/ordenCompra');
require('./models/traspaso');
require('./models/almacen');
require('./models/Salida');

// Rutas principales
app.get('/', (req, res) => {
  res.json({
    message: 'SmartFlow API - Funcionando correctamente',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// Ruta para verificar archivos (solo para debugging)
app.get('/debug/files', (req, res) => {
  const fs = require('fs');
  const path = require('path');
  
  try {
    const uploadsPath = path.join(__dirname, 'uploads', 'users');
    
    // Verificar si existe el directorio
    if (!fs.existsSync(uploadsPath)) {
      return res.json({
        error: 'Directorio uploads/users no existe',
        path: uploadsPath
      });
    }
    
    // Listar archivos
    const files = fs.readdirSync(uploadsPath);
    res.json({
      directory: uploadsPath,
      files: files,
      totalFiles: files.length
    });
  } catch (error) {
    res.json({
      error: 'Error listando archivos',
      message: error.message
    });
  }
});

app.use('/api/informe', informeRoutes);

app.use('/reportes', reportesRoutes);

app.use('/api/salidas', salidaRoutes);

app.use('/api/movimientos', movimientoRoutes);

app.use('/api', ingresoDesdeReferenciaRoutes);

// Conertir documento de entradas a ordenes_compra
app.use('/api', conversionRoutes);

// Rutas de la API
app.use('/api/auth', authRoutes);

// Rutas específicas por rol
app.use('/api/auditor', auditorRoutes);
app.use('/api/admin', adminRoutes);

// Servir archivos estáticos (imágenes)
app.use('/uploads', express.static('uploads'));

app.use('/api/traspasos', traspasoRoutes);

// Middleware de manejo de errores
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    error: 'Algo salió mal en el servidor',
    message: process.env.NODE_ENV === 'development' ? err.message : 'Error interno del servidor'
  });
});

app.use('/api', entradaRoutes);
app.use('/api/ordenes', ordenCompraRoutes);

// Manejo de rutas no encontradas
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Ruta no encontrada',
    message: `La ruta ${req.originalUrl} no existe`
  });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  console.log(`🌐 API disponible en: http://localhost:${PORT}`);
  console.log(`🌍 Producción en Render: https://smartflow-mwmm.onrender.com`);
});

module.exports = app;
