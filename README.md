### grpc-astray

[![Build](https://github.com/CharLemAznable/grpc-astray/actions/workflows/build.yml/badge.svg)](https://github.com/CharLemAznable/grpc-astray/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/grpc-astray/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/grpc-astray/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)
![GitHub code size](https://img.shields.io/github/languages/code-size/CharLemAznable/grpc-astray)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=alert_status)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=bugs)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=security_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=sqale_index)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=code_smells)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=ncloc)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=coverage)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_grpc-astray&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=CharLemAznable_grpc-astray)

The Java gRPC Server/Client implementation, using JSON marshaller.

##### Maven Dependency

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>grpc-astray</artifactId>
  <version>2023.2.0</version>
</dependency>
```

##### Maven Dependency SNAPSHOT

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>grpc-astray</artifactId>
  <version>2023.2.1-SNAPSHOT</version>
</dependency>
```

#### 创建gRPC服务

```java
@GRpcService
public class MyService {
    @GRpcMethod
    public MyResponse methodName(MyRequest req) {
        // 业务逻辑
    }
}
```

启动SpringBoot后, gRPC服务默认监听7018端口.

#### 使用客户端调用

```java
@GRpcClient("MyService")
@GRpcChannel("service_host:7018")
public interface MyClient {
    @GRpcCall
    MyResponse methodName(MyRequest req);
}

MyClient client = GRpcFactory.getClient(MyClient.class);
MyResponse resp = client.methodName(req);
```
