# 待学习清单

> 这里记录**项目里用到、或暂时跳过，但我以后要自己搞懂**的技术点。
> 规则：每当项目中出现「先用现成方案，以后再手写一遍」或「暂时绕过，以后补上」的决策，
> 就往下面追加一条，并写清「背景」「要掌握什么」「什么时候提醒我」。
> AI 助手请勿只把这类事项记在对话里 —— 对话会被压缩，本文件才是可靠记忆。

---

## 1. 自己实现 MyBatis 分页拦截器（替代 PageHelper）

**背景**：Emp 分页查询时在 `web-management/pom.xml` 引入了 `pagehelper-spring-boot-starter:4.1.1`，
但真正有价值的是「分页插件到底怎么运作的」。所以决定：先只引依赖、不改代码，
等项目功能做完后，手写一版自己的分页拦截器。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | `@Intercepts` + `@Signature` | 声明拦截哪个类的哪个方法；分页拦截的是 `StatementHandler.prepare()` |
| 2 | MyBatis 四大可拦截对象 | `Executor` / `StatementHandler` / `ParameterHandler` / `ResultSetHandler` |
| 3 | `Plugin.wrap()` | 用 JDK 动态代理包装目标对象 —— 这是 MyBatis 插件机制的入口 |
| 4 | 改写 SQL | 在 `prepare()` 中拿到 `BoundSql`，为其追加 `LIMIT ?, ?` |
| 5 | 自动 count | 由原 SQL 推导出 `select count(*) from (...)` —— 对比 PageHelper 的做法 |
| 6 | ThreadLocal 传参 | 分页参数如何从 Service 传到 Mapper；**这正是 PageHelper 的 `startPage()` 必须紧挨查询调用的原因** |
| 7 | 读源码 | 对照 `com.github.pagehelper.PageInterceptor` 看工业级实现 |

**为什么值得学**：MyBatis 插件机制 =「动态代理 + 责任链」的经典案例。
掌握后，SQL 审计、多租户隔离、数据权限过滤都能复用同一套思路。

**提醒时机**：项目功能开发基本完成、进入工程化收尾（M9）时。

---

## 2. 事务的四大特性（ACID）　【复习内容】

> **备注：复习内容** —— 这是已经学过的知识，不是要新学的东西。
> 需要的不是"学会"，而是**定期回顾 + 在项目里真正用上一次**。

**四项特性**：

| 特性 | 英文 | 一句话含义 | 破坏了会怎样 |
|---|---|---|---|
| **原子性** | Atomicity | 事务里的操作要么全成功，要么全回滚 | 转账扣了钱、对方没到账 |
| **一致性** | Consistency | 事务前后数据的完整性约束不被破坏 | 账户总额凭空多出 / 少掉 |
| **隔离性** | Isolation | 并发事务之间互不干扰 | 脏读、不可重复读、幻读 |
| **持久性** | Durability | 提交后的修改永久落盘 | 断电后已提交的数据丢失 |

**要能答上来的问题**（答不顺 = 没真懂）：

| # | 问题 | 要点 |
|---|---|---|
| 1 | ACID 里哪个是**目的**、哪几个是**手段**？ | **一致性是目的**；原子性、隔离性、持久性都是为它服务的手段。这个角度最常被追问 |
| 2 | 四种隔离级别分别解决什么、还剩什么？ | 读未提交 → 脏读；读已提交 → 不可重复读；可重复读 → 幻读；串行化 → 全解决但性能极差 |
| 3 | MySQL 默认隔离级别是哪个？怎么查？ | 可重复读（REPEATABLE READ）；`SELECT @@transaction_isolation;` |
| 4 | `@Transactional` 默认对哪些异常回滚？ | 只对 `RuntimeException` 和 `Error` 回滚，**受检异常默认不回滚** —— 这也是 `BusinessException` 要 `extends RuntimeException` 的原因之一 |
| 5 | 脏读 / 不可重复读 / 幻读，各自是什么场景？ | 能用自己的话各举一个例子讲清楚 |

**在本项目里的落点**：

- `DeptServiceImpl`、`EmpServiceImpl` 现在每个方法**只操作一张表** —— 加不加 `@Transactional` 效果一样，所以**现在不用急着加**
- 等 **M4 练习闭环**做「提交试卷」时，一次要写 **4 张表**：`practice_record`、`answer_detail`、`wrong_book`、`knowledge_mastery`
  → 那时事务是**必需的**，否则会出现"有作答记录、却没有答案明细"的脏数据
- 到那时**动手验证一次**：故意让第 3 步抛异常，确认前两步被回滚
  → 这是唯一能真正证明自己懂事务的方式，光背 ACID 不算

**复习时机**：
- **M4 练习闭环开始之前**（正好要用，趁热打铁）
- 面试前
- 任何时候想不起来"不可重复读和幻读的区别"的时候

---

## 3. MyBatis 一对多映射（`<resultMap>` + `<collection>`）

