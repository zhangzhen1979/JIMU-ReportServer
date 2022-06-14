# Report Server 报表服务

将JSON数据生成报表、将报表模板中设置的SQL查询结果生成报表。
支持生成PDF、HTML、docx、xlsx等文件，支持回写到指定位置；支持直接返回HTTP Reponse。

---

## 说明文档

[![快速开始](https://img.shields.io/badge/%E8%AF%95%E7%94%A8-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B-blue.svg)](readme.md)
[![详细介绍](https://img.shields.io/badge/%E6%8E%A5%E5%8F%A3-%E8%AF%A6%E7%BB%86%E4%BB%8B%E7%BB%8D-blue.svg)](detail.md)

---

## 特性

* 支持多种输出方式：文件路径、http、ftp，可扩展
* 接口方式：REST，POST方式。
* 参数格式：JSON。
* 数据源：JSON / DB（JSON中可以设置查询参数，与报表模板中参数匹配）。
* 报表引擎：JasperReport、XMReport。
* 报表生成方式：
  * HTTP Reponse：页面可直接显示。
    * HTML：返回html文件流，可直接用于页面展示。但不建议用于打印，此种模式对纸张的兼容性不好。
    * PDF：返回pdf文件流，可直接用于页面展示、打印。

  * 报表文件：支持直接回写（文件路径、ftp），也可异步回写（MQ队列处理）。可同时生成多个报表文件。

* 支持结果回调。

## 依赖

* `jdk8`: 编译、运行环境
* `maven`: 只运行`jar`不需要；编译打包需要，建议`V3.6.3`以上版本
* `数据库`: 当前版本支持MySQL，如需支持其他数据库，请加入对应的数据库驱动jar包，并修改配置文件。
* `JasperReport Studio 5.6.1`：报表模板设计器。如需自制报表模板，则需要此项。使用手册请在网络中搜索。

## 快速启动

1. 确认文件目录结构

   ```
   │  application.yml
   │  reportserver-{版本号}.jar
   │  {项目名}.license
   │  reportfile【文件夹】
   │  cacheDir【文件夹】
   ```

2. 修改配置`application.yml`：

   ​	2.1. 临时文件夹：用于存储生成的PDF报表文件。需要配置 reportserver.outPutPath 的值，此处需要配置文件夹的绝对路径。

   ​		2.2. 数据库连接配置：如果使用“数据库查询”类型的报表，则需要进行此配置。

   ​			2.2.1. 注释掉原有的  spring.autoconfigure.exclude 配置项，打开数据库自动连接。

   ​			2.2.2. 配置  spring.datasource和spring.jpa 下的参数

   | 配置名                                  | 配置说明            | 示例                                                         |
   | --------------------------------------- | ------------------- | ------------------------------------------------------------ |
   | spring.datasource.driver-class-name     | 数据库驱动          | com.mysql.cj.jdbc.Driver                                     |
   | spring.datasource.url                   | 数据库连接URL       | jdbc:mysql://localhost/udmc?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&serverTimezone=UTC&useSSL=false |
   | spring.datasource.username              | 用户名              | root                                                         |
   | spring.datasource.password              | 密码                | root                                                         |
   | spring.jpa.properties.hibernate.dialect | Hibernate SQL方言包 | org.hibernate.dialect.MySQL57Dialect                         |

   ​		2.3. MQ消息队列配置：如果使用RabbitMQ进行异步处理（提升稳定性），则需要进行此配置（默认已关闭）。

   | 配置名                                       | 配置说明         | 示例      |
   | -------------------------------------------- | ---------------- | --------- |
   | spring.rabbitmq.host                         | MQ服务地址       | 127.0.0.1 |
   | spring.rabbitmq.port                         | 端口             | 5672      |
   | spring.rabbitmq.username                     | 用户名           | guest     |
   | spring.rabbitmq.password                     | 密码             | guest     |
   | spring.rabbitmq.listener.direct.auto-startup | “生产者”监听开关 | true      |
   | spring.rabbitmq.listener.simple.auto-startup | “消费者”监听开关 | true      |

3. 以管理员身份运行

   | 操作系统 | 运行示例                                    |
   | -------- | ------------------------------------------- |
   | Windows  | javaw -jar reportserver-{版本号}.jar        |
   | linux    | nohup java -jar reportserver-{版本号}.jar & |

4. 浏览器访问 `http://{ip}:{端口}` , 返回 **启动成功** 标识项目启动正常



## 常见问题

1. 项目日志在哪里？

  运行目录下logs文件夹内

2. 项目启动失败，日志中有`The Tomcat connector configured to listen on port 8080 failed to start. The port may already be in use or the connector may be misconfigured.`的报错
   

  端口被占用，修改`application.yml`中`server.port`, 改为其他端口
