# 部署说明

## 为什么 Vercel 显示 404

当前项目是 Spring Boot + Thymeleaf 的 Java Web 应用，需要启动一个长期运行的 JVM 服务。Vercel 更适合静态前端、Next.js 等前端框架，以及它官方支持的函数运行时。Vercel 官方函数运行时列表没有 Java/Spring Boot，所以这个仓库直接导入 Vercel 后，平台会按前端/Node.js 项目处理，最终没有可访问的根路由，访问域名就会看到 `404 NOT_FOUND`。

如果必须使用 Vercel，建议只把前端静态站或未来拆分出的前端部署到 Vercel，Spring Boot 后端单独部署到支持 Java 或 Docker 的平台，再通过环境变量配置 API 地址。

## 推荐部署方式

推荐使用支持 Docker 或 Java Web Service 的平台，例如 Render、Railway、Fly.io、云服务器、阿里云/腾讯云容器服务等。

本项目已经补充：

- `Dockerfile`：使用 Maven + JDK 21 构建并运行 Spring Boot jar。
- `docker-compose.yml`：本地启动应用和 MySQL 8.4。
- `render.yaml`：Render Blueprint 配置，声明这是 Docker Web Service。
- `.env.example`：环境变量模板，真实密钥和密码不要提交到 Git。
- `application.yml`：支持读取云平台注入的 `PORT`。
- `application-mysql.yml`：支持通过环境变量注入数据库连接。

## Render 部署

截图中的失败日志显示 Render 正在执行 `npm`，并尝试读取 `/opt/render/project/src/package.json`。这说明服务被创建成了 Static Site 或 Node 项目，而当前仓库是 Spring Boot/Maven 项目，没有 `package.json`。

推荐重新创建服务：

1. 先把 `Dockerfile`、`render.yaml` 等部署文件提交并推送到 GitHub 的 `main` 分支。
2. 在 Render Dashboard 点击 `New` -> `Web Service`，不要选 `Static Site`。
3. 选择 GitHub 仓库 `RHB12345/github-repository` 和 `main` 分支。
4. `Language` 选择 `Docker`。
5. `Dockerfile Path` 保持默认 `./Dockerfile`。
6. 环境变量至少保留 `PORT=10000`。Render Web Service 默认也使用 `10000`，项目会通过 `server.port=${PORT:8080}` 自动读取。
7. 点击创建后重新部署。

如果使用 Render Blueprint，也可以在 Render 中选择 Blueprint 并指向仓库根目录的 `render.yaml`。注意：已有的 Static Site 服务不能直接变成 Docker Web Service，建议删除失败的 Static Site，重新创建 Web Service。

默认不配置 MySQL 时，应用会使用 H2 内存数据库，适合演示但数据会在重启后丢失。正式使用建议连接外部 MySQL，并在 Render 的 Environment 中配置：

```text
SPRING_PROFILES_ACTIVE=mysql
SPRING_DATASOURCE_URL=jdbc:mysql://数据库地址:3306/campus_market?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=数据库用户名
SPRING_DATASOURCE_PASSWORD=数据库密码
```

上线前请先在 MySQL 中导入 `docs/schema-mysql.sql`。

## 本地 Docker 启动

复制环境变量模板：

```bash
cp .env.example .env
```

修改 `.env` 中的数据库密码后启动：

```bash
docker compose up --build
```

访问：

```text
http://localhost:8080
```

首次启动 MySQL 容器时会自动执行 `docs/schema-mysql.sql` 建表。已有数据库卷不会重复执行初始化脚本。

## 线上部署环境变量

至少需要配置：

```text
PORT=8080
SPRING_PROFILES_ACTIVE=mysql
SPRING_DATASOURCE_URL=jdbc:mysql://数据库地址:3306/campus_market?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=数据库用户名
SPRING_DATASOURCE_PASSWORD=数据库密码
CAMPUS_UPLOAD_DIR=/app/uploads
```

AI 客服接真实模型时再配置：

```text
AI_CUSTOMER_API_URL=
AI_CUSTOMER_API_KEY=
AI_CUSTOMER_MODEL=deepseek-v4-flash
```

上线前请先在云数据库中导入 `docs/schema-mysql.sql`。不要把 `.env`、API Key、数据库密码提交到 GitHub。
