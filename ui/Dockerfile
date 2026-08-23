FROM node:20-alpine

WORKDIR /app

# 先装依赖（利用构建缓存，package*.json 变更才重跑这层）
COPY package*.json ./
RUN npm ci --omit=dev --no-audit --no-fund

# 应用代码
COPY server.js index.html ./
COPY vendor/ vendor/

ENV PORT=5180
EXPOSE 5180

CMD ["node", "server.js"]
