

```mermaid
sequenceDiagram
    participant User as ユーザー
    participant Frontend as フロントエンド (Next.js)
    participant AuthBackend as 認証バックエンド (Play Framework)
    participant Backend as バックエンド (Play Framework)
    participant MySQLDB as MySQL データベース
    participant BacklogAPI as Backlog API

    User->>Frontend: 認証リクエスト
    Frontend->>AuthBackend: 認証リクエスト (NextAuth.js)
    AuthBackend->>MySQLDB: ユーザー認証情報の確認
    MySQLDB-->>AuthBackend: 認証結果 (ユーザーが存在するか)
    AuthBackend-->>Frontend: アクセストークンの発行
    Frontend-->>User: アクセストークンの受信

    User->>Frontend: タスク取得リクエスト
    Frontend->>Backend: アクセストークンを使用してリクエスト
    Backend->>AuthBackend: アクセストークンの検証
    AuthBackend-->>Backend: アクセストークンの検証結果
    Backend->>BacklogAPI: アクセストークンを使用してBacklog APIにリクエスト
    BacklogAPI-->>Backend: タスクデータのレスポンス
    Backend-->>Frontend: タスクデータのレスポンス
    Frontend-->>User: タスク表示

```

- BacklogApiはAPI Keyで行う。  
[参考資料](https://developer.nulab.com/ja/docs/backlog/auth/)


```sh
docker build -t db ./database/.
docker run --name db-container -p 3306:3306 -d db
```

```sh
# コンテナを停止
docker stop db-container
# コンテナを削除
docker rm db-container
# イメージを削除
docker rmi db
```