**背景**：Emp 模块的「查询回显」需要"查员工 + 带上他的多条工作经历"。
当时为了先把功能跑通，用了**分两次查**的方案（方案 B）：

```java
// EmpServiceImpl.getById —— 当前上线版本
Emp emp = empMapper.getById(id);
if (emp == null) throw new BusinessException("该员工不存在");
emp.setExprList(empExprMapper.getById(id));   // ← 第二次查询
return emp;
```

**这个方案能工作，也不丢人**（简单、直观、两边独立）。
但**一对多映射是 MyBatis 区别于"纯 JDBC 包装"的核心能力**，值得单独用 `<resultMap>` 重写一遍。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | `resultType` vs `resultMap` | `resultType` 是"**一行 → 一个对象**"的自动映射；`resultMap` 是手工定义映射规则，**只有它能表达嵌套的集合 / 关联** |
| 2 | `<collection>` | 表达"一个对象持有多个子对象"。写 **`ofType`（集合元素类型）**，不是 `javaType` —— 后者是属性本身的类型（`List`），新手最常错 |
| 3 | **`<id>` 标签的作用** | MyBatis 靠它判断"**哪些行属于同一个父对象**"，用它把 N 行**折叠**成 1 个对象。不写 `<id>` 会得到 N 个重复的父对象，每个只挂一条子记录 |
| 4 | 列别名不能省 | 两表同名列（`emp.id` 与 `emp_expr.id`、`emp.job` 与 `emp_expr.job`）**必须起别名**（如 `expr_id`、`expr_job`），否则无法区分哪列归属哪个对象 |
| 5 | `<association>` | 一对一版本（比如"员工 → 所属部门"），和 `<collection>` 是孪生标签 |
| 6 | 嵌套查询 vs 嵌套结果 | `<collection select="...">` 会产生 N+1 次查询（可配延迟加载 `fetchType="lazy"`）；`<collection>` 里直接写列则是**单次 join**。要知道两者差别 |
| 7 | 为什么有人偏好"分两次查" | 简单、可读、**分页时不会因为 join 放大行数而算错**。join 聚合在"分页 + 一对多"组合里反而容易出问题 |

**动手改造对象**：`EmpMapper.getById` —— 把现在的

```java
@Select("select id, username, ... from emp where id = #{id}")
Emp getById(Integer id);
```

改成 `resultMap` + `left join emp_expr` + `<collection>`，**一次查完**。
（方案 A 的完整写法在当时的对话里，需要时可以让我再贴一次。）

**验收标准**：
- 改造后 `GET /emps/{id}` 返回的 `exprList` 条数和内容都正确
- **只发一条 SQL** —— 开 `logging.level.com.wanna.webmanagement.mapper=debug`，数一下 `Preparing:` 出现几次

**提醒时机**：
- **等 Emp 模块彻底稳定之后**（现在正在收尾，别打断节奏）
- 或者**第一次遇到"分页 + 一对多"组合**的时候 —— 那时会真正体会到"join 会把结果集放大、导致分页数量算错"的坑
- 后面做「试卷 + 题目列表」「知识点 + 题目列表」时一定会再用到

---

## 4. 配置外置化与秘钥管理（环境变量 / Profile / 配置中心）

**背景**：接入阿里云 OSS 时，把 `access-key-id` / `access-key-secret` 直接写进了
`application.yaml` 并提交，`git push` 被 GitHub Push Protection 拦下
（`GH013: Push cannot contain secrets`）。
当时把两项改成 `${ALIYUN_OSS_ACCESS_KEY_ID:}` 环境变量占位符才推上去 ——
这是**临时绕过**：只解决了「这一个文件」，没解决「配置该怎么按环境管理」。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | Spring Boot 配置优先级 | 命令行参数 > 环境变量 > `application-{profile}.yaml` > `application.yaml`。要能解释**为什么环境变量能覆盖打包进 jar 的配置** |
| 2 | `${ENV:default}` 占位符 | `:` 后面是默认值，写成 `${X:}` 表示「取不到就用空串」。以及 `ALIYUN_OSS_ACCESS_KEY_ID` → `aliyun.oss.access-key-id` 的**宽松绑定（relaxed binding）**规则 |
| 3 | Profile 多环境 | `application-dev.yaml` / `application-prod.yaml` + `spring.profiles.active`；把本地敏感配置放进 `application-local.yaml` 并写进 `.gitignore` |
| 4 | `@ConfigurationProperties` vs `@Value` | 本项目 `OssProperties` 用的是前者。弄清它怎么做类型转换、松散绑定，以及配 `@Validated` 做启动期校验 |
| 5 | **秘钥泄露的处置流程** | 核心认知：**秘钥一旦进入提交历史，改文件是没用的**（历史里还在），必须去阿里云 RAM 控制台**禁用旧 Key + 新建一对**。GitHub 提示里的 "unblock secret" 链接是「允许带着秘钥推」，**绝不要点** |
| 6 | Git 历史里的秘钥怎么清 | 未推送 → `git commit --amend`（本次就是）；已推送 → `git filter-repo` / BFG + 强推。顺带理解 reflog 和「悬挂对象」还留着旧提交 |
| 7 | `.gitignore` 该忽略什么 | 已在仓库根加好 `.gitignore`（`target/`、`.idea/`、`*.iml`、`logs/`、`application-local.yaml` 等），并用 `git rm -r --cached` 停掉了旧跟踪。要自己搞懂的是**为什么光写规则不生效、必须先 `--cached`**（已跟踪的文件不受 ignore 约束），以及「IDE 配置到底该不该进仓库」这个判断 |
| 8 | 生产级方案（了解即可） | 配置中心（Nacos / Apollo）、KMS、云厂商 RAM 角色 / STS 临时凭证 |

