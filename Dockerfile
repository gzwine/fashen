# 使用多阶段构建
# 第一阶段：构建应用
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 第二阶段：运行应用
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 从构建阶段复制 jar 文件
COPY --from=builder /build/target/*.jar app.jar

# 创建配置目录
RUN mkdir -p /app/config

# 复制配置文件
COPY src/main/resources/application.properties /app/config/

# 设置环境变量
ENV SERVER_PORT=8081
ENV UPLOAD_PATH=/app/uploads
ENV SPRING_CONFIG_LOCATION=file:/app/config/application.properties

# 创建上传目录
RUN mkdir -p ${UPLOAD_PATH}

# 暴露端口
EXPOSE 8081

# 设置容器启动命令
ENTRYPOINT ["java", "-jar", "app.jar"] 