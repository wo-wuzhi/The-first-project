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
