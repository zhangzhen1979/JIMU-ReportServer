# Report Server 报表服务

---

## 说明文档

[![快速开始](https://img.shields.io/badge/%E8%AF%95%E7%94%A8-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B-blue.svg)](readme.md)
[![详细介绍](https://img.shields.io/badge/%E6%8E%A5%E5%8F%A3-%E8%AF%A6%E7%BB%86%E4%BB%8B%E7%BB%8D-blue.svg)](detail.md)

---

## 简介

- 本服务用于将JSON数据生成报表、将报表模板中设置的SQL查询结果生成报表。
- 支持生成各种格式报表文件，支持回写到指定位置；支持直接返回HTTP Reponse。
- 本服务集成了XMReport、JasperReport两个开源报表引擎，分别针对不同场景
  - XMReport支持通过Web方式在线设置报表模板，可以提供在线生成报表预览的能力，支持输出多种文件格式的报表。此引擎适合制作传统的（纵向输出）列表、卡片、单据等。
  - JasperReport需要使用JasperReport Studio编制报表。支持所有类型的报表编制，包括：（纵向输出）列表、卡片、单据，还支持横向输出的报表（例如：档案盒脊背）。并且其采用独立的缓存机制，报表生成效率更高。（此引擎支持生成报表文件并回写）



------

## 配置说明

本服务的所有配置信息均在于jar包同级文件夹中的application.yml中，默认内容如下：

```yml
# Tomcat
server:
  tomcat:
    uri-encoding: UTF-8
  # 端口号
  port: 8080

spring:
  application:
    # 应用名称。如果启用nacos，此值必填
    name: com.thinkdifferent.reportserver

  # datasource（Druid用）
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost/udmc?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&serverTimezone=UTC&useSSL=false
    username: root
    password: root
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      #初始化连接池大小
      initial-size: 5
      #配置最小连接数
      min-idle: 5
      #配置最大连接数
      max-active: 300
      #配置连接等待超时时间
      max-wait: 60000
      #配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
      time-between-eviction-runs-millis: 60000
      #配置一个连接在池中最小生存的时间，单位是毫秒
      min-evictable-idle-time-millis: 300000
      #测试连接
      validation-query: SELECT 1
      #申请连接的时候检测，建议配置为true，不影响性能，并且保证安全
      test-while-idle: true
      #获取连接时执行检测，建议关闭，影响性能
      test-on-borrow: false
      #归还连接时执行检测，建议关闭，影响性能
      test-on-return: false
      #是否开启PSCache，PSCache对支持游标的数据库性能提升巨大，oracle建议开启，mysql下建议关闭
      pool-prepared-statements: true
      #开启poolPreparedStatements后生效
      max-pool-prepared-statement-per-connection-size: 20
      #配置扩展插件，常用的插件有=>stat:监控统计  log4j:日志  wall:防御sql注入
      filters: stat,wall,slf4j
      #打开mergeSql功能；慢SQL记录
      connection-properties: druid.stat.mergeSql\=true;druid.stat.slowSqlMillis\=5000
      #配置DruidStatFilter
      web-stat-filter:
        enabled: true
        url-pattern: "/*"
        exclusions: "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*"
      #配置DruidStatViewServlet
      stat-view-servlet:
        url-pattern: "/druid/*"
        #IP白名单(没有配置或者为空，则允许所有访问)
        allow: 127.0.0.1,192.168.163.1

        #IP黑名单 (存在共同时，deny优先于allow)
        # deny: 192.168.1.73

        # 禁用HTML页面上的“Reset All”功能
        reset-enable: false
        #登录名
        login-username: root
        #登录密码
        login-password: root
  
  # 此处需要与数据库配置同步修改，设置为对应的SQL方言包
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL57Dialect

  # RabbitMQ设置
  rabbitmq:
    # 访问地址
    host: 127.0.0.1
    # 端口
    port: 5672
    # 用户名
    username: guest
    # 密码
    password: guest
    # 监听设置
    listener:
      # 生产者
      direct:
        # 自动启动开关
        auto-startup: false
      # 消费者
      simple:
        # 自动启动开关
        auto-startup: false

  cloud:
    # Nacos的配置。
    # 如果启用Nacos服务作为配置中心，
    # 则此部分之后的内容均可以在Nacos配置中心中管理，
    # 不必在此配置文件中维护。
    nacos:
      config:
        # 配置服务地址
        server-addr: 127.0.0.1:8848
        # 启用状态
        enabled: false
      discovery:
        # 服务发现服务地址
        server-addr: 127.0.0.1:8848
        # 启用状态
        enabled: false


# log4j2设置
logging:
  level:
    root: info
    com.thinkdifferent: debug
  file:
    name: logs/application.log

# 线程设置参数 #######
ThreadPool:
  # 核心线程数10：线程池创建时候初始化的线程数
  CorePoolSize: 10
  # 最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
  MaxPoolSize: 20
  # 缓冲队列200：用来缓冲执行任务的队列
  QueueCapacity: 200
  # 保持活动时间60秒
  KeepAliveSeconds: 60
  # 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
  AwaitTerminationSeconds: 60


reportserver:
  outPutPath: D:/data2pdf/pdf/
```

可以根据服务器的实际情况进行修改。

重点需要修改的内容：

- 数据库设置：spring.datasource、spring.jpa中的设置，需要根据实际数据库情况进行修改。必须使用数据库，主要用于XMReport存储模板、脚本等信息。
- 本服务设置：根据本服务所在服务器的实际情况，修改本地文件输出路径。
- RabbitMQ设置：根据实际软件部署情况，控制是否启用RabbitMQ；如果启用RabbitMQ，一定要根据服务的配置情况修改地址、端口、用户名、密码等信息。
- 线程参数设置：需要根据实际硬件的承载能力，调整线程池的大小。
- Nacos服务设置：设置是否启用、服务地址和端口。



------

## XMReport引擎接口使用说明

XMReport引擎在本服务中提供如下接口：

### templateManager/getTemplates：模板查询

使用报表前，需要先在desinger中配置报表模板，并保存。

可以利用接口查询系统中的报表模板的信息：

- 接口地址：http://host:port/templateManager/getTemplates?groupId=
- 接口调用方式：GET

请求后，系统会返回数据库中当前存储的模板信息。后续调用报表预览接口时，需要使用模板“id”的值。

```json
[
    {
        "id": "template-2c343377-c779-4af9-9a80-8cab37f09e83",
        "groupId": "0",
        "name": "归档文件目录-sql",
        "isGroup": false
    },
    {
        "id": "template-e7e2ad60-596b-41d4-80f8-65991da9049a",
        "groupId": "0",
        "name": "归档文件目录",
        "isGroup": false
    }
]
```



### report/getStream：生成报表

提供REST接口供外部系统调用，可直接生成各种格式报表。

- 接口地址：http://host:port/report/getStream

- 接口调用方式：POST

- 传入参数形式：JSON

  传入参数示例：

```JSON
{
    "reportFile": "template-e7e2ad60-596b-41d4-80f8-65991da9049a",
    "data": [
        {
            "year": "2022",
            "fonds": "维森集团",
            "retention": "30年",
            "box_no": "0001",
            "barcode": "1234567891"
        },
        {
            "year": "2022",
            "fonds": "维森集团",
            "retention": "10年",
            "box_no": "0002",
            "barcode": "1234567892"
        },
        {
            "year": "2022",
            "fonds": "维森集团",
            "retention": "10年",
            "box_no": "0003",
            "barcode": "1234567893"
        },
        {
            "year": "2022",
            "fonds": "维森集团",
            "retention": "30年",
            "box_no": "0004",
            "barcode": "1234567894"
        }
    ],
    "options": {
        "docType": "PDF",
        "dividePage": true
    }
}
```

以下分块解释传入参数每部分的内容。



#### 报表信息

```json
	"reportFile": "template-e7e2ad60-596b-41d4-80f8-65991da9049a",
   	"options": {
        "docType": "PDF",
        "dividePage": true
    }
```

- reportFile：必填。报表模板ID。可从前述接口的结果中获得。
- options：必填。报表生成参数。
  - docType：预览格式，包括：PDF、Word、Excel、HTML、Image
  - dividePage：分页。当预览格式为PDF、PNG时，此项默认为true；当为其他格式时，可以选择分页（true）或不分页（false）。




#### 数据域

必填！！

data域为传送给报表模板的核心值，其中为JSON数组。数组中的内容按照报表中的实际设置而定。例如：

```json
    "data": [
        {
            "year": "2022",
            "fonds": "维森集团",
            "retention": "30年",
            "box_no": "0001",
            "barcode": "1234567891"
        }
    ],
```

如果报表是通过数据库查询生成，则数据域为sql参数（无需data标签）。例如：

```json
    "fields": "filing_year,fonds_no,retention,piece_no",
    "table": "dat_archive_arc_jh",
    "where": "1=1",
    "orderBy": "id",
```

对应的，报表中设置表格中“行”绑定的“数据脚本为：

```javascript
=executeJDBCQuery("select ${fields} from ${table} where ${where} order by ${orderBy}")
```

- fields：查询字段列表。不可使用“*”，会查不出来字段，导致无法获取字段值。
- table：数据表名。
- where：查询子句。
- orderBy：排序字段列表。

#### 返回信息

此接口给HTTPReponse返回HTML、PDF等格式的文件流，均可以在浏览器中直接显示。



### report/getFile：生成报表文件并回写到指定位置

系统提供生成报表文件，并根据传入的参数要求，将文件回写到指定位置的功能。

- 接口地址：http://host:port/report/getFile
- 接口调用方式：POST
- 传入参数形式：JSON

本例以“JSON生成报表”为例说明如何使用本接口；“查询数据库生成报表”的参数内容，可参考前述章节的说明，结合本章节内容组装。

传入参数示例：

```JSON
{
	"reportFile":"template-e7e2ad60-596b-41d4-80f8-65991da9049a",
	"fileName":"arcList",
    "writeBackType": "path",
    "writeBack":{
        "path":"D:/data2pdf/pdf/"
    },
	"data":[
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0001",
    		"barcode":"1234567891"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0002",
    		"barcode":"1234567892"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0003",
    		"barcode":"1234567893"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0004",
    		"barcode":"1234567894"
    	}
    ],
    "options": {
        "docType": "PDF",
        "dividePage": true
    }
}
```

以下分块解释传入参数每部分的内容。



#### 报表信息

与“report/getStream”接口“报表信息”内容相同。


#### 数据域

与“report/getStream”接口“数据域”内容相同。

#### 回写信息

本服务支持以下回写方式：文件路径（path）、http协议上传（url）、FTP服务上传（ftp）。

##### path

当使用文件路径（path）方式回写时，配置如下：

```json
	"fileName": "arcList",
	"writeBackType": "path",
	"writeBack": {
		"path": "D:/data2pdf/pdf/"
	},
```

- fileName：输出的报表文件名。（不包含扩展名，系统会自动根据选择的输出格式，自动组装扩展名）
- writeBackType：必填，值为“path”。
- writeBack：必填。JSON对象，path方式中，key为“path”，value为MP4文件回写的路径。

##### url

当使用http协议上传（url）方式回写时，配置如下：

```json
	"writeBackType": "url",
	"writeBack": {
		"url": "http://localhost/uploadfile.do"
	},
	"writeBackHeaders": {
		"Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
	},
```

- writeBackType：必填，值为“url”。
- writeBack：必填。JSON对象，url方式中，key为“url”，value为业务系统提供的文件上传接口API地址。
- writeBackHeaders：非必填。如果Web服务器访问时需要设置请求头或Token认证，则需要在此处设置请求头的内容；否则此处可不添加。

##### ftp

当使用FTP服务上传（ftp）方式回写时，配置如下：

```json
	"writeBackType": "ftp",
	"writeBack": {
         "passive": "false",
		"host": "ftp://localhost",
         "port": "21",
         "username": "guest",
         "password": "guest",
         "filepath": "/2021/10/"
	},
```

- writeBackType：必填，值为“ftp”。
- writeBack：必填。JSON对象。
  - passive：是否是被动模式。true/false
  - host：ftp服务的访问地址。
  - port：ftp服务的访问端口。
  - username：ftp服务的用户名。
  - password：ftp服务的密码。
  - filepath：文件所在的路径。

#### 回调信息

业务系统可以提供一个GET方式的回调接口，在视频文件转换、回写完毕后，本服务可以调用此接口，传回处理的状态。

```json
	"callBackURL": "http://10.11.12.13/callback.do",
	"callBackHeaders": {
		"Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
	},
```

- callBackURL：回调接口的URL。回调接口需要接收两个参数：
  - file：处理后的文件名。本例为“001-online”。
  - flag：处理后的状态，值为：success 或 error。
- callBackHeaders：如果回调接口需要在请求头中加入认证信息等，可以在此处设置请求头的参数和值。

接口url示例：

```
http://10.11.12.13/callback.do?file=001-online&flag=success
```

#### 返回信息

在文件生成、回写过程处理完毕后，接口返回信息示例如下：

```json
{
    "flag": "success",
    "message": "PDF reoprt create success. PDF file write back success. API call back success.",
    "file": "123.pdf;456.pdf"
}
```

- flag：处理状态。success，成功；error，错误，失败。
- message：返回接口消息。
- file：生成的文件名列表



### report/getBase64：返回报表文件Base64

系统提供生成报表文件，并根据传入的参数要求，将文件Base64编码后返回。

- 接口地址：http://host:port/report/getBase64
- 接口调用方式：POST
- 传入参数形式：JSON

本例以“JSON生成报表”为例说明如何使用本接口；“查询数据库生成报表”的参数内容，可参考前述章节的说明，结合本章节内容组装。

传入参数示例：

```JSON
{
	"reportFile":"template-e7e2ad60-596b-41d4-80f8-65991da9049a",
	"fileName":"arcList",
	"data":[
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0001",
    		"barcode":"1234567891"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0002",
    		"barcode":"1234567892"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0003",
    		"barcode":"1234567893"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0004",
    		"barcode":"1234567894"
    	}
    ],
    "options": {
        "docType": "PDF",
        "dividePage": true
    }
}
```

以下分块解释传入参数每部分的内容。



#### 报表信息

与“report/getStream”接口“报表信息”内容相同。


#### 数据域

与“report/getStream”接口“数据域”内容相同。

#### 返回信息

接口在处理完毕后，返回信息示例如下：

```
JVBERi0xLjQKJeLjz9MKNCAwIG9iago8PC9TdWJ0eXBlL0Zvcm0vRmlsdGVyL0ZsYXRlRGVjb2RlL1R5………………
```

返回Base64编码后的文件的内容。可供前端页面直接将其放入iframe的src属性中显示。



### report/put2Mq：数据加入到MQ

系统提供生成报表文件，并根据传入的参数要求，将文件Base64编码后返回。

- 接口地址：http://host:port/report/put2Mq
- 接口调用方式：POST
- 传入参数形式：JSON

本例以“JSON生成报表”为例说明如何使用本接口；“查询数据库生成报表”的参数内容，可参考前述章节的说明，结合本章节内容组装。

传入参数示例：

```JSON
{
	"reportFile":"template-e7e2ad60-596b-41d4-80f8-65991da9049a",
	"fileName":"arcList",
	"writeBackType": "path",
	"writeBack": {
		"path": "D:/data2pdf/pdf/"
	},
	"data":[
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0001",
    		"barcode":"1234567891"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0002",
    		"barcode":"1234567892"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0003",
    		"barcode":"1234567893"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0004",
    		"barcode":"1234567894"
    	}
    ],
    "options": {
        "docType": "PDF",
        "dividePage": true
    }
}
```

以下分块解释传入参数每部分的内容。



#### 报表信息

与“report/getStream”接口“报表信息”内容相同。


#### 数据域

与“report/getStream”接口“数据域”内容相同。

#### 回写信息

与“report/getFile”接口“回写信息”内容相同。

#### 回调信息

与“report/getFile”接口“回调信息”内容相同。

#### 返回信息

接口在处理完毕后，返回信息示例如下：

```
JVBERi0xLjQKJeLjz9MKNCAwIG9iago8PC9TdWJ0eXBlL0Zvcm0vRmlsdGVyL0ZsYXRlRGVjb2RlL1R5………………
```

返回Base64编码后的文件的内容。可供前端页面直接将其放入iframe的src属性中显示。



------

## JasperReport引擎使用说明

XMReport引擎在本服务中提供如下接口：

### jasper/getStream：生成报表

传入JSON或数据表查询参数（JSON格式），返回报表给HTTP Reponse。

- 接口地址：http://host:port/jasper/getStream

- 接口调用方式：POST

- 传入参数形式：JSON

  传入参数示例：

```json
{
	"reportFile":"dah/jb-4cm",
	"fileName":"dahjb",
    "docType": "pdf",
    "dataSource": "json",
	"data":[
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0001",
            "barcode":"1234567891"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0002",
            "barcode":"1234567892"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0003",
            "barcode":"1234567893"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0004",
            "barcode":"1234567894"
    	}
    ]
}
```

以下分块解释传入参数每部分的内容。



#### 输入信息

系统支持JSON格式的数据输入，并将数据传入JasperReport报表模板，由其生成符合要求格式的报表文件。

注意：在生成列表格式的报表时，JasperReport没有办法在末尾页添加空行，所以需要在输入的JSON中传入对应格式的空白对象。此时，报表的“自适应行高”功能则不可用，否则会导致行数计算错误，输出报表格式错乱。

以下是输入设置部分：

```json
	"reportFile":"dah/jb-4cm",
	"fileNameKey":"barcode",
    "docType": "pdf",
    "dataSource": "json",
```

或

```json
	"reportFile":"dah/jb-4cm",
	"fileName":"dahjb",
    "docType": "pdf",
    "dataSource": "db",
```

- reportFile：必填。报表文件。此处需要输入报表模板在本服务所在相对路径和文件名（不含扩展名）。
- fileNameKey 或 fileName：必填其一（不可同时存在）。
  - 本服务允许对一个报表模板，一次传入多组数据，对应生成多个报表文件。所以，需要在此指定从data域中的哪个key取值作为pdf的文件名。
  - 如果只需要将所有数据生成到一个报表文件中，则需要配置“fileName”，并指定一个文件名。
- docType：输出报表格式。值可以为：HTML、PDF、Word、Excel、CSV、XML、ODT
- dataSource：数据源类型。与data中的内容对应。值可以为：json、db。



#### 数据域

必填！！

data域为传送给报表模板的核心值，其中为JSON数组。数组中的内容按照报表中的实际设置而定。

如果数据源类型为“json”，则内容为报表中的实际数据。例如：

```json
	"data":[
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0001",
            "barcode":"1234567891"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0002",
            "barcode":"1234567892"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0003",
            "barcode":"1234567893"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0004",
            "barcode":"1234567894"
    	}
    ]
```

如果数据源类型为“db”（即使用“数据库查询”类型的报表模板），则data域中可以存放报表模板中设置的参数以及参数值。例如：

```json
    "data":[
        {
            "table": "dat_archive_arc_jh",
            "where": "1=1",
            "orderBy": "id"
        }
    ]
```



#### 返回信息

给HTTPReponse返回报表文件流，可以在浏览器中直接显示。



### jasper/getFile：生成报表文件并回写到指定位置

传入JSON或数据表查询参数（JSON格式），生成报表文件，回写到指定位置，并可回调接口。

- 接口地址：http://host:port/jasper/getFile

- 接口调用方式：POST

- 传入参数形式：JSON

  传入参数示例：

传入参数示例：

```JSON
{
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
    "dataSource": "json",
    "docType": "pdf",
    "writeBackType": "path",
    "writeBack":{
       "path":"d:/outpdf/"
    },
    "callBackURL": "http://1234.com/callback.do",
    "callBackHeaders": {
		"Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
	},
	"data":[
		{
			"id": "1",
		    "voucher_code": "20210507SJFBX1234567", 
		    "voucher_company_name": "3000XXXX有限公司", 
		    "create_date": "2021年08月31日", 
		    "voucher_number": "6012345234", 
		    "ac_doc_typ_name": "EMS凭证", 
		    "total_chn": "壹佰元整", 
		    "debit_sum": "100.00", 
		    "credit_sum": "100.00", 
		    "post_name": "PI_USER",
		    "pages":2,
		    "detail":[
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "",
				    "credit_amount_lc": "100.00"
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				}
		    ]
		},
		{
			"id": "2",
		    "voucher_code": "20210507SJFBX1234568", 
		    "voucher_company_name": "3000XXXX有限公司", 
		    "create_date": "2021年08月31日", 
		    "voucher_number": "6012345234", 
		    "ac_doc_typ_name": "EMS凭证", 
		    "total_chn": "陆佰元整", 
		    "debit_sum": "600.00", 
		    "credit_sum": "600.00", 
		    "post_name": "PI_USER",
		    "pages":2,
		    "detail":[
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "",
				    "credit_amount_lc": "600.00"
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				}
		    ]
		}
	]
}
```

以下分块解释传入参数每部分的内容。



#### 输入信息

与“jasper/getStream”接口“输入信息”内容相同。

#### 数据域

必填！！

与“jasper/getStream”接口“数据域”内容相同。

#### 回写信息

本服务支持以下回写方式：文件路径（path）、http协议上传（url）、FTP服务上传（ftp）。

##### path

当使用文件路径（Path）方式回写时，配置如下：

```json
	"writeBackType": "path",
	"writeBack": {
		"path": "D:/data2pdf/pdf/"
	},
```

- writeBackType：必填，值为“path”。
- writeBack：必填。JSON对象，path方式中，key为“path”，value为MP4文件回写的路径。

##### url

当使用http协议上传（url）方式回写时，配置如下：

```json
	"writeBackType": "url",
	"writeBack": {
		"url": "http://localhost/uploadfile.do"
	},
	"writeBackHeaders": {
		"Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
	},
```

- writeBackType：必填，值为“url”。
- writeBack：必填。JSON对象，url方式中，key为“url”，value为业务系统提供的文件上传接口API地址。
- writeBackHeaders：非必填。如果Web服务器访问时需要设置请求头或Token认证，则需要在此处设置请求头的内容；否则此处可不添加。

##### ftp

当使用FTP服务上传（ftp）方式回写时，配置如下：

```json
	"writeBackType": "ftp",
	"writeBack": {
         "passive": "false",
		"host": "ftp://localhost",
         "port": "21",
         "username": "guest",
         "password": "guest",
         "filepath": "/2021/10/"
	},
```

- writeBackType：必填，值为“ftp”。
- writeBack：必填。JSON对象。
  - passive：是否是被动模式。true/false
  - host：ftp服务的访问地址。
  - port：ftp服务的访问端口。
  - username：ftp服务的用户名。
  - password：ftp服务的密码。
  - filepath：文件所在的路径。

#### 回调信息

业务系统可以提供一个GET方式的回调接口，在视频文件转换、回写完毕后，本服务可以调用此接口，传回处理的状态。

```json
	"callBackURL": "http://10.11.12.13/callback.do",
	"callBackHeaders": {
		"Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
	},
```

- callBackURL：回调接口的URL。回调接口需要接收两个参数：
  - file：处理后的文件名。本例为“001-online”。
  - flag：处理后的状态，值为：success 或 error。
- callBackHeaders：如果回调接口需要在请求头中加入认证信息等，可以在此处设置请求头的参数和值。

接口url示例：

```
http://10.11.12.13/callback.do?file=001-online&flag=success
```

#### 返回信息

文件生成、回写过程处理完毕后，接口返回信息示例如下：

```json
{
    "flag": "success",
    "message": "PDF reoprt create success. PDF file write back success. API call back success.",
    "file": "123.pdf;456.pdf"
}
```

- flag：处理状态。success，成功；error，错误，失败。
- message：返回接口消息。
- file：生成的文件名列表



### jasper/getBase64：返回报表文件Base64

传入JSON或数据表查询参数（JSON格式），生成单个报表文件，直接返回文件的Base64字符串。

- 接口地址：http://host:port/jasper/getBase64

- 接口调用方式：POST

- 传入参数形式：JSON

  传入参数示例：

```json
{
	"reportFile":"dah/jb-4cm",
	"fileName":"dahjb",
    "docType": "pdf",
    "dataSource": "json",
	"data":[
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0001",
            "barcode":"1234567891"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0002",
            "barcode":"1234567892"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"10年",
    		"box_no":"0003",
            "barcode":"1234567893"
    	},
    	{
		    "year": "2022", 
		    "fonds": "维森集团", 
    		"retention":"30年",
    		"box_no":"0004",
            "barcode":"1234567894"
    	}
    ]
}
```

以下分块解释传入参数每部分的内容。



#### 输入信息

与“jasper/getStream”接口“输入信息”内容相同。

#### 数据域

必填！！

与“jasper/getStream”接口“数据域”内容相同。

#### 返回信息

返回单个报表文件Base64接口，在处理完毕后，返回信息示例如下：

```
JVBERi0xLjQKJeLjz9MKNCAwIG9iago8PC9TdWJ0eXBlL0Zvcm0vRmlsdGVyL0ZsYXRlRGVjb2RlL1R5………………
```

- 返回Base64编码后的文件的内容。可供前端页面直接将其放入iframe的src属性中显示。



### jasper/getBase64s：返回多个报表文件Base64

传入JSON或数据表查询参数（JSON格式），生成多个报表文件，通过JSON返回文件的Base64字符串。

- 接口地址：http://host:port/jasper/getBase64s

- 接口调用方式：POST

- 传入参数形式：JSON

  传入参数示例：

传入参数示例：

```JSON
{
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
    "dataSource": "json",
    "docType": "pdf",
	"data":[
		{
			"id": "1",
		    "voucher_code": "20210507SJFBX1234567", 
		    "voucher_company_name": "3000XXXX有限公司", 
		    "create_date": "2021年08月31日", 
		    "voucher_number": "6012345234", 
		    "ac_doc_typ_name": "EMS凭证", 
		    "total_chn": "壹佰元整", 
		    "debit_sum": "100.00", 
		    "credit_sum": "100.00", 
		    "post_name": "PI_USER",
		    "pages":2,
		    "detail":[
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "",
				    "credit_amount_lc": "100.00"
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				}
		    ]
		},
		{
			"id": "2",
		    "voucher_code": "20210507SJFBX1234568", 
		    "voucher_company_name": "3000XXXX有限公司", 
		    "create_date": "2021年08月31日", 
		    "voucher_number": "6012345234", 
		    "ac_doc_typ_name": "EMS凭证", 
		    "total_chn": "陆佰元整", 
		    "debit_sum": "600.00", 
		    "credit_sum": "600.00", 
		    "post_name": "PI_USER",
		    "pages":2,
		    "detail":[
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "",
				    "credit_amount_lc": "600.00"
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				}
		    ]
		}
	]
}
```

以下分块解释传入参数每部分的内容。



#### 输入信息

与“jasper/getStream”接口“输入信息”内容相同。

#### 数据域

必填！！

与“jasper/getStream”接口“数据域”内容相同。

#### 返回信息

返回多个文件Base64接口，在处理完毕后，返回信息示例如下：

```json
{
    "flag": "success",
    "message": "Create Pdf Report file Success",
    "base64": [
        {
            "filename": "20210507SJFBX1234567.pdf",
            "base64": "JVBERi0xLjQKJeLjz9MKNCAwIG9iago8PC9TdWJ0eXBlL0Zvcm0vRmlsdGVy…………"
        },
        {
            "filename": "20210507SJFBX1234568.pdf",
            "base64": "JVBERi0xLjQKJeLjz9MKNCAwIG9iago8PC9TdWJ0eXBlL0Zvcm0vRmlsdGVy…………"
        }
    ]
}
```

- flag：处理状态。success，成功；error，错误，失败。
- message：返回接口消息。
- base64
  - filename：文件名
  - base64：文件Base64编码之后的字符串。



### jasper/put2Mq：数据加入到MQ

传入JSON，加入到MQ队列异步处理，生成单个或多个报表文件，回写到指定位置。

- 接口地址：http://host:port/jasper/put2Mq

- 接口调用方式：POST

- 传入参数形式：JSON

  传入参数示例：

传入参数示例：

```JSON
{
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
    "dataSource": "json",
    "docType": "pdf",
    "writeBackType": "path",
	"writeBack": {
		"path": "D:/data2pdf/pdf/"
	},
    "callBackURL": "http://1234.com/callback.do",
    "callBackHeaders": {
		"Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
	},
	"data":[
		{
			"id": "1",
		    "voucher_code": "20210507SJFBX1234567", 
		    "voucher_company_name": "3000XXXX有限公司", 
		    "create_date": "2021年08月31日", 
		    "voucher_number": "6012345234", 
		    "ac_doc_typ_name": "EMS凭证", 
		    "total_chn": "壹佰元整", 
		    "debit_sum": "100.00", 
		    "credit_sum": "100.00", 
		    "post_name": "PI_USER",
		    "pages":2,
		    "detail":[
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "",
				    "credit_amount_lc": "100.00"
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},

		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				}
		    ]
		},
		{
			"id": "2",
		    "voucher_code": "20210507SJFBX1234568", 
		    "voucher_company_name": "3000XXXX有限公司", 
		    "create_date": "2021年08月31日", 
		    "voucher_number": "6012345234", 
		    "ac_doc_typ_name": "EMS凭证", 
		    "total_chn": "陆佰元整", 
		    "debit_sum": "600.00", 
		    "credit_sum": "600.00", 
		    "post_name": "PI_USER",
		    "pages":2,
		    "detail":[
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "100.00",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "XXXXX店报销手机费",
				    "subject_name": "6601000000手机费",
				    "debit_amount_lc": "",
				    "credit_amount_lc": "600.00"
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				},
		    	{
				    "abstract": "",
				    "subject_name": "",
				    "debit_amount_lc": "",
				    "credit_amount_lc": ""
				}
		    ]
		}
	]
}
```

以下分块解释传入参数每部分的内容。



#### 输入信息

与“jasper/getStream”接口“输入信息”内容相同。

#### 数据域

必填！！

与“jasper/getStream”接口“数据域”内容相同。

#### 回写信息

与“jasper/getFile”接口“回写信息”内容相同。

#### 回调信息

与“jasper/getFile”接口“回调信息”内容相同。

