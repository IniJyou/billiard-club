# 台球厅会员管理系统（Billiard Club Management System）

软件课程设计 I 课程项目。面向台球厅前台/老板与会员的计费、充值与会员权益管理一体化系统。

## 技术栈（对应课程 MVC / J2EE 多层架构）

| 层 | 技术 |
|---|---|
| 表示层（View / 前端） | Vue3 + Vite + Element Plus + Pinia + Vue Router + Axios |
| 表示层（Controller） | Spring Boot REST Controller |
| 业务逻辑层（Service） | Spring Boot Service / ServiceImpl |
| 数据访问层（Mapper） | MyBatis-Plus（MyBatis） |
| 数据库 | MySQL 8 |

## 目录结构

```
billiard-club/
├── database/
│   └── init.sql                  # 建库建表 + 初始化数据（一键导入）
├── docs/
│   └── 详细设计说明书.md          # 需求分析 + 详细设计（提交前改名: 组号+详细设计说明书.pdf）
├── backend/                      # Spring Boot 后端
│   ├── pom.xml
│   ├── .mvn/jvm.config           # -Dfile.encoding=UTF-8（解决 GBK 乱码）
│   └── src/main/
│       ├── java/com/club/
│       │   ├── BilliardClubApplication.java   # 启动类
│       │   ├── common/            # Result / PageResult / 异常处理
│       │   ├── config/            # MyBatis-Plus 配置
│       │   ├── controller/        # 登录、会员、球桌、计费、流水 REST 接口
│       │   ├── service/           # 事务、计费、折扣、积分与权限逻辑
│       │   ├── mapper/            # MyBatis-Plus Mapper + 行锁/关联查询
│       │   └── entity/            # 8 张业务表对应实体
│       └── resources/
│           ├── application.yml    # 配置（含数据库连接，需改密码）
│           └── mapper/            # MyBatis XML（可选，复杂 SQL 用）
└── frontend/                     # Vue3 前端
    ├── package.json
    ├── vite.config.js            # 已配置 /api 代理到 8080
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/               # 路由
        ├── api/                  # axios 封装
        ├── styles/               # 全局样式
        ├── layouts/              # 登录后的公共侧边栏与顶栏
        ├── stores/               # Session 用户状态
        └── views/                # 登录、工作台、会员、球桌、流水页面
```

## 快速开始

### 1. 初始化数据库
```bash
mysql -u root -p < database/init.sql
```
或在 Navicat / DBeaver / MySQL Workbench 里直接执行 `init.sql`。

如果已经导入过旧版 `init.sql`，再执行：

```bash
mysql -u root -p < database/migration_v2.sql
```

### 2. 启动后端
1. 修改 `backend/src/main/resources/application.yml` 里的 `spring.datasource.password` 为你的 MySQL root 密码。
2. 启动：
```bash
cd backend
mvn spring-boot:run
```
默认端口 8080。

### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```
访问 http://localhost:5173 ，前端 `/api` 请求会自动代理到后端 8080。

演示账号：

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | 123456 |
| 前台 | cashier | 123456 |

## 核心业务闭环（演示主线）

登录 → 会员建档/充值 → 选桌开台计时 → 结账（时长 × 单价 × 会员折扣）→ 扣余额/累积分 → 流水与报表查询。

## 已实现的核心功能

1. HttpSession 登录、会话恢复、退出和管理员权限控制。
2. 会员分页检索、建档、编辑、启停、手机号唯一校验。
3. 会员等级折扣、消费积分和按积分阈值自动升级。
4. 会员充值、余额更新和充值流水事务一致性。
5. 球桌空闲/使用中/维护看板，重复开台保护。
6. 整小时进位结账、现金/余额支付、折扣、积分、账单与消费流水。
7. 充值流水和消费流水分页查询。

商品销售、复杂报表、员工管理和优惠活动仍属于扩展范围。

## 验证与打包

```bash
cd backend
mvn test
mvn package

cd ../frontend
npm run build
```

Windows 下可以在项目根目录执行 `scripts/build-delivery.ps1 -GroupNo 你的组号`。脚本会生成两个文件：一个只含源码、SQL 和文档的 20MB 内源码包；另一个包含后端 jar 与前端 dist 的完整可运行包。当前 Spring Boot 可执行 jar 约 27.4MB，因此完整包会超过课程要求的 20MB，脚本会保留文件并给出警告；提交前需要向老师确认是否可以拆分上传。

## 团队分工（3 人，按模块切分，便于统计个人代码量）

| 成员 | 负责 | 产出 |
|---|---|---|
| 组长 A | 架构搭建 + 数据库整体设计 + 会员/充值模块 + 需求分析主导 | 会员/充值前后端 + ER/表结构 + 用例图 |
| 成员 B | 球桌 + 开台结账核心计费 | 球桌/会话/账单 + 计费算法 + 序列图 |
| 成员 C | 登录权限 + 流水查询 + 报表 + 前端公共组件 | 鉴权 + 流水/报表 + 页面原型 + 测试 |

## 约定

- 统一返回体 `Result<T>`、统一异常处理（见 `common/` 包）。
- 业务接口需要登录；管理员专属接口返回 HTTP 403，登录失效返回 HTTP 401。
- 命名规范：`XxxController` / `XxxService` / `XxxServiceImpl` / `XxxMapper` / `Xxx`(entity)。
- 数据库表名蛇形、Java 字段驼峰（MyBatis-Plus 已开启 `map-underscore-to-camel-case`）。
- 交付压缩包需 < 20M，注意排除 `node_modules`、`target`、`.git`。
