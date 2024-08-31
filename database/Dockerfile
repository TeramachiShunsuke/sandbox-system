# MySQLの公式イメージを使用
FROM mysql:9.0

# 環境変数を設定 本来はもっと複雑なパスワードを設定するべき
ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=db
ENV MYSQL_USER=dbuser
ENV MYSQL_PASSWORD=dbuser

# 初期化用のSQLスクリプトをコピー
COPY init.sql /docker-entrypoint-initdb.d/