# 码上启航 - 校园二手交易平台

面向安徽信息工程学院师生的校园二手交易 Web 应用，基于 Spring Boot、MyBatis、Thymeleaf、Bootstrap 5、jQuery AJAX、H2/MySQL 构建。

## 功能

- 用户注册、登录、学号实名认证、个人中心、密码修改
- 商品发布、多图上传、列表分页、分类筛选、关键词搜索、详情展示
- 商品收藏、留言评论、站内私信
- 下单预订、卖家确认、买家完成、订单状态联动商品状态
- 管理后台、商品管理、ECharts 分类与订单统计
- AI 发布助手：智能生成标题、优化描述、估算价格区间、检测交易风险
- AI 商品洞察：详情页展示可信度评分、价格信号、交易前确认清单
- AI 相似推荐：根据分类和热度推荐同类在售商品
- AI 搜索：支持自然语言意图识别，并通过同义词扩展提升“耳麦/耳机、资料/教材”等搜索命中率
- 响应式页面，适配桌面端和移动端
- 页面动效：动态路灯登录页、商品轮播、卡片悬浮、滚动入场、指标数字增长、AI 扫描反馈、按钮加载状态
- 体验优化：Toast 提示、图片懒加载、骨架屏、商品浏览量去重
- 性能与安全：Caffeine 搜索/统计缓存、AI 接口限流、结构化访问日志

## 快速启动

本项目默认使用 H2 内存数据库，启动后会自动建表并导入演示数据。

```bash
mvn spring-boot:run
```

也可以直接双击桌面快捷方式：

```text
启动码上启航校园二手交易平台
```

该快捷方式会自动判断系统是否已启动：已启动则直接打开首页，未启动则启动本地服务并在就绪后自动打开首页。

对应脚本为：

```text
scripts/start-campus-market.bat
```

浏览器访问：

```text
http://localhost:8080
```

演示账号：

```text
普通用户：2023123401 / 123456
管理员：2023000001 / 123456
```

H2 控制台：

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:campus_market
User: sa
Password: 留空
```

## 切换 MySQL

1. 在 MySQL 8.0 中执行 [docs/schema-mysql.sql](docs/schema-mysql.sql)。
2. 修改 [src/main/resources/application-mysql.yml](src/main/resources/application-mysql.yml) 中的用户名和密码。
3. 使用 MySQL Profile 启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 项目结构

```text
src/main/java/com/qihang/campusmarket
  config       Web 配置、鉴权拦截器、全局模型
  controller   页面路由和 AJAX 接口
  dto          页面展示对象和统计对象
  entity       数据实体
  form         表单校验对象
  mapper       MyBatis 数据访问层
  service      业务逻辑层
  util         Session 和密码工具

src/main/resources
  db           H2/MySQL 兼容建表与演示数据
  templates    Thymeleaf 页面模板
  static       CSS、JS、图片资源
```

## 技术栈

- Spring Boot 3.3.5
- MyBatis Spring Boot Starter 3.0.3
- Spring Cache + Caffeine
- Thymeleaf
- Bootstrap 5
- jQuery AJAX
- ECharts
- H2 / MySQL 8.0
- Maven

## AI 能力说明

当前 AI 能力采用“本地规则引擎 + 商品库统计”的方式实现，不依赖外部 API Key，适合课程设计演示和离线运行。后续如果需要接入大模型，可以替换 `AiAssistantService` 内部实现，前端和控制器接口无需大改。

## 优化路线

已完成：

- 零成本体验优化：商品轮播、卡片动效、Toast、图片懒加载、骨架屏
- 零成本性能优化：Caffeine 缓存、浏览量 30 分钟内去重
- 技术增强：AI 搜索、语义同义词扩展、接口限流、访问日志规范

后续可扩展：

- 通义千问 AI 图片识别：需要配置 `DASHSCOPE_API_KEY` 后接入商品图片识别、自动分类和描述生成
- Docker 部署与 ENV 隔离：拆分 `dev/mysql/prod` 配置，补充 `Dockerfile` 与 `docker-compose.yml`
- AI 导购对话：在现有 `/ai/search` 基础上增加多轮对话和商品推荐卡片
- 分享海报：基于商品详情生成 Canvas/服务端海报，支持一键保存或分享