**提醒时机**：
- **下一次要往配置文件里写任何密码、密钥、Token 的时候**（提前拦住，而不是等 push 失败）
- M9 工程化收尾（要把 `application.yaml` 里的数据库口令一起外置化）

---

## 5. OSS 鉴权：RAM 最小权限 → STS 临时凭证 → 客户端直传

**背景**：本项目现在用「**RAM 用户的永久 AccessKey + 环境变量**」访问 OSS。
这已经比主账号 AK 好得多，但官方文档明确建议**优先用 STS 临时凭证**。
现阶段先用永久 AK（简单够用），把 STS 留到「前端直传」或「部署到服务器」时再补。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | 四种凭证的定位 | 主账号 AK（最危险，别用）→ RAM 用户永久 AK（能长期用，泄露即长期风险）→ STS 临时凭证（有效期 + 可再限权）→ ECS 实例 RAM 角色（代码里干脆不出现密钥） |
| 2 | 最小权限策略怎么写 | 自定义策略 + `acs:oss:*:*:<bucket>/<前缀>/*`；只给用得上的 Action（本项目够用：`oss:PutObject`；要服务端读图再加 `oss:GetObject`）；**不要**用 `AliyunOSSFullAccess` |
| 3 | Action 与 Resource 的对应 | 对象级操作（Put/Get/DeleteObject）的 Resource 是 `bucket/前缀/*`；bucket 级操作（如 ListObjects）的 Resource 是 **bucket 本身**。写错就是 `AccessDenied` |
| 4 | STS 三步 | ① RAM 用户加 `AliyunSTSAssumeRoleAccess`（这只管「能不能调用 AssumeRole」，与 OSS 权限无关）→ ② 建 RAM 角色（信任主体 = 当前云账号）→ ③ 给角色挂 OSS 策略，再 AssumeRole 换临时凭证 |
| 5 | 两个必须记住的坑 | ① **不能用主账号 AK 调用 AssumeRole**（直接报错）；② AssumeRole 的 `policy` 参数与角色自身策略是**取交集**，不是覆盖 |
| 6 | 有效期 | `DurationSeconds` 最小 900 秒；角色会话默认 3600 秒、可放宽到最大 43200 秒；过期后必须重新获取 |
| 7 | 在代码里的落点 | 只需改 `OssConfig.ossClient()` 这一个 `@Bean`：`StaticCredentialsProvider` → 带 `SecurityToken` 的凭证提供者。**这就是把 client 抽成 Bean 的回报** |
| 8 | 私有 Bucket 下前端怎么显示图片 | 三条路的取舍：公共读（简单但任何人可读）、**预签名 URL**（推荐，服务端签发、有时效）、CDN + 回源鉴权。**永远不要用「公共读写」** |
| 9 | 客户端直传 | 服务端只签发临时凭证/签名，浏览器直传 OSS。此时必须：绝不把永久 AK 交给前端、配 CORS、用 policy 限制目录和文件大小 |

