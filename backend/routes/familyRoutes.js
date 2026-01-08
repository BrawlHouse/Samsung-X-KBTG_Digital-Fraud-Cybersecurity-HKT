const express = require('express');
const router = express.Router();
const familyController = require('../controllers/familyController');
const authMiddleware = require('../middlewares/authMiddleware');

router.use(authMiddleware);


/**
 * @swagger
 * tags:
 *   name: Family
 *   description: Family management (create, join, members, remove)
 */


/**
 * @swagger
 * /family/create:
 *   post:
 *     summary: Create a new family
 *     description: Create a family and assign the current user as a member
 *     tags: [Family]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       201:
 *         description: Family created successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Family created successfully
 *                 family_id:
 *                   type: integer
 *                   example: 1
 *                 invite_code:
 *                   type: string
 *                   example: A1B2C3
 *                 created_at:
 *                   type: string
 *                   example: 2026-01-06T10:00:00Z
 *                 updated_at:
 *                   type: string
 *                   example: 2026-01-06T10:00:00Z
 *       401:
 *         description: Unauthorized (missing or invalid token)
 *       500:
 *         description: Internal server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 */

// 1. สร้างครอบครัว (POST /family/create)
// Header: Authorization: Bearer <token>
router.post('/create', authMiddleware, familyController.createFamily);




/**
 * @swagger
 * /family/join:
 *   post:
 *     summary: Join a family using invite code
 *     tags: [Family]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - invite_code
 *             properties:
 *               invite_code:
 *                 type: string
 *                 example: A1B2C3
 *     responses:
 *       200:
 *         description: Joined family successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Joined family successfully
 *                 family:
 *                   type: object
 *                   properties:
 *                     family_id:
 *                       type: integer
 *                       example: 1
 *                     invite_code:
 *                       type: string
 *                       example: A1B2C3
 *       404:
 *         description: Invalid invite code
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Invalid invite code
 *       401:
 *         description: Unauthorized
 *       500:
 *         description: Internal server error
 */

// 2. เข้าร่วมครอบครัว (POST /family/join)
// Body: { "invite_code": "XXXXXX" }
router.post('/join', authMiddleware, familyController.joinFamily);




/**
 * @swagger
 * /family/members:
 *   get:
 *     summary: Get members of current user's family
 *     tags: [Family]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Family members retrieved successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 family_id:
 *                   type: integer
 *                   example: 1
 *                 invite_code:
 *                   type: string
 *                   example: A1B2C3
 *                 members:
 *                   type: array
 *                   items:
 *                     type: object
 *                     properties:
 *                       user_id:
 *                         type: integer
 *                         example: 10
 *                       nickname:
 *                         type: string
 *                         example: Mom
 *                       role:
 *                         type: string
 *                         example: parent
 *                       device_id:
 *                         type: string
 *                         example: device_abc_123
 *                       email:
 *                         type: string
 *                         example: mom@example.com
 *                       bank_account_number:
 *                         type: string
 *                         example: 123-4-56789-0
 *       400:
 *         description: User is not in a family
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: You are not in a family
 *       404:
 *         description: Family not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Family not found
 *       401:
 *         description: Unauthorized
 *       500:
 *         description: Internal server error
 */

// 3. ดูสมาชิกในครอบครัว (GET /family/members)
// ไม่ต้องส่ง param อะไรมา ระบบจะดูจาก Token เองว่าอยู่บ้านไหน
router.get('/members', authMiddleware, familyController.getMembers);




/**
 * @swagger
 * /family/members/{user_id}:
 *   delete:
 *     summary: Remove a member from the family
 *     tags: [Family]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *         example: 5
 *     responses:
 *       200:
 *         description: Member removed successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Member removed successfully
 *       400:
 *         description: User is not in a family
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: You are not in a family
 *       403:
 *         description: Forbidden (no permission or different family)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Cannot remove user from another family
 *       404:
 *         description: Target user not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User not found
 *       401:
 *         description: Unauthorized
 *       500:
 *         description: Internal server error
 */

// 4. ลบสมาชิก (DELETE /family/members/:user_id)
// ส่ง ID ของคนที่จะลบไปใน URL
router.delete('/members/:user_id', authMiddleware, familyController.removeMember);





/**
 * @swagger
 * /members/child:
 *   get:
 *     summary: Get all children in the same family
 *     description: >
 *       ดึงรายชื่อสมาชิกที่มี role เป็น `child` ภายในครอบครัวเดียวกัน  
 *       โดยอ้างอิง family_id จากผู้ใช้งานที่ login อยู่ (JWT Required)
 *     tags:
 *       - Family
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Successfully retrieved children in the family
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 family_id:
 *                   type: integer
 *                   example: 101
 *                 users:
 *                   type: array
 *                   items:
 *                     type: object
 *                     properties:
 *                       user_id:
 *                         type: integer
 *                         example: 10
 *                       nickname:
 *                         type: string
 *                         example: Ninja
 *                       email:
 *                         type: string
 *                         example: ninja@email.com
 *                       role:
 *                         type: string
 *                         example: child
 *       401:
 *         description: Unauthorized (missing or invalid JWT)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Unauthorized
 *       404:
 *         description: User or family not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User or Family not found
 *       500:
 *         description: Internal server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Server Error
 */

router.get('/members/child', authMiddleware, familyController.getFamilyChildren);

module.exports = router;