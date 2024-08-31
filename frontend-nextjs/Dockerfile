# 1. ビルドステージ
FROM node:18-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
RUN npm prune --production

# 2. 実行ステージ
FROM node:18-alpine
WORKDIR /app

# ビルドされたアーティファクトをコピー
COPY --from=builder /app ./

# 実行時の環境変数を設定
ENV NODE_ENV=production
# ENV NEXT_PUBLIC_AUTH_SECRET=your-256-bit-secret
# ENV NEXT_PUBLIC_BASE_URL=http://backend:9000
# ENV NEXTAUTH_URL=http://frontend:3000

EXPOSE 3000
CMD ["npm", "start"]