# ビルドステージ
FROM hseeberger/scala-sbt:graalvm-ce-21.3.0-java17_1.6.2_3.1.1 as builder

# アプリケーション用のディレクトリを作成
WORKDIR /app

# SBT関連のファイルをコピーして依存関係を解決
COPY build.sbt /app/
COPY project /app/project
RUN sbt update

# アプリケーションのソースコードをコピーしてビルド
COPY . /app
RUN sbt stage

# 実行ステージ
FROM openjdk:21-jdk-slim

# アプリケーション用のディレクトリを作成
WORKDIR /app
ENV JWT_SECRET="your-256-bit-secret-docker"
ENV APPLICATION_SECRET="Y@eP8>lgAQ]7Dbf7:]osvwNb`1r/IpQCeKdIA4Cm;?mprB3<3cwKk6e;9Jp1]VgG"
# ビルドステージから実行に必要なファイルだけをコピー
COPY --from=builder /app/target/universal/stage /app

# ポート番号を設定（例: 9000番ポート）
EXPOSE 9000

# アプリケーションの起動コマンド
CMD ["./bin/auth-backend"]