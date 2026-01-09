const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const authMiddleware = require('../middlewares/authMiddleware'); 


/**
 * @swagger
 * tags:
 *   - name: Users
 *     description: จัดการผู้ใช้งาน (Authentication & Profile)
 */

/**
 * @swagger
 * /users/register:
 *   post:
 *     summary: สมัครสมาชิกใหม่ (Register)
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
 *               - email
 *               - password
 *               - nickname
 *               - role
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *                 example: mom@example.com
 *               password:
 *                 type: string
 *                 format: password
 *                 example: password123
 *               nickname:
 *                 type: string
 *                 example: แม่สมศรี
 *               role:
 *                 type: string
 *                 enum: [parent, child]
 *                 example: parent
 *               device_id:
 *                 type: string
 *                 example: device_abc_123
 *               bank_account_number:
 *                 type: string
 *                 example: 123-4-56789-0
 *     responses:
 *       201:
 *         description: สมัครสมาชิกสำเร็จ
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
 *                       example: แม่สมศรี
 *                     email:
 *                       type: string
 *                       example: mom@example.com
 *                     role:
 *                       type: string
 *                       example: parent
 *                     status:
 *                       type: string
 *                       example: normal
 *       400:
 *         description: ข้อมูลไม่ครบ หรือ อีเมลซ้ำ
 *         content:
 *           application/json:
 *             examples:
 *               missing_field:
 *                 summary: กรอกข้อมูลไม่ครบ
 *                 value:
 *                   error: Please provide all required fields
 *               duplicate_email:
 *                 summary: อีเมลซ้ำ
 *                 value:
 *                   error: Email already exists
 *       500:
 *         description: เกิดข้อผิดพลาดที่ Server
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Registration failed
 *                 details:
 *                   type: string
 */


router.post('/register', userController.register)



/**
 * @swagger
 * /users/login:
 *   post:
 *     summary: เข้าสู่ระบบ (Login)
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
 *               - email
 *               - password
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *                 example: mom@example.com
 *               password:
 *                 type: string
 *                 format: password
 *                 example: password123
 *     responses:
 *       200:
 *         description: เข้าสู่ระบบสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Login successful
 *                 token:
 *                   type: string
 *                   example: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *                 user:
 *                   type: object
 *                   properties:
 *                     user_id:
 *                       type: integer
 *                       example: 1
 *                     email:
 *                       type: string
 *                       example: mom@example.com
 *                     nickname:
 *                       type: string
 *                       example: แม่สมศรี
 *                     role:
 *                       type: string
 *                       example: parent
 *                     family_id:
 *                       type: integer
 *                       nullable: true
 *                       example: null
 *                     bank_account_number:
 *                       type: string
 *                       example: 123-4-56789-0
 *                     status:
 *                       type: string
 *                       example: normal
 *       401:
 *         description: อีเมลหรือรหัสผ่านไม่ถูกต้อง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Invalid email or password
 *       500:
 *         description: Server Error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Login failed
 */

router.post('/login', userController.login);



/**
 * @swagger
 * /users/fcm-token:
 *   post:
 *     summary: อัปเดต FCM Token สำหรับรับแจ้งเตือน
 *     tags:
 *       - Users
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - token
 *             properties:
 *               token:
 *                 type: string
 *                 example: fcm_token_string_from_firebase...
 *     responses:
 *       200:
 *         description: บันทึก Token สำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: FCM Token updated successfully
 *       401:
 *         description: Authentication failed
 *       404:
 *         description: User not found
 */

// Update FCM Token (Require Login) --> ต้องเอาไว้ก่อน /:user_id เพื่อกันสับสน
router.post('/fcm-token', authMiddleware, userController.updateFcmToken);



/**
 * @swagger
 * /users/{user_id}:
 *   get:
 *     summary: ดูข้อมูลส่วนตัว (Full Profile)
 *     tags:
 *       - Users
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *           example: 1
 *     responses:
 *       200:
 *         description: ข้อมูลผู้ใช้งาน
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
 *                   example: แม่สมศรี
 *                 email:
 *                   type: string
 *                   example: mom@example.com
 *                 role:
 *                   type: string
 *                   example: parent
 *                 family_id:
 *                   type: integer
 *                   nullable: true
 *                   example: 1
 *                 device_id:
 *                   type: string
 *                   example: device_abc
 *                 bank_account_number:
 *                   type: string
 *                   example: 123-4-56789-0
 *                 fcm_token:
 *                   type: string
 *                   nullable: true
 *                 createdAt:
 *                   type: string
 *                   format: date-time
 *                 updatedAt:
 *                   type: string
 *                   format: date-time
 *                 status:
 *                   type: string
 *                   example: normal
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
 *     summary: ลบผู้ใช้งาน
 *     tags:
 *       - Users
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: ลบสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: User deleted successfully
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

router.get('/:user_id', authMiddleware , userController.getUserInfo);
router.delete('/:user_id', authMiddleware,  userController.deleteUser);


/**
 * @swagger
 * /users/status:
 *   put:
 *     summary: Update user status
 *     description: >
 *       ใช้สำหรับเปลี่ยนสถานะของผู้ใช้  
 *       - สถานะที่อนุญาต: allow, reject, waiting, normal  
 *       - ผู้ใช้ที่มี role = child จะถูกบังคับให้เป็น status = normal เท่านั้น
 *     tags: [Users]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - user_id
 *               - status
 *             properties:
 *               user_id:
 *                 type: integer
 *                 example: 5
 *               status:
 *                 type: string
 *                 enum: [allow, reject, waiting, normal]
 *                 example: allow
 *     responses:
 *       200:
 *         description: Status updated successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Status updated successfully
 *                 user:
 *                   type: object
 *                   properties:
 *                     user_id:
 *                       type: integer
 *                       example: 5
 *                     nickname:
 *                       type: string
 *                       example: เด็กชายสมชาย
 *                     role:
 *                       type: string
 *                       example: parent
 *                     status:
 *                       type: string
 *                       example: allow
 *
 *       400:
 *         description: Invalid input or invalid status value
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Invalid status value
 *
 *       403:
 *         description: Forbidden - Cannot change child user status
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Cannot change status of a 'child' user. They must remain 'normal'.
 *
 *       404:
 *         description: User not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User not found
 *
 *       500:
 *         description: Internal Server Error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Internal Server Error
 */


router.put('/status', authMiddleware, userController.updateUserStatus);

module.exports = router;