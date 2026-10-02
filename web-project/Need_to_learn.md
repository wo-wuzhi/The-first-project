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
