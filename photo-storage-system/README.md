# 照片存储系统 (Photo Storage System)

基于Spring Boot开发的照片上传下载系统，提供安全、高效的图片存储和管理服务。

## 功能特性

### 核心功能
- **单文件/多文件上传**：支持批量上传，单次最多10个文件
- **文件类型验证**：仅允许JPG、PNG、GIF、WEBP、BMP格式
- **文件大小限制**：单个文件最大10MB
- **断点续传下载**：支持HTTP Range请求，实现断点续传
- **在线预览**：直接查看图片，无需下载
- **缩略图生成**：自动创建缩略图，优化加载速度

### 安全特性
- **文件类型安全检查**：基于MIME类型和文件魔数双重验证
- **文件名安全处理**：防止路径遍历和XSS攻击
- **防盗链保护**：基于Referer的访问控制
- **请求频率限制**：基于IP的限流保护
- **文件内容检测**：检测恶意文件伪装

### 性能优化
- **上传进度监控**：实时跟踪上传进度
- **图片压缩**：支持上传时压缩图片
- **缓存机制**：Caffeine缓存，加速文件信息查询
- **断点续传**：大文件下载优化

### 存储管理
- **存储容量监控**：实时监控存储使用情况
- **自动清理**：定期清理过期文件
- **重复文件检测**：基于MD5哈希去重

## 技术栈

- **框架**：Spring Boot 3.2.1
- **JDK**：Java 17
- **数据库**：H2（开发）/ 可切换至MySQL、PostgreSQL
- **缓存**：Caffeine
- **图片处理**：Thumbnailator
- **文档**：SpringDoc OpenAPI
- **构建工具**：Maven

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd photo-storage-system
```

### 2. 构建项目
```bash
mvn clean package
```

### 3. 运行项目
```bash
java -jar target/photo-storage-system-1.0.0.jar
```

或使用Maven插件：
```bash
mvn spring-boot:run
```

### 4. 访问服务
- API文档：http://localhost:8080/api/swagger-ui.html
- H2控制台：http://localhost:8080/api/h2-console

## API接口

### 文件上传
```bash
# 单文件上传
POST /api/files/upload
Content-Type: multipart/form-data

# 多文件上传
POST /api/files/upload/batch
Content-Type: multipart/form-data
```

### 文件下载
```bash
# 下载文件（支持断点续传）
GET /api/files/download/{fileId}

# 预览文件
GET /api/files/preview/{fileId}
```

### 文件管理
```bash
# 获取文件信息
GET /api/files/info/{fileId}

# 文件列表
GET /api/files/list?pageNum=1&pageSize=10

# 删除文件
DELETE /api/files/delete/{fileId}

# 存储统计
GET /api/files/stats
```

### 照片访问（简化接口）
```bash
# 查看照片
GET /api/photos/{fileId}

# 照片画廊
GET /api/photos/gallery

# 搜索照片
GET /api/photos/search?keyword=test
```

## 配置说明

### 文件存储配置 (application.yml)
```yaml
storage:
  upload-dir: ./uploads          # 上传目录
  thumbnail-dir: ./uploads/thumbnails  # 缩略图目录
  allowed-types:                 # 允许的文件类型
    - image/jpeg
    - image/png
    - image/gif
    - image/webp
    - image/bmp
  max-file-size: 10              # 最大文件大小(MB)
  generate-thumbnail: true       # 是否生成缩略图
  cleanup-interval-days: 30      # 清理间隔(天)
  max-storage-size: 10           # 最大存储容量(GB)
```

### 安全配置 (application.yml)
```yaml
security:
  anti-hotlink-enabled: true     # 启用防盗链
  allowed-referers:              # 允许的Referer
    - localhost
    - 127.0.0.1
  rate-limit: 100                # 每分钟请求限制
```

## 项目结构

```
photo-storage-system/
├── src/
│   ├── main/
│   │   ├── java/com/photostorage/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── exception/       # 异常处理
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── security/        # 安全相关
│   │   │   ├── service/         # 业务逻辑层
│   │   │   └── utils/           # 工具类
│   │   └── resources/
│   │       └── application.yml  # 配置文件
│   └── test/                    # 测试代码
├── pom.xml                      # Maven配置
└── README.md                    # 项目说明
```

## 测试

运行单元测试：
```bash
mvn test
```

## 部署

### Docker部署
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/photo-storage-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

构建并运行：
```bash
docker build -t photo-storage-system .
docker run -p 8080:8080 -v /host/uploads:/app/uploads photo-storage-system
```

## 注意事项

1. **生产环境**：请修改默认的API密钥和H2配置
2. **存储路径**：确保应用有写入权限
3. **备份策略**：定期备份uploads目录和数据库
4. **安全配置**：根据实际需求调整防盗链和限流配置

## 许可证

MIT License
