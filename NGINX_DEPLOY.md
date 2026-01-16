# Nginx部署说明

## 部署步骤

### 1. 启动SpringBoot后端服务

首先确保SpringBoot应用已经启动：

```bash
# 在项目根目录执行
mvn spring-boot:run
```

或者直接运行jar包：

```bash
java -jar target/photo-generate-1.0.0.jar
```

确保后端服务运行在 `http://localhost:8080`

### 2. 启动Nginx

进入nginx目录并启动：

```bash
cd nginx-1.20.2
nginx.exe
```

### 3. 访问系统

打开浏览器访问：http://localhost

## Nginx配置说明

### 配置文件位置
`nginx-1.20.2/conf/nginx.conf`

### 主要配置项

#### 1. 静态文件服务
```nginx
location / {
    root   html;
    index  index.html index.htm;
}
```
- 将前端页面（login.html、register.html、index.html）放在 `nginx-1.20.2/html/` 目录下
- 访问 http://localhost 会自动显示 index.html

#### 2. API反向代理
```nginx
location /api/ {
    proxy_pass   http://localhost:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```
- 将 `/api/` 开头的请求转发到SpringBoot后端
- 后端地址：http://localhost:8080/api/

#### 3. 文件访问代理
```nginx
location /uploads/ {
    proxy_pass   http://localhost:8080/uploads/;
}

location /exports/ {
    proxy_pass   http://localhost:8080/exports/;
}
```
- 处理上传和导出文件的访问

## Nginx常用命令

### 启动Nginx
```bash
nginx.exe
```

### 停止Nginx
```bash
nginx.exe -s stop
```

### 重新加载配置
```bash
nginx.exe -s reload
```

### 检查配置文件
```bash
nginx.exe -t
```

### 查看Nginx版本
```bash
nginx.exe -v
```

## 目录结构

```
nginx-1.20.2/
├── conf/
│   └── nginx.conf          # Nginx配置文件
├── html/
│   ├── index.html          # 主页面
│   ├── login.html          # 登录页面
│   ├── register.html       # 注册页面
│   └── 50x.html            # 错误页面
├── logs/
│   ├── access.log          # 访问日志
│   ├── error.log           # 错误日志
│   └── nginx.pid           # 进程ID文件
└── nginx.exe               # Nginx可执行文件
```

## 常见问题

### 1. 端口被占用

如果80端口被占用，修改 `nginx.conf` 中的监听端口：

```nginx
server {
    listen       8081;  # 改为其他端口
    server_name  localhost;
    ...
}
```

### 2. 后端服务未启动

确保SpringBoot应用已启动并运行在8080端口：

```bash
# 检查8080端口是否被占用
netstat -ano | findstr :8080
```

### 3. 静态文件404

确保前端页面已复制到 `nginx-1.20.2/html/` 目录下：

```bash
# 检查文件是否存在
dir nginx-1.20.2\html\
```

### 4. API请求失败

检查nginx配置中的proxy_pass地址是否正确：

```nginx
location /api/ {
    proxy_pass   http://localhost:8080/api/;  # 确保地址正确
}
```

### 5. 跨域问题

如果遇到跨域问题，可以在nginx配置中添加CORS头：

```nginx
location /api/ {
    proxy_pass   http://localhost:8080/api/;
    add_header Access-Control-Allow-Origin *;
    add_header Access-Control-Allow-Methods 'GET, POST, OPTIONS';
    add_header Access-Control-Allow-Headers 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization';
    
    if ($request_method = 'OPTIONS') {
        return 204;
    }
}
```

## 性能优化

### 1. 开启Gzip压缩

在 `nginx.conf` 的 http 块中取消注释：

```nginx
gzip  on;
gzip_min_length 1k;
gzip_buffers 4 16k;
gzip_comp_level 2;
gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/x-httpd-php image/jpeg image/gif image/png;
gzip_vary off;
```

### 2. 增加worker进程

```nginx
worker_processes  auto;  # 自动根据CPU核心数设置
```

### 3. 增加连接数

```nginx
events {
    worker_connections  2048;  # 增加连接数
}
```

## 安全建议

1. **隐藏Nginx版本号**
```nginx
http {
    server_tokens off;
}
```

2. **限制请求大小**
```nginx
client_max_body_size 10M;
```

3. **启用HTTPS**
在生产环境中建议使用HTTPS证书

## 日志查看

### 查看访问日志
```bash
type nginx-1.20.2\logs\access.log
```

### 查看错误日志
```bash
type nginx-1.20.2\logs\error.log
```

### 实时监控日志
```bash
# 使用PowerShell
Get-Content nginx-1.20.2\logs\access.log -Wait -Tail 10
```

## 完整启动流程

1. **启动数据库**
   ```bash
   # 确保MySQL服务已启动
   ```

2. **初始化数据库**
   ```bash
   mysql -u root -p < src/main/resources/sql/init.sql
   ```

3. **启动SpringBoot应用**
   ```bash
   mvn spring-boot:run
   ```

4. **启动Nginx**
   ```bash
   cd nginx-1.20.2
   nginx.exe
   ```

5. **访问系统**
   ```
   http://localhost
   ```

## 停止服务

### 停止Nginx
```bash
cd nginx-1.20.2
nginx.exe -s stop
```

### 停止SpringBoot
在运行SpringBoot的终端按 `Ctrl + C`

## 注意事项

1. 确保SpringBoot应用在Nginx之前启动
2. 修改nginx配置后需要重新加载：`nginx.exe -s reload`
3. 查看日志文件排查问题
4. 生产环境建议使用专业的进程管理工具（如PM2、Supervisor）