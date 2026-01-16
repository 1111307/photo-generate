# 图片文字合成系统

基于SpringBoot + MyBatisPlus实现的图片文字合成系统，支持单次生成、批量生成、Excel上传等功能。

## 功能特性

1. **用户认证**
   - 用户注册和登录
   - Token认证机制
   - Session管理（20分钟过期）
   - 自动刷新Token有效期

2. **图片生成**
   - 单次文字合成到图片
   - 批量文字合成到图片
   - Excel批量上传生成
   - 自定义模板配置（文字位置、字体大小、颜色等）

3. **导出功能**
   - 批量导出生成的图片为ZIP文件

4. **后台管理**
   - 用户管理
   - 使用明细查询
   - 统计信息展示
   - 模板管理

5. **安全特性**
   - 双重拦截器机制
   - ThreadLocal用户信息管理
   - 防止数据泄露
   - 请求结束自动清理ThreadLocal

## 技术栈

- **后端框架**: SpringBoot 2.7.14
- **ORM框架**: MyBatisPlus 3.5.3.1
- **数据库**: MySQL 8.0
- **连接池**: Druid 1.2.18
- **工具库**: Hutool 5.8.20
- **Excel处理**: Apache POI 5.2.3
- **JWT**: jjwt 0.9.1
- **前端**: HTML + CSS + JavaScript

## 项目结构

```
photo-generate/
├── src/main/java/com/photo/
│   ├── common/              # 公共类
│   │   └── Result.java      # 统一响应结果
│   ├── config/              # 配置类
│   │   └── WebMvcConfig.java # Web MVC配置
│   ├── controller/          # 控制器
│   │   ├── AuthController.java    # 认证控制器
│   │   ├── PhotoController.java   # 图片控制器
│   │   └── AdminController.java   # 管理员控制器
│   ├── entity/              # 实体类
│   │   ├── User.java              # 用户实体
│   │   ├── PhotoTemplate.java     # 图片模板实体
│   │   └── UsageRecord.java       # 使用记录实体
│   ├── interceptor/         # 拦截器
│   │   ├── TokenRefreshInterceptor.java  # Token刷新拦截器
│   │   └── AuthInterceptor.java          # 认证拦截器
│   ├── mapper/              # Mapper接口
│   │   ├── UserMapper.java
│   │   ├── PhotoTemplateMapper.java
│   │   └── UsageRecordMapper.java
│   ├── service/             # 服务层
│   │   ├── UserService.java
│   │   ├── PhotoService.java
│   │   └── impl/
│   ├── util/                # 工具类
│   │   ├── JwtUtil.java     # JWT工具类
│   │   └── UserContext.java # 用户上下文工具类
│   └── PhotoGenerateApplication.java # 启动类
├── src/main/resources/
│   ├── sql/
│   │   └── init.sql         # 数据库初始化脚本
│   ├── templates/           # 前端页面
│   │   ├── login.html       # 登录页面
│   │   ├── register.html    # 注册页面
│   │   └── index.html       # 主页面
│   └── application.yml      # 配置文件
├── pom.xml                  # Maven配置文件
└── README.md                # 项目说明文档
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

创建数据库并执行初始化脚本：

```bash
mysql -u root -p < src/main/resources/sql/init.sql
```

### 3. 修改配置文件

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/photo_generate?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: root
      password: your_password
```

### 4. 启动项目

```bash
mvn clean install
mvn spring-boot:run
```

### 5. 访问系统

- 访问地址: http://localhost:8080/api
- 默认管理员账号: admin / admin123

## API接口文档

### 认证接口

#### 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "test",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

#### 用户登出
```
POST /api/auth/logout
Authorization: Bearer {token}
```

### 图片接口

#### 获取模板列表
```
GET /api/photo/templates
Authorization: Bearer {token}
```

#### 单次生成图片
```
POST /api/photo/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "text": "测试文字",
  "templateId": 1
}
```

#### 批量生成图片
```
POST /api/photo/batch-generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "textList": ["文字1", "文字2", "文字3"],
  "templateId": 1
}
```

#### Excel上传生成
```
POST /api/photo/upload-excel
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: {excel文件}
templateId: 1
```

#### 导出图片
```
POST /api/photo/export
Authorization: Bearer {token}
Content-Type: application/json

{
  "imagePaths": ["/uploads/xxx.png", "/uploads/yyy.png"]
}
```

### 管理员接口

#### 获取用户列表
```
GET /api/admin/users?page=1&size=10
Authorization: Bearer {token}
```

#### 获取使用明细
```
GET /api/admin/usage-records?page=1&size=10&userId=1
Authorization: Bearer {token}
```

#### 获取统计信息
```
GET /api/admin/statistics
Authorization: Bearer {token}
```

#### 上传模板图片
```
POST /api/admin/template/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: {图片文件}
```

#### 保存模板配置
```
POST /api/admin/template/save
Authorization: Bearer {token}
Content-Type: application/json

{
  "templateName": "测试模板",
  "imagePath": "./templates/xxx.jpg",
  "textX": 100,
  "textY": 200,
  "textWidth": 300,
  "textHeight": 100,
  "fontSize": 24,
  "fontColor": "#000000",
  "status": 1
}
```

## 拦截器说明

### Token刷新拦截器（第一次拦截）
- 拦截所有请求
- 检查token是否在session中
- 如果在session中，刷新session有效期（20分钟）
- 无论token是否在session中，都放行

### 认证拦截器（第二次拦截）
- 拦截除登录、注册外的所有请求
- 检查请求是否携带token
- 如果不携带token，直接拦截
- 如果携带token，从session中获取用户信息
- 用户信息存在则放入ThreadLocal
- 请求结束后自动清理ThreadLocal，防止数据泄露

## 注意事项

1. **密码安全**: 当前版本密码未加密，生产环境请使用BCrypt等加密方式
2. **文件存储**: 默认存储在项目目录下，生产环境建议使用云存储
3. **Session管理**: Session默认20分钟过期，可根据需要调整
4. **并发处理**: ThreadLocal确保线程安全，但请求结束必须清理
5. **模板配置**: 首次使用需要管理员在后台配置模板

## 许可证

MIT License