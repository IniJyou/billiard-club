# 台球厅会员管理系统（Billiard Club Management System）

软件课程设计 I 课程项目。面向台球厅管理员、前台与普通用户的会员办理、球桌预约、计费充值与经营管理一体化系统。

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
│   ├── init.sql                  # 建库建表 + 初始化数据（一键导入）
│   ├── migration_v2.sql          # 旧数据库升级脚本
│   ├── migration_v3_user_portal.sql  # 用户端与当天预约升级脚本
│   └── migration_v4_member_profile.sql # 会员资料字段升级脚本
├── docs/
│   └── 详细设计说明书.md          # 需求分析 + 详细设计（提交前改名: 组号+详细设计说明书.pdf）
├── backend/                      # Spring Boot 后端
│   ├── pom.xml
│   ├── .mvn/jvm.config           # -Dfile.encoding=UTF-8（解决 GBK 乱码）
│   └── src/
│       ├── main/
│       │   ├── java/com/club/
│       │   │   ├── BilliardClubApplication.java   # 启动类
│       │   │   ├── common/        # 响应封装、异常处理与角色校验
│       │   │   ├── config/        # MyBatis-Plus、鉴权、日志与序列化配置
│       │   │   ├── controller/    # 登录、会员、球桌、预约、计费、流水与报表接口
│       │   │   ├── service/       # 事务、计费、折扣、积分、预约与权限逻辑
│       │   │   ├── mapper/        # MyBatis-Plus Mapper + 行锁/关联查询
│       │   │   ├── entity/        # 会员、用户、球桌、预约、账单与流水实体
│       │   │   ├── dto/           # 请求参数对象
│       │   │   ├── vo/            # 接口响应视图对象
│       │   │   └── util/          # 计费、编号、摘要与 CSV 工具
│       │   └── resources/
│       │       ├── application.yml    # 数据库及应用配置
│       │       └── logback-spring.xml # 运行日志配置
│       └── test/                  # 后端单元测试与集成测试
├── frontend/                     # Vue3 前端
│   ├── package.json
│   ├── vite.config.js            # 已配置 /api 代理到 8080
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/                  # 管理端、前台端和用户端接口封装
│       ├── components/           # 页面头部、指标卡、图表等公共组件
│       ├── layouts/              # 登录后的公共侧边栏与顶栏
│       ├── router/               # 三类角色路由与访问控制
│       ├── stores/               # Session 用户状态
│       ├── styles/               # 全局样式
│       ├── utils/                # 格式化与预约时段工具
│       └── views/                # 登录、会员、球桌、预约、流水、报表及用户端页面
└── scripts/
    └── build-delivery.ps1        # Windows 课程设计交付包构建脚本
```

## 快速开始

### 1. 初始化数据库
```bash
mysql -u root -p < database/init.sql
```
或在 Navicat / DBeaver / MySQL Workbench 里直接执行 `init.sql`。

如果已经导入过旧版 `init.sql`，按版本依次执行升级脚本。升级到用户端版本至少需要执行：

```bash
mysql -u root -p < database/migration_v2.sql
mysql -u root -p < database/migration_v3_user_portal.sql
mysql -u root -p < database/migration_v4_member_profile.sql
```

每个迁移脚本只需执行一次。`migration_v3_user_portal.sql` 会增加用户账号与会员的绑定字段、当天预约表和演示用户；`migration_v4_member_profile.sql` 会增加会员性别与出生日期字段。

### 2. 启动后端
1. 默认数据库账号为 `root / 123456789`；如本机不同，可设置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`，或修改 `application.yml`。
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
| 用户 | 13900000000 | 123456 |

## 核心业务闭环（演示主线）

管理员/前台：登录 → 会员建档/充值 → 处理预约或选桌开台 → 结账（时长 × 单价 × 会员折扣）→ 流水与报表查询。

普通用户：注册/登录 → 在线办理会员 → 查看卡号、余额与折扣 → 模拟微信/支付宝充值 → 预约当天球桌 → 查看个人流水。

## 已实现的核心功能

