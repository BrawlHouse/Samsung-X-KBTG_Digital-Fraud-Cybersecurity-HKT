const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

/**
 * @swagger
 * tags:
 *   - name: Users
 *     description: จัดการผู้ใช้งาน (Authentication)
 */

/**
 * @swagger
 * /users:
 *   post:
 *     summary: ลงทะเบียนเข้าใช้งานครั้งแรก (Register / Login)
 *     tags:
 *       - Users
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - nickname
 *               - device_id
 *               - role
 *             properties:
 *               nickname:
 *                 type: string
 *                 example: คุณแม่สมศรี
 *               device_id:
 *                 type: string
 *                 example: device_abc_123
 *               role:
 *                 type: string
 *                 enum: [parent, child]
 *                 example: parent
 *     responses:
 *       201:
 *         description: ลงทะเบียนสำเร็จ ได้รับ Token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: User registered successfully
 *                 user:
 *                   type: object
 *                   properties:
 *                     user_id:
 *                       type: integer
 *                       example: 1
 *                     nickname:
 *                       type: string
 *                       example: คุณแม่สมศรี
 *                     role:
 *                       type: string
 *                       example: parent
 *                     family_id:
 *                       type: integer
 *                       nullable: true
 *                       example: null
 *                     device_id:
 *                       type: string
 *                       example: device_abc_123
 *                     token:
 *                       type: string
 *                       description: JWT Token สำหรับใช้ยิง API อื่น
 *       400:
 *         description: ข้อมูลไม่ครบ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Please provide nickname, device_id, and role
 */

/**
 * @swagger
 * /users/{user_id}:
 *   get:
 *     summary: ดูข้อมูลส่วนตัว (Get User Info)
 *     tags:
 *       - Users
 *     security: []
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *         example: 1
 *     responses:
 *       200:
 *         description: พบข้อมูลผู้ใช้งาน
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 user_id:
 *                   type: integer
 *                   example: 1
 *                 nickname:
 *                   type: string
 *                   example: คุณแม่สมศรี
 *                 created_at:
 *                   type: string
 *                   format: date-time
 *                   example: 2024-01-05T12:00:00.000Z
 *                 device_id:
 *                   type: string
 *                   example: device_abc_123
 *       404:
 *         description: ไม่พบผู้ใช้งาน
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User not found
 *
 *   delete:
 *     summary: ลบผู้ใช้งานออกจากระบบ (Delete User)
 *     tags:
 *       - Users
 *     security: []
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *         example: 1
 *     responses:
 *       200:
 *         description: ลบผู้ใช้สำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: User 1 deleted successfully
 *       404:
 *         description: ไม่พบผู้ใช้งาน
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User not found
 */

router.post('/', userController.register);
router.get('/user_id', userController.getUserInfo);
router.delete('/user_id', userController.deleteUser);

module.exports = router;