**官方文档**：
- [使用 RAM 用户访问密钥访问 OSS](https://help.aliyun.com/zh/oss/developer-reference/use-the-accesskey-pair-of-a-ram-user-to-initiate-a-request)
- [使用 STS 临时访问凭证访问 OSS](https://help.aliyun.com/zh/oss/developer-reference/use-temporary-access-credentials-provided-by-sts-to-access-oss)
- [创建 RAM 用户](https://help.aliyun.com/zh/ram/user-guide/create-a-ram-user)

**提醒时机**：
- **M5 前端**（要把上传放到页面上、要显示头像）时
- 或**第一次把项目部署到服务器/ECS 上跑**时（那时改用实例 RAM 角色，代码里连密钥都不需要）

---

## 6. SQL `CASE` 表达式 与「SELECT 别名不能在 WHERE 中引用」

**背景**：`ClazzMapper.list` 需要算出班级状态。数据库 `clazz` 表**没有 `status` 列**，
所以用 `case` 表达式现算一个虚拟列：

```sql
case
    when curdate() < c.begin_date then '未开班'
    when curdate() > c.end_date   then '已结课'
    else '在读'
end as status
```

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | 两种写法 | **搜索 CASE**：`case when 条件 then 值 ... end`；**简单 CASE**：`case 列 when 值 then 值 ... end`。本项目用的是前者 |
| 2 | 三个必写的部分 | `end` 不能省；`case` 后**不能加括号**（它不是函数！）；`as 别名` 在 MyBatis 自动映射里**不能省**，否则 `Clazz.status` 永远是 `null` |
| 3 | **短路求值** | 从上往下逐条判断，命中第一条就返回，后面不再看 → **`when` 的顺序会影响结果**。区间判断里要把「更严格的条件」放前面 |
| 4 | `else` 缺省 | 全部不匹配且**没有 `else`** 时返回 **`NULL`**（不是空串、不是报错）。想避免前端处理空值就写 `else` |
| 5 | 区间边界的闭开性 | 「开课当天」「结课当天」算不算在读？`<=` / `<` 一字之差就是业务语义。现在用 `else` 兜底 = 两端都算「在读」 |
| 6 | `curdate()` vs `now()` | `curdate()` 只有日期（`2026-10-16`），`now()` 带时分秒。与 `date` 类型的列比较**必须用 `curdate()`**，否则结课当天会因为 `20:00 > 00:00` 而**提前一天**判成「已结课」 |
| 7 | **执行顺序（本节最核心）** | `FROM → JOIN → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT`。`WHERE` 比 `SELECT` **先**执行 → **WHERE 里不能用 SELECT 的别名**，报 `Unknown column 'xxx' in 'where clause'`；`ORDER BY` / `HAVING` 在 `SELECT` 之后，**可以**用别名 |
| 8 | 派生列不是物理列 | 虚拟列**不能**出现在 `update ... set status = ?` 里，也无法建索引。要按状态统计，最终仍要回到 `begin_date` / `end_date` 做条件，或在 Service 层用 Java 算 |

**在本项目里的落点**：
- [ClazzMapper.xml](web-management/src/main/resources/com/wanna/webmanagement/mapper/ClazzMapper.xml) 的 `list` —— 现在写了一半（缺 `end`、缺 `as status`，且 `case(` 多了括号），要补完
- **前端一旦要「按状态筛选班级」**，就会撞上知识点 7。两条出路：
  - 方案 A：把 CASE **原样再抄一遍**放进 `WHERE`（改一处要改两处，易漏）
  - 方案 B（推荐）：子查询包一层 —— `select * from (原查询) t where t.status = #{status}`
- ⚠️ 用手写 `limit #{start}, #{pageSize}` 分页时，选了方案 B 后 **`count` 查询也要跟着包一层**，否则总数算错
  （若走 PageHelper，它生成 count SQL 时对子查询是支持的）

**提醒时机**：
- **给班级列表加「状态」筛选条件的那一刻**（也就是必须解决「别名不能在 WHERE 里用」的时候）
- 任何时候写出 `Unknown column ... in 'where clause'` 这个报错时，先回来确认执行顺序

---

## 7. SQL 三值逻辑：`NULL` 参与比较的结果是「未知」

**背景**：上面那个 `case` 里，如果有班级的 `end_date` 还没定（`NULL`），
第二个条件 `curdate() > c.end_date` 的结果是 **`NULL`（未知）**，**不是 `false`**——
于是它落到 `else`，班级显示「在读」。这个行为恰好合乎预期，但**纯属侥幸**：
一旦某个 `when` 依赖 `NULL` 列比较，结果就会变得难以预测。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | 三值逻辑 | SQL 布尔运算有 **`TRUE` / `FALSE` / `UNKNOWN`** 三种结果。任何与 `NULL` 的比较（`=`、`>`、`<`、`!=`）结果都是 **`UNKNOWN`**，而不等于 `FALSE` |
| 2 | `NULL = NULL` 是假 | 连自己都不等于自己，必须写 `IS NULL` / `IS NOT NULL`（MySQL 的 `<=>` 是「安全等于」，可比较两列是否都为 NULL） |
| 3 | **`NOT IN` + NULL 是灭顶之灾** | `x NOT IN (1, NULL)` 永远为 `UNKNOWN` → **查不出任何行**。这是「SQL 看着没错、结果全空」的头号元凶（对应 `IN` 侧则是 `x IN (1, NULL)` 只匹配到 1） |
| 4 | `WHERE` 只保留 `TRUE` | `FALSE` 和 `UNKNOWN` **都会被过滤掉** —— 所以「漏数据」通常不是条件写反，而是 NULL 让条件变成了 UNKNOWN |
| 5 | 聚合函数的忽略规则 | `count(*)` 数所有行；`count(列)` **跳过 NULL**；`sum`/`avg` 忽略 NULL（`avg` 的分母因此变小）；想让 NULL 参与就用 `ifnull()` / `coalesce()` |
| 6 | `!=` 的陷阱 | `where status != 1` **不会**选出 `status IS NULL` 的行，很多人以为它会 |
| 7 | 约束与默认值 | `NOT NULL DEFAULT` 设计时就要挡住；本项目约定时间字段由 Service 层赋值（`LocalDateTime.now()`），因此不允许为 NULL |
| 8 | Java 侧的呼应 | `Integer` 拆箱为 `int` 遇 `null` 抛 `NullPointerException`；所以 POJO 时间/数字字段用包装类型（`Integer`、`LocalDate`）而不是基本类型 |

**本项目里的检查动作**：
- 回头确认 `clazz.end_date`、`clazz.begin_date` 在表结构里**是否允许为 NULL**
- 复习 [EmpMapper.xml](web-management/src/main/resources/com/wanna/webmanagement/mapper/EmpMapper.xml) 那个 `getExistIds` 的 `foreach`：
  若 `ids` 集合里混入一个 `null` 元素，`id in (3, NULL)` 会**静默漏掉结果** —— 集合入口处就该过滤 `null`

**提醒时机**：
- **每次写含 `NULL` 可能的条件时**（尤其 `NOT IN`、`!=`、`count(列)`）先问一句「三值逻辑会怎么走」
- **「SQL 看着没错但查不出数据」时**的第一个怀疑对象
- 排查 `@Transactional` 的怪异行为时也常绕到「`default`/`NULL` 导致条件失效」这条线上

---

## 8. Git 核心知识：三区模型、`.gitignore` vs `.gitattributes`、行尾策略

> **备注：复习内容 + 实践内容** —— 三区模型是学过的，行尾与属性声明是这次 `git add` 提示
> `LF will be replaced by CRLF` 才真正用上的，需要在项目里落地一次才算掌握。

**背景**：`git add` 时出现

```
in the working copy of '.../Need_to_learn.md', LF will be replaced by CRLF the next time Git touches it
```

追查发现：`core.autocrlf=true`（Windows 版 Git 安装时默认勾选）加上**仓库根没有 `.gitattributes`**，
导致根目录文件（`Need_to_learn.md`、`AGENTS.md`）没有任何行尾规则覆盖。
`web-management/.gitattributes` 里已有的两条规则（`/mvnw text eol=lf`、`*.cmd text eol=crlf`）
**只管得住自己所在目录及以下** —— 这是本次踩到的关键点。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | **三区模型** | 工作区（working tree）→ 暂存区（index / staging）→ 仓库（repository / HEAD）。`git add` = 工作区 → 暂存区；`git commit` = 暂存区 → 仓库。理解这三区，才能明白「未跟踪 / 已修改 / 已暂存」三种状态从哪来 |
| 2 | **`git status` 到底在比什么** | 左列 = 暂存区 vs HEAD，右列 = 工作区 vs 暂存区。`M ` 和 ` M` 一个字母的位置差别，含义完全不同 |
| 3 | `core.autocrlf` 三种取值 | `true`（Windows 默认）：提交 CRLF→LF，签出 LF→CRLF；`input`（Linux/Mac）：只提交时转换，签出不转；`false`：完全不管。**"替换成 CRLF"是发生在工作区的，仓库里始终是 LF** |
| 4 | **`.gitignore` vs `.gitattributes`** | `.gitignore` 管**未跟踪文件**（决定"要不要跟踪"）；`.gitattributes` 管**已跟踪文件**（决定"如何存储与比较"：行尾、二进制、diff 方式、合并策略、linguist 语言统计）。两者职责完全不同 |
| 5 | **作用域** | 两者都是「**所在目录及所有子目录**」。所以根目录的文件必须由**根目录**的 `.gitattributes` 覆盖，写在深层目录里管不到它 |
| 6 | 为什么会警告"LF will be replaced by CRLF" | 这是在 `git add` 时的提示：文件当前是 LF，但按 `autocrlf=true` 的策略，**下次签出会变成 CRLF**。是 info 级提示，不影响提交内容 |
| 7 | **为什么行尾重要** | `mvnw` 这类 Shell 脚本带 CRLF 会报 `bad interpreter: No such file or directory`（`^M` 被当成解释器路径的一部分）；`.cmd` 用 LF 又可能被 cmd.exe 解析异常。真实坑，不是洁癖 |
| 8 | 三条排查命令 | `git ls-files --eol`（看每个文件 `i/` 仓库、`w/` 工作区、`attr/` 规则三处行尾）；`git check-attr text eol -- <file>`（问某文件最终适用哪条规则）；`git config --get core.autocrlf` |
| 9 | 「整个文件都被改了」的诡异 diff | 先怀疑行尾规则，用知识点 8 的命令确认；确认后**单独提交一次**，别和真实改动混在一起 |
| 10 | **`.gitignore` 写完为什么不生效** | **已跟踪的文件不受 `.gitignore` 约束**。要让规则生效必须 `git rm -r --cached <path>` 停止跟踪（工作区文件保留），再提交 |
| 11 | `git add --renormalize .` | 按新规则重新规范化已跟踪文件（行尾策略调整后使用）。**先看清影响范围再执行** |
| 12 | 其他常用面（了解 + 会用） | `git reflog` 找回误删提交；`git revert`（新增反向提交，安全）vs `git reset --hard`（改写历史，危险）；`git stash` 暂存现场；`git diff --cached`（看暂存区） |

**在本项目里的落点**：
- 已在**仓库根**新建 `.gitattributes`：`* text=auto eol=lf` 统一 LF，`*.cmd text eol=crlf`，二进制显式声明 `binary`
- `web-management/.gitattributes` 的 `/mvnw text eol=lf` 现在与根规则重复了，**可以保留**（无害），不用专门清理
- 根目录 `.gitattributes` 生效后：规则从「每台机器各自的全局配置」搬到「**跟着仓库走的声明文件**」，
  别人克隆或将来在 Linux/CI 上构建，拿到的行尾都一致

**提醒时机**：
- 再遇到「`git diff` 显示整个文件都变了」的时候
- 调整 `.gitattributes` / `core.autocrlf` / 行尾策略时
- 写 `.gitignore` 却发现不生效时（回到知识点 10）

---

## 9. 操作日志异步化（`@Async` + ThreadLocal 跨线程传递）

**背景**：`aop/LogAspect` 现在在 `finally` 里**同步**执行 `operateLogMapper.insert(...)`。
功能正确，但每做一次增删改，接口都要多等一次数据库写入。
日志是"旁路需求"，不该拖慢主流程 —— 所以计划改成异步写入。

**这不是简单地加个 `@Async` 就完事**，它牵出两个必须搞懂的问题：
**线程池怎么配**、**ThreadLocal 里的登录用户 id 怎么带过去**。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | `@Async` 生效的两个前提 | ① 主类（或配置类）上有 `@EnableAsync`；② **必须通过 Spring 代理调用** —— 同类内部 `this.saveLog()` 不会异步（代理没被绕过） |
| 2 | `@Async` 方法的签名限制 | 只能是 `public`；返回值要么 `void`，要么 `Future<T>` / `CompletableFuture<T>`。返回 `void` 时异常**不会抛给调用方**（需要自定义 `AsyncUncaughtExceptionHandler`） |
| 3 | **默认线程池的坑** | 不配 Executor 时 Spring 用 `SimpleAsyncTaskExecutor` —— **每次调用新建一个线程、不复用**，并发高时线程爆炸。真实项目必须自定义 `ThreadPoolTaskExecutor` |
| 4 | 线程池核心参数怎么定 | `corePoolSize` / `maxPoolSize` / `queueCapacity` / `keepAliveSeconds` / 拒绝策略。日志任务的特点：**短、高频、可丢** → 更适合小 `maxPoolSize` + 有界队列 + `CallerRunsPolicy` |
| 5 | **ThreadLocal 跨线程必丢** | `BaseContext` 用的是 `ThreadLocal`，`@Async` 换了线程 → `getCurrentId()` 返回 `null`。这正是现在 `LogAspect` **必须在主线程里先取出 `operateEmpId`** 的原因（现有写法已经是安全的） |
| 6 | 为什么 `InheritableThreadLocal` 也救不了 | 它只在**创建子线程时**复制一次值；而线程池的线程是**复用**的，不会重新创建 → 继承不到。（`@Async` 场景的经典误解） |
| 7 | 正确的跨线程传递方式 | `TaskDecorator`（把父线程的上下文复制到任务里）+ `RequestContextHolder` 的 `setRequestAttributes(..., true)`；或者干脆**把 id 作为方法参数传进去**（最简单可靠） |
| 8 | 日志写入失败要不要影响业务 | **不能影响**。现在 `insert` 外面包了 `try-catch` 就是这个意思 —— 日志是附属品，丢了可以忍，业务失败不可忍 |
| 9 | 另一种思路：消息队列 | 日志量大时，`@Async` 仍会占 JVM 内存和数据库连接。可以投递到 MQ，由消费者批量入库（了解即可，本项目用不上） |

**在本项目里的落点**：
- `aop/LogAspect.recordLog` —— 把 `insert` 抽成另一个 Bean 的 `@Async` 方法，
  或者配一个 `ThreadPoolTaskExecutor` 后直接 `executor.submit(...)`
- **改造时必须保证**：`operateEmpId` 在**切回异步之前**就已经从 `BaseContext` 取出来
  （现在代码在 `finally` 里调用 `setOperateEmpId(operateEmpId)`，`operateEmpId` 变量是主线程取的 ✅ 保持这个结构就行）

**提醒时机**：
- **接口响应时间明显变慢**、或者压测发现"写日志"占了大量耗时的时候
- 或者第一次需要"业务方法里再启一个并发任务"的时候（那时 `@Async` 这套知识会直接复用）

---

## 10. 操作日志查询接口（`GET /log/page`）：join 取姓名 + 长文本字段

**背景**：接口文档 5.5 定义了日志列表查询，但后端**还没实现**。
`operate_log` 表只有 `operate_emp_id`，而响应要求带 `operateEmpName`（操作人姓名），
所以查询必须 join `emp`。另外 `method_params` / `return_value` 都是 `varchar(2000)`，
返回给前端时要注意长度和可读性。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | 文档要求的响应结构 | `{ total, rows: [{ id, operateEmpId, operateTime, className, methodName, methodParams, returnValue, costTime, operateEmpName }] }` |
| 2 | **join 取姓名** | `operate_log l left join emp e on l.operate_emp_id = e.id` → `e.name as operateEmpName`。用 `left join` 而不是 `inner join`，因为如果操作人被删了，日志仍然要能显示（`operateEmpName` 为 null 而不是整条记录消失） |
| 3 | `count` 要不要 join | **不要**。`count` 只查 `operate_log` 单表就够了；画蛇添足地 join 会让总数在"一人多行"时算错（本场景不会，但养成习惯） |
| 4 | 分页参数 | `?page=1&pageSize=10`，与其它模块保持一致：`limit #{start}, #{pageSize}`，`start = (page-1)*pageSize` |
| 5 | 排序 | `order by l.operate_time desc, l.id desc` —— **加 `id` 兜底**，因为同一毫秒内可能有多条，只按时间排序结果不稳定 |
| 6 | 长文本怎么处理 | `method_params` 可能几百字符。① 前端用省略号 + 悬浮展示；② 后端出列表时可以 `left(method_params, 200)` 截断，详情接口再给全文 |
| 7 | `cost_time` 的类型 | 表里是 `bigint unsigned`，Java 用 `Long`（别用 `Integer`，虽然毫秒数装得下，但 `unsigned` 映射到 `Integer` 会溢出的风险） |
| 8 | 时间格式 | `operateTime` 是 `LocalDateTime`，默认序列化成 `2026-11-02T21:30:00`。若前端要 `2026-11-02 21:30:00`，用 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 或全局配置 |

**在本项目里的落点**：
- `OperateLog` 实体已经预留了 `operateEmpName` 字段 ✅，写查询时直接映射
- 需要新增：`OperateLogMapper.count()` / `list()`（XML 里写）、`ReportService`/新的 `LogService`、`LogController` 的 `GET /log/page`

**提醒时机**：
- **前端要做「操作日志」页面时**（M5 前端阶段）
- 或者要在答辩/展示时体现"有审计能力"的时候

---

## 11. Spring AOP：切点表达式 与「只代理 Spring Bean」

**背景**：这次做 `LogAspect` 用了 `@Around("@annotation(com.wanna.webmanagement.anno.Log)")`。
能跑通，但 AOP 有几条"不生效"的规则必须提前知道，
否则将来写事务、缓存、权限时会出现"注解明明加了却没反应"的诡异现象。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | 五种通知类型 | `@Before`（前置）、`@AfterReturning`（正常返回后）、`@AfterThrowing`（抛异常后）、`@After`（finally 语义）、`@Around`（最强，能改参数/返回值/吞异常）。**要记返回值、耗时、异常三样，只有 `@Around` 够用** |
| 2 | 切点表达式的四种常见写法 | `execution(* com.wanna..*Service.*(..))`（按包+方法签名）；`@annotation(注解全限定名)`（按注解）；`within(类名)`（按类）；`bean(beanName)`（按 Bean 名）。本项目用 `@annotation` 最贴合业务 |
| 3 | `execution` 的六个部分 | `execution(修饰符? 返回类型 包.类.方法名(参数) throws?)`。`*` 匹配任意，`..` 匹配任意层包/任意参数。**写错一个字符切点就可能匹配不到，且不报错** |
| 4 | **AOP 只代理 Spring 容器里的 Bean** ⭐ | `new` 出来的对象不会被代理 → 切面不生效。**这是最重要的一条**：`@Service` / `@Component` 上的注解能生效，自己 `new` 的不能 |
| 5 | **自调用导致切面失效** ⭐ | 同一个类里 `this.methodB()` 调用带注解的 `methodB` → **不经过代理 → 切面/事务都不生效**。这正是 `@Transactional` 最经典的坑（`@Async` 同理） |
| 6 | 怎么解决自调用 | ① 把方法拆到另一个 Bean；② 注入自己（`@Autowired private XxxService self;`）然后 `self.methodB()`；③ `AopContext.currentProxy()`（需开 `@EnableAspectJAutoProxy(exposeProxy = true)`） |
| 7 | JDK 动态代理 vs CGLIB | JDK 基于**接口**（注入时必须用接口类型）；CGLIB 基于**继承**（生成子类，所以 `final` 类/`final` 方法无法代理）。Spring Boot 2.x 起**默认用 CGLIB** |
| 8 | `joinPoint.proceed()` 的含义 | 调用"下一个通知或目标方法"。`@Around` 里**必须调用它**，否则业务方法根本不会执行（这也是写日志/权限切面时"接口返回 null、什么都没做"的常见原因） |
| 9 | `ProceedingJoinPoint` 能拿到什么 | `getTarget()`（目标对象）、`getSignature()`（方法签名：类名、方法名）、`getArgs()`（参数数组）、`getThis()`（代理对象）。**`getTarget().getClass().getName()` 和 `getSignature().getDeclaringTypeName()` 的区别**：前者是实际类，后者是声明类（对子类/继承场景不同） |
| 10 | 切面的多个顺序 | 多个切面作用于同一方法时，用 `@Order(数字)` 控制顺序，数字越小越外层。**顺序错了可能导致"事务提交了但日志还没写"之类的时序问题** |

**在本项目里的落点**：
- `aop/LogAspect` —— 现在能正常工作，但要验证一个点：**`@Log` 加在 Controller 上，而 Controller 是 Spring Bean**（`@RestController`），所以生效 ✅
- **将来若在 Service 层也加日志切面**，注意 Service 之间的自调用会失效

**提醒时机**：
- **任何一次"注解加了但不生效"的排查**（事务不生效、日志不记录、缓存没命中）
- 或者**第一次要写"业务方法内部再调本类另一个方法"的时候**（提前避开自调用坑）

---

## 12. Lombok `@ToString` 家族 与 敏感字段防泄露

**背景**：做 AOP 操作日志时，`LogAspect` 记录 `Arrays.toString(joinPoint.getArgs())` ——
如果参数是 `Emp` 对象，`@Data` 自动生成的 `toString()` 会把 `password` 一起写进数据库的
`operate_log.method_params` 列。**操作日志表里存明文密码**，这是真实的安全事故。

修复方式：在 `password` 字段上加 `@ToString.Exclude`。
这个注解属于 Lombok 的 `@ToString` 家族，值得系统搞清楚。

**要掌握的知识点**：

| # | 知识点 | 具体内容 |
|---|---|---|
| 1 | **`@Data` 包含了什么** ⭐ | `@Data` = `@Getter` + `@Setter` + `@RequiredArgsConstructor` + **`@ToString`** + **`@EqualsAndHashCode`**。所以加 `@Data` 就自动有了 `toString()`，**它会把所有非 static 字段都打出来** |
| 2 | `@ToString.Exclude` | 让某个字段**不出现在 `toString()` 里**。最常用，一字段一行 |
| 3 | `@ToString.Of` / `@ToString.OnlyExplicitlyIncluded` | 白名单模式：类上加 `@ToString(onlyExplicitlyIncluded = true)`，字段上只有标了 `@ToString.Include` 的才输出。**字段多、敏感字段也多时更安全**（默认不输出，而非默认输出） |
| 4 | **`@EqualsAndHashCode` 同理** ⭐ | `@Data` 生成的 `equals`/`hashCode` 也包含所有字段。如果 `password` 参与 `equals`，会出现"密码不同就是不同对象"的奇怪行为（比如放进 `Set` 时）。敏感字段同样要 `@EqualsAndHashCode.Exclude` |
| 5 | `@ToString(callSuper = true)` | 继承场景下默认**不打印父类字段**，需要显式开启。写了 `extends` 的实体要检查 |
| 6 | `@ToString(exclude = {"password"})` | 类级别的写法，效果同字段级 `@ToString.Exclude`。**字段分散时用类级集中管理更清晰** |
| 7 | **三个出口要一起堵** ⭐ | 敏感字段（密码、密钥、身份证）有三个泄露出口：① **JSON 序列化**（`@JsonProperty(access = WRITE_ONLY)`）；② **`toString()`**（`@ToString.Exclude`）；③ **`equals/hashCode`**（`@EqualsAndHashCode.Exclude`）。**只堵一个等于没堵** |
| 8 | 自定义 `toString()` | Lombok 检测到你自己写了 `toString()` 时**不会生成**，会直接用它。可以在里面做脱敏（比如 `password=***`） |
| 9 | 其他易踩的 Lombok 默认行为 | `@Data` 生成的 `equals` **不含父类字段**（除非 `callSuper=true`）；`@Builder` 与 `@NoArgsConstructor` 的冲突需要 `@AllArgsConstructor` 配合；`@SneakyThrows` 会吞掉受检异常声明 |

**在本项目里的落点**：
- `pojo/Emp.password` 已经加了 `@JsonProperty(access = WRITE_ONLY)` + `@ToString.Exclude` ✅
- **待检查**：`Emp` 的 `@EqualsAndHashCode` 是否也该排除 `password`（目前 `@Data` 默认包含它）
- 以后加 `User`（学员登录）、或任何含 `token`、`phone`、`idCard` 的实体时，按知识点 7 一次性把三个出口都堵上
  （`Student` 里已经有 `phone` 和 `idCard`，虽然目前不算敏感到要脱敏，但知道这个思路）

**提醒时机**：
- **任何一次"日志/响应/报错信息里出现了密码、手机号、身份证"的时候**
- 定义新实体、尤其是带敏感字段的实体时（提前堵，别等漏了再补）
- 排查"两个对象明明字段一样却不 `equals`"的诡异问题时（回到知识点 4）
