# ใช้ node lts ล่าสุด และเลือก debian slim เพราะเบา ปัญหาเจอน้อยกว่า
FROM node:24-bookworm-slim

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY .env .

COPY . .

EXPOSE 3000

CMD ["npm", "run", "dev"]
