const express = require('express');
const router = express.Router();
const familyController = require('../controllers/familyController');
const authMiddleware = require('../middlewares/authMiddleware');

router.use(authMiddleware);

/**
 * @swagger
 * tags:
 *   - name: Family
 *     description: ระบบจัดการครอบครัว
 */

/**
 * @swagger
 * /family:
 *   post:
 *     summary: สร้างครอบครัวใหม่
 *     tags: [Family]
 *     responses:
 *       201:
 *         description: สร้างครอบครัวสำเร็จ
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
 *                   example: 101
 *       400:
 *         description: ผู้ใช้นี้มีครอบครัวอยู่แล้ว
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User already belongs to a family
 */
router.post('/', familyController.createFamily);

/**
 * @swagger
 * /family/{user_id}:
 *   get:
 *     summary: ดู Family ID ของ User
 *     tags: [Family]
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID ของผู้ใช้ที่ต้องการดูข้อมูล
 *     responses:
 *       200:
 *         description: พบข้อมูลครอบครัว
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 user_id:
 *                   type: integer
 *                   example: 1
 *                 family_id:
 *                   type: integer
 *                   example: 101
 *       404:
 *         description: ไม่พบผู้ใช้ หรือ ไม่มีครอบครัว
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User has no family
 */
router.get('/:user_id', familyController.getFamilyInfo);

/**
 * @swagger
 * /family/{family_id}/members:
 *   get:
 *     summary: ดูรายชื่อสมาชิกในครอบครัว
 *     tags: [Family]
 *     parameters:
 *       - in: path
 *         name: family_id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID ของครอบครัว
 *     responses:
 *       200:
 *         description: รายชื่อสมาชิกในครอบครัว
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 family_id:
 *                   type: integer
 *                   example: 101
 *                 members:
 *                   type: array
 *                   items:
 *                     type: object
 *                     properties:
 *                       user_id:
 *                         type: integer
 *                         example: 2
 *                       nickname:
 *                         type: string
 *                         example: ลูกชาย
 *                       role:
 *                         type: string
 *                         example: child
 *                       device_id:
 *                         type: string
 *                         example: device_xyz_456
 *       404:
 *         description: ไม่พบครอบครัว
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Family not found
 */
router.get('/:family_id/members', familyController.getMembers);

/**
 * @swagger
 * /family/{family_id}/members:
 *   post:
 *     summary: เพิ่มสมาชิกเข้าครอบครัว
 *     tags: [Family]
 *     parameters:
 *       - in: path
 *         name: family_id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID ของครอบครัว
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - user_id
 *             properties:
 *               user_id:
 *                 type: integer
 *                 example: 5
 *                 description: ID ของ User ที่จะเพิ่ม
 *     responses:
 *       200:
 *         description: เพิ่มสมาชิกสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: User 5 added to family 101 successfully
 *       400:
 *         description: User มีบ้านอยู่แล้ว หรือข้อมูลไม่ครบ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User already belongs to a family
 *       404:
 *         description: ไม่พบ User หรือ Family
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User or Family not found
 */
router.post('/:family_id/members', familyController.addMember);

/**
 * @swagger
 * /family/{family_id}/members/{user_id}:
 *   delete:
 *     summary: ลบสมาชิกออกจากครอบครัว
 *     tags: [Family]
 *     parameters:
 *       - in: path
 *         name: family_id
 *         required: true
 *         schema:
 *           type: integer
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: ลบสมาชิกสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Member removed from family successfully
 *       400:
 *         description: User ไม่ได้อยู่ใน Family นี้
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User is not in this family
 *       404:
 *         description: ไม่พบ User หรือ Family
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User or Family not found
 */
router.delete('/:family_id/members/:user_id', familyController.removeMember);

module.exports = router;