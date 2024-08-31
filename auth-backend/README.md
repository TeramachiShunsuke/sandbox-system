確認するためのコマンド

```sh
# ユーザ一覧取得
curl -X GET http://localhost:9000/users

# 特定のユーザ取得
curl -X GET http://localhost:9000/users/d2c4e1d6-63be-11ef-9628-0242ac110002

# ユーザの作成
curl -X POST http://localhost:9000/users \
  -H "Content-Type: application/json" \
  -d '{
        "username": "newuser",
        "password": "password123",
        "email": "newuser@example.com"
      }'

# ユーザの更新
curl -X PUT http://localhost:9000/users/{id} \
  -H "Content-Type: application/json" \
  -d '{
        "id": "{id}",
        "username": "newUsername",
        "password": "newPassword",
        "email": "newEmail@example.com",
        "apikey": "newApikey",
        "isDeleted": false,
        "createdAt": null,
        "updatedAt": null
      }'

# ユーザの倫理削除
curl -X DELETE http://localhost:9000/users/{id}

# APIKeyの更新
curl -X PUT http://localhost:9000/users/{id}/apikey \
  -H "Content-Type: application/json" \
  -d '{
        "apikey": "newApikeyValue"
      }'
```

```sh
docker build -t auth-backend .    
```

```sh
docker run -t auth-backend
docker run -p 9000:9000 -t auth-backend
```