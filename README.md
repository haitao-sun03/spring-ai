# Spring AI 智能对话系统

一个基于 Spring AI 框架构建的智能对话系统，集成了阿里云通义千问大模型、向量数据库和 RAG（检索增强生成）功能，支持文本对话、图像理解和文档知识库问答。

## 🚀 项目特性

- **多模态AI对话**：支持文本和图像输入的智能对话
- **RAG知识库**：基于向量数据库的文档检索增强生成
- **图像理解**：集成通义千问视觉模型，支持图像内容分析
- **对话记忆**：支持会话上下文记忆和历史对话管理
- **文档处理**：支持多种格式文档的向量化存储
- **RESTful API**：提供完整的REST API接口

## 🛠️ 技术栈

- **框架**：Spring Boot 3.5.0
- **AI框架**：Spring AI 1.0.0
- **大语言模型**：阿里云通义千问（Qwen-Plus、QVQ-Max）
- **向量数据库**：PostgreSQL + pgvector
- **数据库**：MySQL（对话历史）、PostgreSQL（向量存储）
- **文档处理**：Apache Tika
- **构建工具**：Maven
- **Java版本**：17

## 📋 系统要求

- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (需安装 pgvector 扩展)
- MySQL 8.0+
- 阿里云通义千问 API Key

## 🔧 安装配置

### 1. 克隆项目

```bash
git clone <repository-url>
cd ai
```

### 2. 数据库配置

#### PostgreSQL (向量数据库)
```sql
-- 创建数据库
CREATE DATABASE vector_db;

-- 安装 pgvector 扩展
CREATE EXTENSION vector;
```

#### MySQL (对话历史)
```sql
-- 创建数据库
CREATE DATABASE ai_chat;
```

### 3. 配置文件

修改 `src/main/resources/application.yaml`：

```yaml
spring:
  application:
    name: ai
  ai:
    dashscope:
      base-url: https://dashscope.aliyuncs.com
      api-key: your-dashscope-api-key  # 替换为你的API Key
      chat:
        options:
          model: qwen-plus
          temperature: 0.8
      embedding:
        options:
          model: text-embedding-v4
          dimensions: 1536
      vision:
        options:
          model: qvq-max
  datasource:
    url: jdbc:postgresql://localhost:5432/vector_db
    username: your-username
    password: your-password
    vector-store:
      pgvector:
        initialize-schema: true
        dimensions: 1536
```

### 4. 启动应用

```bash
# 使用 Maven 启动
./mvnw spring-boot:run

# 或者打包后运行
./mvnw clean package
java -jar target/ai-0.0.1-SNAPSHOT.jar
```

## 📚 API 接口

### 智能对话

#### 文本对话
```http
POST /ai/chat
Content-Type: multipart/form-data

sessionId=session123&input=你好，请介绍一下Spring AI
```

#### 图像对话
```http
POST /ai/chat
Content-Type: multipart/form-data

sessionId=session123&input=这张图片里有什么？&image=<image-file>
```

#### 删除对话历史
```http
DELETE /ai/deleteChat?sessionId=session123
```

### 文档管理

#### 上传文档到知识库
```http
POST /ai/upload
Content-Type: multipart/form-data

file=<document-file>
```

#### 查询知识库
```http
GET /ai/search?query=Spring AI是什么
```

## 🏗️ 项目结构

```
src/main/java/com/haitao/ai/
├── AiApplication.java              # 应用启动类
├── advisor/                        # 自定义顾问
│   └── ReReadingAdvisor.java      # 重读顾问
├── configuration/                  # 配置类
│   ├── CommonConfiguration.java   # 通用配置
│   ├── CommandRunners.java        # 启动运行器
│   ├── DBChatMemoryRepository.java # 数据库聊天记忆仓库
│   └── JacksonConfig.java         # JSON配置
├── controller/                     # 控制器
│   └── ChatController.java        # 聊天控制器
├── entity/                         # 实体类
│   └── ChatMessageEntity.java     # 聊天消息实体
├── model/                          # 模型类
│   └── ApiResponse.java           # API响应模型
├── repository/                     # 数据访问层
│   └── ChatMessageRepository.java # 聊天消息仓库
├── service/                        # 服务层
│   ├── ImageUnderstandService.java # 图像理解服务接口
│   ├── ImageUnderstandServiceImpl.java # 图像理解服务实现
│   └── VectorStoreManager.java    # 向量存储管理器
└── utils/                          # 工具类
    └── ImageUtils.java            # 图像工具类
```

## 🔍 核心功能说明

### 1. 多模态对话
- 支持纯文本对话
- 支持图像+文本的多模态对话
- 自动识别输入类型并调用相应的AI模型

### 2. RAG 知识库
- 使用 pgvector 存储文档向量
- 支持多种文档格式（PDF、Word、TXT等）
- 基于语义相似度的文档检索
- 结合检索结果生成准确回答

### 3. 对话记忆
- 基于数据库的对话历史存储
- 支持多会话管理
- 上下文感知的对话连续性

### 4. 图像理解
- 集成通义千问视觉模型
- 支持图像内容描述和分析
- 图像与文本的联合理解

## 🚨 注意事项

1. **API Key 安全**：请妥善保管阿里云 API Key，不要提交到版本控制系统
2. **数据库权限**：确保数据库用户有足够的权限创建表和扩展
3. **向量维度**：embedding 模型和向量数据库的维度必须一致（1536）
4. **文件大小**：上传文档时注意文件大小限制
5. **网络连接**：确保服务器能够访问阿里云 API 服务

## 🐛 常见问题

### Q: 启动时出现向量维度不匹配错误
A: 检查 `application.yaml` 中 embedding 和 vectorstore 的 dimensions 配置是否一致

### Q: 无法连接到阿里云API
A: 检查 API Key 是否正确，网络是否能访问 dashscope.aliyuncs.com

### Q: PostgreSQL 连接失败
A: 确认 PostgreSQL 服务已启动，用户名密码正确，并已安装 pgvector 扩展

## 📄 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 项目 Issues
- 邮箱：[your-email@example.com]

---

**注意**：使用前请确保已获得阿里云通义千问的使用授权，并遵守相关服务条款。