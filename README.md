# 图书馆管理系统 (Library Management System)

本项目是一个前后端分离的图书馆管理系统，旨在提供用户管理、图书管理等核心功能。

## 主要功能

* **用户管理：**
    * 用户注册
    * 用户登录 (基于 Token 的认证)
    * 用户信息查看与修改 (管理员)
    * 用户删除 (管理员)
    * 用户列表展示 (管理员)
* **图书管理：**
    * 图书信息录入 (增)
    * 图书信息删除 (删)
    * 图书信息修改 (改)
    * 图书信息查询 (查)，支持按书名、作者等条件搜索
    * 图书库存数量管理
    * 图书价格管理
* **(可选/未来功能)**
    * 图书借阅与归还流程
    * 借阅记录查询
    * 用户角色与权限细分 (例如：普通用户、图书管理员、系统管理员)
    * 图书封面图片上传与展示

## 技术栈

**后端 (Backend - Spring Boot)**

* **框架:** Spring Boot 3.x
* **语言:** Java 17
* **数据持久化:** Spring Data JPA (Hibernate)
* **数据库:** MySQL
* **安全性:** Spring Security (Token-based Authentication - JWT)
* **API 文档:** Springdoc OpenAPI (Swagger UI)
* **构建工具:** Maven
* **其他:** Lombok

**前端 (Frontend - Vue.js)**

* **框架:** Vue.js 3.x
* **构建工具:** Vite
* **HTTP 客户端:** Axios
* **路由:** Vue Router
* **状态管理 (可选):** Pinia 或 Vuex 
* **UI (可选):** Element Plus, Vuetify, 
* **开发工具:** HBuilderX (或 VS Code 等)

## 项目结构
## 环境准备

在运行项目之前，请确保你已安装以下软件：

* **Java Development Kit (JDK):** 17 或更高版本
* **Maven:** 3.6 或更高版本 (用于构建后端)
* **Node.js:** 16.x 或更高版本 (包含 npm，用于构建和运行前端)
* **MySQL Server:** 5.7 或更高版本 (或 8.x)
* **Git:** 用于版本控制
* **IDE:**
    * 后端: IntelliJ IDEA (推荐)
    * 前端: HBuilderX, VS Code (推荐, 并安装 Vue 相关插件如 Volar)

## 数据库设置

1.  **创建数据库：**
    在你的 MySQL 服务器中创建一个新的数据库。例如，可以命名为 `library_db`。
    ```sql
    CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  **配置数据库连接 (后端)：**
    * 打开后端项目 `libraryBackend/src/main/resources/application.properties` 文件。
    * 修改以下属性以匹配你的 MySQL 设置：
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
        spring.datasource.username=your_mysql_username
        spring.datasource.password=your_mysql_password
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

        spring.jpa.hibernate.ddl-auto=update # 开发时可以使用 update 或 create，生产环境建议使用 validate 或 none，并通过迁移工具管理
        spring.jpa.show-sql=true
        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect # 或 org.hibernate.dialect.MySQL8Dialect
        ```
    * 将 `your_mysql_username` 和 `your_mysql_password` 替换为你的 MySQL 用户名和密码。
3.  **初始化表结构和数据：**
    * 当后端应用第一次启动时，如果 `spring.jpa.hibernate.ddl-auto` 设置为 `update` 或 `create`，Hibernate 会自动创建或更新数据库表。
    * 你可能需要手动插入一些初始用户数据（特别是管理员账户），并确保密码是使用后端配置的 `BCryptPasswordEncoder` 加密后的哈希值。请参考项目内或联系开发者获取生成哈希密码的方法。
        * **示例管理员用户 (需手动加密密码后插入)：**
            * 用户名: `admin`
            * 明文密码: `admin123` (数据库中存储的是其 BCrypt 哈希值)
            * 角色: `ROLE_ADMIN`

## 如何运行项目

你需要分别启动后端和前端服务。

