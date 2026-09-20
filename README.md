# Virtual Bank & Family Task Manager

一个基于 Java Swing 的家庭任务与虚拟账户管理应用。家长可创建家庭组、分配任务和管理虚拟账户，子女可加入家庭组并跟踪任务状态。

## 主要功能

- 家长/子女用户注册与登录
- 家庭组与任务分配
- 任务状态、紧急度、奖励及重复周期管理
- 活期账户、定期账户、存取款与转账
- JSON 本地数据持久化
- JFreeChart 图表支持

## 运行要求

- JDK 17 或更高版本
- Maven 3.9 或更高版本

## 构建和运行

```bash
mvn clean package
java -jar target/virtual-bank-1.0.0.jar
```

也可在 IntelliJ IDEA 中直接运行 `MainGUI.main()`。

## 测试

```bash
mvn test
```

自动化测试覆盖金额校验、交易查询和重复任务 ID/生成时机等核心规则。

## 数据与隐私

运行时会在当前目录生成 `users.json`、`accounts.json`、`tasks.json` 和 `transactions.json`。这些文件可能包含个人数据和本地凭据，已被 `.gitignore` 排除，不会提交到 GitHub。

> 本项目是课程/演示用途的本地应用，当前 JSON 凭据存储不适用于真实金融或生产环境。