1. HttpSession 登录、用户注册、会话恢复、修改密码、退出和三角色权限控制；兼容旧 MD5 密码并在成功登录后自动升级为 BCrypt。
2. 会员分页检索、建档、编辑、启停、现场注销和手机号唯一校验；注销卡不再出现在有效会员列表中，但保留历史业务记录。
3. 会员等级折扣、消费积分和按积分阈值自动升级。
4. 会员充值、余额更新和充值流水事务一致性。
5. 球桌空闲/使用中/维护看板，显示每张球桌当天的有效预约起止时间，并提供重复开台保护。
6. 整小时进位结账、现金/余额支付、折扣、积分、账单与消费流水。
7. 充值流水和消费流水分页查询，支持日期、支付方式、操作员筛选及 UTF-8 CSV 导出。
8. 管理员经营报表：消费收入、充值实收、优惠、客群、支付方式、每日趋势和球桌利用率。
9. 访问、业务操作、SQL 和异常的滚动文件日志，日志请求使用 `X-Request-Id` 关联。
10. 普通用户端在线办理会员，可填写姓名、性别、出生日期并选择会员卡类别；卡面按类别显示不同颜色、类别名称和消费折扣，不展示或设置积分。
11. 普通用户模拟微信/支付宝在线充值，查看自己的充值与消费流水。
12. 当天球桌预约，每次 1 至 4 小时，支持待处理、已开台、已完成、已取消四种状态；用户按半小时选择开始时间，过去、冲突、跨天及不可用时段显示为灰色且不可选择；管理员/前台可将到店预约转为开台。
13. 会员注销只能由管理员或前台在本人到场后办理。账户余额不影响注销；注销后原会员卡和卡号永久失效、用户端卡面消失，用户可以重新办理并取得新卡号。为保证账单和流水完整，已注销档案只在后台历史数据中保留。

商品销售、员工账号管理、优惠活动和跨门店报表仍属于扩展范围。

### 权限说明

| 功能 | 管理员 | 前台 | 用户 |
|---|---|---|---|
| 会员管理、线下充值、开台、结账、业务流水 | 可以 | 可以 | 不可以 |
| 当天预约处理 | 可以 | 可以 | 仅新建、查看和取消自己的待处理预约 |
| 查看球桌状态和今日预约时段 | 可以 | 可以 | 可以 |
| 在线办理会员、在线充值、个人流水 | 不可以 | 不可以 | 可以 |
| 会员注销 | 本人到场后可办理 | 本人到场后可办理 | 不提供注销入口，需本人到现场办理 |
| 球桌维护状态 | 可以 | 不可以（HTTP 403） | 不可以（HTTP 403） |
| 经营报表 | 可以 | 不可以（HTTP 403） | 不可以（HTTP 403） |

### 成员 C 接口

- `POST /api/auth/login`、`GET /api/auth/me`、`POST /api/auth/logout`
- `GET /api/records/recharges`、`GET /api/records/consumptions`：支持 `keyword`、`startDate`、`endDate`、`payWay`、`operatorId`
- `GET /api/records/operators`：流水操作员筛选项
- `GET /api/records/recharges/export`、`GET /api/records/consumptions/export`：按当前条件导出 CSV
- `GET /api/reports/overview`：管理员经营报表，日期范围最多 366 天

报表中“消费收入”取已结账账单实收金额，“充值实收”只取充值本金、不含赠送。球桌利用率按非取消开台与查询区间重叠的实际分钟数计算；当前库没有维护时段历史，因此维护时间不从分母扣除。

## 日志

使用 `cd backend` 启动后，日志默认写入 `backend/logs/`：

- `billiard-club.log`：启动信息、业务操作和 MyBatis SQL。
- `access.log`：每次 API 请求的方法、路径、状态码、操作员、IP 和耗时。
- `error.log`：ERROR 级别异常及堆栈。
- `archive/`：按日期和每 10MB 滚动的压缩历史日志，默认保留 30 天。

登录失败、越权访问、会员建档/修改/启停/充值、球桌维护、开台、取消和结账都会留下审计记录。日志不会记录登录密码或完整请求体。每个响应包含 `X-Request-Id`，可用它关联同一次请求产生的访问日志和业务日志。

## 验证与打包

```bash
cd backend
mvn test
mvn package

cd ../frontend
npm run test
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
