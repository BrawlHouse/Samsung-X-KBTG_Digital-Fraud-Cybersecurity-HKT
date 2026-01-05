# Dockerfile
# ใช้ Node.js เวอร์ชั่นล่าสุดแบบเบาๆ (Alpine)
FROM node:18-alpine

# สร้าง folder ทำงานข้างใน container
WORKDIR /app

# ก๊อปปี้ไฟล์ package.json ไปก่อน (เพื่อลง library)
COPY package*.json ./

# ลง library ต่างๆ
RUN npm install

# ก๊อปปี้ไฟล์โค้ดทั้งหมดตามเข้าไป
COPY . .

# เปิด Port 3000
EXPOSE 3000

# คำสั่งรัน Server
CMD ["npm", "run", "dev"]