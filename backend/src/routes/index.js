/**
 * Top-level router. Mounts all feature routers under /api.
 */
const express = require('express');
const router  = express.Router();

const { authenticate, requireRole } = require('../middleware/auth');
const auth      = require('../controllers/authController');
const delivery  = require('../controllers/deliveryController');
const dashboard = require('../controllers/dashboardController');
const message   = require('../controllers/messageController');
const lookup    = require('../controllers/driverController');

// ---- public ----
router.post('/auth/login', auth.login);

// ---- everything below requires a valid JWT ----
router.use(authenticate);

// Deliveries (controller-side endpoints restricted to Controleur)
router.get('/deliveries',           requireRole('Controleur'), delivery.listDeliveries);
router.get('/deliveries/today',     requireRole('Controleur'), delivery.todayDeliveries);
router.get('/deliveries/my-today',  requireRole('Livreur'),    delivery.myTodayDeliveries);
router.get('/deliveries/:nocde',                               delivery.getDeliveryDetail);
router.patch('/deliveries/:nocde/status',                      delivery.updateDeliveryStatus);

// Dashboard
router.get('/dashboard/by-driver', requireRole('Controleur'), dashboard.byDriver);
router.get('/dashboard/by-client', requireRole('Controleur'), dashboard.byClient);

// Messages
router.post('/messages/info',     requireRole('Controleur'), message.sendInfo);
router.post('/messages/urgence',  requireRole('Livreur'),    message.sendUrgence);
router.get('/messages',                                       message.inbox);

// Lookups
router.get('/drivers', requireRole('Controleur'), lookup.listDrivers);
router.get('/clients', requireRole('Controleur'), lookup.listClients);

module.exports = router;
