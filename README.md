# 学生成绩管理系统 (Student Score System)

基于 Spring Boot 3 和 Vue 3 开发的前后端分离学生成绩综合管理系统。系统旨在为管理员、教师与学生提供全面、规范且易用的成绩管理和教务数据流转服务。

## 🛠 技术栈概览

### 后端 (student-score-server)
- **核心框架**: Java 17+ / Spring Boot 3.x
- **持久层中间件**: MyBatis-Plus / MySQL 8.0
- **连接池 / 监控**: Druid 
- **安全与权限**: Spring Security + JWT Token + RBAC模型
- **缓存机制**: Redis（用于 Token 存储、热点数据聚合计算）
- **周边工具**: Knife4j (Swagger) 接口文档 / EasyExcel 报表导入导出 / Lombok

### 前端 (student-score-ui)
- **前端框架**: Vue 3 (Composition API) / Vite
- **状态管理配置**: Pinia
- **路由控制**: Vue Router 4 (支持动态路由与菜单权限鉴定)
- **UI 组件库**: Element Plus
- **数据可视化**: ECharts 5.x
- **网络请求**: Axios (集成全局异常拦截与Token注入)

## 🚀 项目运行规范操作

请严格按照以下步骤进行本地开发环境的部署与测试运行：

### 1. 数据库准备与初始化
1. 安装并进入 **MySQL (8.0+)** 客户端。
2. 创建对应的数据库 `student_score`（建议字符集使用 `utf8mb4`，排序规则 `utf8mb4_general_ci`）。
3. 使用 Navicat 或本地命令执行后端工程中的 SQL 文件进行数据库初始化：
   - 路径：`student-score-server/sql/student_score.sql`
   - 此脚本包含了完整的表结构定义、外键引用以及用于测试的默认菜单结构和基础管理员、普通用户账号数据。

### 2. 后端服务启动
1. 确保已在本地或测试服务器上成功启动 **Redis (6.0+)** 服务端（默认 6379 端口，若设置了密码请留意）。
2. 使用 IDEA 等开发工具打开 `student-score-server` 目录。
3. 调整项目配置文件 `src/main/resources/application.yml`：
   - 确认并修改 `spring.datasource` 中的数据库账号密码及连接地址。
   - 确认并修改 `spring.data.redis` 中的 Redis 账号密码及端口。
4. 运行主启动类 `StudentScoreApplication.java`。或者使用 Maven 命令行：
   ```bash
   cd student-score-server
   mvn clean install
   mvn spring-boot:run
   ```
   > 启动成功后，后端服务默认监听 `8080` 端口。接口文档访问地址可参考控制台输出（通常为 `http://localhost:8080/doc.html`）。

### 3. 前端服务启动
1. 确保您的电脑内已安装有 **Node.js 18+**。
2. 使用终端工具进入 `student-score-ui` 目录。
3. 执行安装项目所有的依赖库：
   ```bash
   npm install
   ```
4. 启动前端 Vite 调试服务器：
   ```bash
   npm run dev
   ```
   > 启动成功后，控制台会输出访问地址（通常为 `http://localhost:5173`），点击即可进入登录页。

## 🔐 常用测试账号说明
根据数据库初始化 SQL 的预置设定，可通过以下三种角色的演示账号进行不同业务域的登录测试（默认登录密码均为 `123456`，请参阅表内定义，如无修改以此为准）：

- **系统管理员**: 账号 `admin`，密码 `123456`。拥有最高权限，进入 `管理员后台` 进行所有的配置和调整。
- **教职工组**: 账号 `teacher`（或具体录入的教工号），密码 `123456`。登录后默认显示与教务排课、审批、成绩录入等教师强相关页面。
- **学生组**: 账号 `student`（或具体录入的学生学号），密码 `123456`。登录后默认展示属于学生的入口面板，侧重于查分、查看统计可视化雷达图等。

## 🧱 其它文档参照
- 关于当前数据库具体的设计与关联维度定义，请使用 `draw.io` 直接打开查看同级目录下的 `student-score-server/ER图.drawio` 或预览 `ER图.drawio.png` 即可。
