# 使用说明

## 系统部署步骤

### 1. 环境准备

确保已安装以下软件：
- JDK 1.8 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/sql/init.sql
```

或者直接执行：
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
      password: 你的密码
```

### 4. 编译项目

```bash
mvn clean install
```

### 5. 启动项目

```bash
mvn spring-boot:run
```

或者直接运行jar包：
```bash
java -jar target/photo-generate-1.0.0.jar
```

### 6. 访问系统

打开浏览器访问：http://localhost:8080/api

## 使用流程

### 1. 用户注册

1. 访问注册页面：http://localhost:8080/api/register.html
2. 填写用户名、密码（必填）
3. 可选填写邮箱和手机号
4. 点击注册按钮

### 2. 用户登录

1. 访问登录页面：http://localhost:8080/api/login.html
2. 输入用户名和密码
3. 点击登录按钮
4. 登录成功后自动跳转到主页面

### 3. 配置模板（管理员）

**注意：首次使用需要管理员先配置模板**

1. 使用管理员账号登录（admin / admin123）
2. 准备一张背景图片
3. 使用Postman或其他工具调用模板上传接口：

```bash
POST http://localhost:8080/api/admin/template/upload
Authorization: Bearer {你的token}
Content-Type: multipart/form-data

file: {选择你的背景图片}
```

4. 保存模板配置：

```bash
POST http://localhost:8080/api/admin/template/save
Authorization: Bearer {你的token}
Content-Type: application/json

{
  "templateName": "我的模板",
  "imagePath": "./templates/上传的图片路径",
  "textX": 100,
  "textY": 200,
  "textWidth": 300,
  "textHeight": 100,
  "fontSize": 24,
  "fontColor": "#000000",
  "status": 1
}
```

**参数说明：**
- `textX`: 文字区域左上角X坐标
- `textY`: 文字区域左上角Y坐标
- `textWidth`: 文字区域宽度
- `textHeight`: 文字区域高度
- `fontSize`: 字体大小
- `fontColor`: 字体颜色（十六进制，如 #000000）
- `status`: 1-启用，0-禁用

### 4. 单次生成图片

1. 登录后进入主页面
2. 选择"单次生成"标签
3. 选择模板
4. 输入要合成的文字
5. 点击"生成图片"按钮
6. 查看生成的图片预览

### 5. 批量生成图片

1. 登录后进入主页面
2. 选择"批量生成"标签
3. 选择模板
4. 输入批量文字（每行一条）
5. 点击"批量生成"按钮
6. 查看生成的图片预览
7. 点击"导出图片"按钮下载ZIP文件

### 6. Excel上传生成

1. 准备Excel文件，第一列为文字内容
2. 登录后进入主页面
3. 选择"Excel上传"标签
4. 选择模板
5. 点击上传区域选择Excel文件
6. 等待处理完成
7. 查看生成的图片预览
8. 点击"导出图片"按钮下载ZIP文件

### 7. 查看使用明细（管理员）

1. 使用管理员账号登录
2. 调用使用明细接口：

```bash
GET http://localhost:8080/api/admin/usage-records?page=1&size=10
Authorization: Bearer {你的token}
```

### 8. 查看统计信息（管理员）

```bash
GET http://localhost:8080/api/admin/statistics
Authorization: Bearer {你的token}
```

## 常见问题

### 1. 登录后提示"未登录"

**原因：** Token可能已过期或Session失效

**解决方法：**
- 重新登录
- 检查Session配置，默认20分钟过期

### 2. 生成图片失败

**原因：** 模板配置不正确或模板图片不存在

**解决方法：**
- 检查模板图片路径是否正确
- 检查模板配置参数是否合理
- 确保模板图片已上传到正确位置

### 3. Excel上传失败

**原因：** Excel格式不正确或文件损坏

**解决方法：**
- 确保Excel文件格式为.xlsx或.xls
- 确保第一列为文字内容
- 检查文件是否损坏

### 4. 导出图片失败

**原因：** 没有可导出的图片或权限不足

**解决方法：**
- 确保已生成图片
- 检查用户权限

### 5. 数据库连接失败

**原因：** 数据库配置错误或数据库未启动

**解决方法：**
- 检查数据库连接信息
- 确保MySQL服务已启动
- 检查数据库是否已创建

## 安全建议

1. **修改默认密码**
   - 首次登录后立即修改admin账号密码
   - 使用强密码（包含大小写字母、数字、特殊字符）

2. **密码加密**
   - 当前版本密码未加密，生产环境建议使用BCrypt加密
   - 修改UserServiceImpl中的密码处理逻辑

3. **Token安全**
   - 修改JWT密钥（application.yml中的jwt.secret）
   - 定期更换Token密钥

4. **文件存储**
   - 当前版本文件存储在项目目录，生产环境建议使用云存储
   - 限制上传文件大小和类型

5. **访问控制**
   - 配置防火墙规则
   - 使用HTTPS协议
   - 限制管理员接口访问IP

## 性能优化

1. **数据库优化**
   - 为常用查询字段添加索引
   - 定期清理过期数据
   - 使用连接池（已配置Druid）

2. **缓存优化**
   - 考虑使用Redis缓存模板数据
   - 缓存用户信息

3. **文件优化**
   - 使用CDN加速图片访问
   - 压缩生成的图片

## 扩展功能建议

1. **用户功能**
   - 用户个人中心
   - 修改密码
   - 查看个人使用记录

2. **模板功能**
   - 模板预览
   - 模板分类
   - 模板分享

3. **图片功能**
   - 图片编辑
   - 图片滤镜
   - 批量下载

4. **系统功能**
   - 邮件通知
   - 短信验证
   - 第三方登录

## 技术支持

如有问题，请联系技术支持或查看项目文档。