**1. 运行后端 (Spring Boot):**

   * **使用 IntelliJ IDEA (推荐):**
        1.  打开 IntelliJ IDEA。
        2.  选择 "Open" 并导航到 `libraryBackend` 文件夹，将其作为 Maven 项目打开。
        3.  等待 IDEA 下载所有 Maven 依赖并建立索引。
        4.  找到主应用类 (通常是 `com.example.library.LibraryApplication.java` 或类似名称，带有 `@SpringBootApplication` 注解)。
        5.  右键点击该文件，选择 "Run 'LibraryApplication.main()'"。
        6.  后端服务默认会运行在 `http://localhost:8080` (除非你在 `application.properties` 中修改了 `server.port`)。
   * **使用命令行 (Maven):**
        1.  打开命令行工具 (CMD, PowerShell, Git Bash 等)。
        2.  导航到后端项目根目录：`cd path/to/your/library-monorepo/libraryBackend`
        3.  执行 Maven 命令来构建和运行项目：
            ```bash
            mvn spring-boot:run
            ```
            或者先打包再运行：
            ```bash
            mvn clean package
            java -jar target/library-0.0.1-SNAPSHOT.jar
            ```

**2. 运行前端 (Vue.js):**

   * **使用 HBuilderX:**
        1.  打开 HBuilderX。
        2.  选择 "文件" -> "导入" -> "从本地目录导入"，然后选择 `libraryFrontend` 文件夹。
        3.  项目导入后，如果需要安装依赖，可以在项目根目录右键选择 "外部命令" -> "npm install" (或 yarn)。
        4.  右键点击项目或 `index.html` (或 `App.vue` / `main.js`)，选择 "运行" -> "运行到浏览器" -> 选择浏览器。
   * **使用命令行 (npm/yarn):**
        1.  打开新的命令行工具。
        2.  导航到前端项目根目录：`cd path/to/your/library-monorepo/libraryFrontend`
        3.  安装项目依赖 (如果尚未安装)：
            ```bash
            npm install
            # 或者
            # yarn install
            ```
        4.  启动 Vite 开发服务器：
            ```bash
            npm run dev
            # 或者
            # yarn dev
            ```
        5.  前端服务通常会运行在 `http://localhost:5173` (或其他 Vite 提示的端口)。

**3. 配置前端 API 地址 (如果需要):**

   * 前端应用需要知道后端 API 的地址。这通常在前端项目的 `.env` 文件中配置。
   * 打开 `libraryFrontend/.env` 文件 (如果不存在则创建)。
   * 确保以下环境变量指向你后端服务的正确地址：
       ```env
       VITE_API_BASE_URL=http://localhost:8080/api/v1
       ```
       (如果你的后端 API 基础路径不是 `/api/v1`，或者端口不是 `8080`，请相应修改。)

## 如何使用应用

1.  **确保后端和前端服务都已成功启动。**
2.  在浏览器中打开前端应用的地址 (例如 `http://localhost:5173`)。
3.  你应该会看到应用的首页或登录页面。
4.  **登录：** 使用你在数据库中设置的用户名和对应的**明文密码**进行登录。
5.  登录成功后，你应该可以访问系统的其他功能，例如：
    * **图书管理：** 浏览图书列表，搜索图书，添加新书（管理员），编辑图书信息（管理员），删除图书（管理员）。
    * **用户管理 (管理员)：** 查看用户列表，管理用户信息。
6.  **API 文档 (Swagger UI)：**
    如果后端服务正常运行，你可以通过浏览器访问 `http://localhost:8080/swagger-ui.html` (或其他你在 `application.properties` 中配置的路径) 来查看和测试后端 API 接口。

## 注意事项

* **CORS (跨域资源共享)：** 在开发环境中，由于前端和后端运行在不同的端口上，后端 Spring Boot 应用需要正确配置 CORS 以允许来自前端的请求。请检查后端 `SecurityConfig.java` 中的 CORS 配置。
* **数据库外键约束：** 日志中可能出现的 Hibernate DDL 外键约束错误需要解决，以确保数据库结构的正确性。
* **Bean Validation：** 确保后端项目中包含了 `spring-boot-starter-validation` 依赖，以便 DTO 上的校验注解生效。
* **敏感信息：** 切勿将包含真实密码、密钥等敏感信息的文件直接提交到公共 Git 仓库。使用环境变量或安全的配置文件管理方式。
