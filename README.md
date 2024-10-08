# What's this Repository?

"I’ve written this freely, so it may lack coherence."  
This repository is a quick implementation of the technologies I’m currently interested in.  
The architecture and programming languages may change from time to time.

## Project Overview

This project is a fully integrated system built from the backend to the frontend. The backend is developed using Scala and Play Framework, handling user authentication and database operations. The frontend is built with Next.js and NextAuth, providing the user interface and authentication flow. MySQL is used for the database, and Docker is utilized for containerization.

The system authenticates users through an authentication backend and provides task data to authenticated users. Future plans include adding more APIs, improving the frontend design, and deploying the system on Kubernetes.

## About 
The directory structure is as follows:

- `./auth-backend`  
  This directory contains the backend for authentication logic, built with Play Framework.  
  - You can start the server with the command: `sbt run`.

- `./frontend-nextjs`  
  This directory contains the frontend built with Next.js. The functionality is currently under development.

- `./database`  
  This directory holds the initial database scripts and the Dockerfile.

## Installation Instructions

```sh
git clone <this repository URL>
cd <repository directory>
docker compose up --build
```
The credentials for authentication are:

- ID: admin
- Password: password123


### Summary of the Structure

```mermaid
sequenceDiagram
    participant User as User
    participant Frontend as Frontend (Next.js)
    participant AuthBackend as Auth Backend (Play Framework)
    participant Backend as Backend (Play Framework)
    participant MySQLDB as MySQL Database
    participant BacklogAPI as Backlog API

    User->>Frontend: Authentication request
    Frontend->>AuthBackend: Authentication request (NextAuth.js)
    AuthBackend->>MySQLDB: Verify user credentials
    MySQLDB-->>AuthBackend: Authentication result (user existence)
    AuthBackend-->>Frontend: Issue access token
    Frontend-->>User: Receive access token

    User->>Frontend: Request for tasks
    Frontend->>Backend: Request with access token
    Backend->>AuthBackend: Validate access token
    AuthBackend-->>Backend: Validation result
    Backend->>BacklogAPI: Request to Backlog API with access token
    BacklogAPI-->>Backend: Respond with task data
    Backend-->>Frontend: Respond with task data
    Frontend-->>User: Display tasks
```

- The Backlog API uses an API Key for authentication.  
  [Reference](https://developer.nulab.com/ja/docs/backlog/auth/)

## Explanation of the Technologies Used:
- **Frontend**: Next.js, NextAuth
- **Backend**: Scala, Play Framework
- **Database**: MySQL
- **Docker**

In the future, I plan to deploy this on Kubernetes.

## Future Plans
- **Backend**: I plan to add more APIs and implement phantom tokens.
- **Frontend**: I aim to improve the design for a cleaner look.

## Docker Commands

- **Start the Database**
  ```sh
  docker build -t db ./database/.
  docker run --name db-container -p 3306:3306 -d db


- Start Docker 
```sh
docker compose up --build

```

### Access to the Frontend

After dockercompose build, access the following URL

```
http://localhost:3000
```

![loginpage](./image/signup-page-image.png)

username : admin
password : password123

access to the site is successful.
So far only SignIn is working.

If successful, the following page will appear

![successpage](./image/success-page-image.png)

### Access to the Backend

If you want access to the back end, [here](http://localhost:9000).

The schema of the API is . /auth-backend/conf/routes for reference.


### Access to the Database

Basic information is as follows

```sh
PORT=3306
DATABASENAME=db
USERNAME=dbuser
PASSWORD=dbuser
```

- Stop and Clean Up Docker

```sh
# Stop the container
docker stop db-container
# Remove the container
docker rm db-container
# Remove the image
docker rmi db
```

