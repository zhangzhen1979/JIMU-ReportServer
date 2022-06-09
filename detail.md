# Report Server 报表服务

---

## 说明文档

[![快速开始](https://img.shields.io/badge/%E8%AF%95%E7%94%A8-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B-blue.svg)](readme.md)
[![详细介绍](https://img.shields.io/badge/%E6%8E%A5%E5%8F%A3-%E8%AF%A6%E7%BB%86%E4%BB%8B%E7%BB%8D-blue.svg)](detail.md)

---

## 简介

本服务用于将JSON数据生成报表、将报表模板中设置的SQL查询结果生成报表。
支持生成PDF文件，支持回写到指定位置；支持直接返回HTTP Reponse。

## 配置说明

本服务的所有配置信息均在于jar包同级文件夹中的application.yml中，默认内容如下：

```yml
# Tomcat
server:
  tomcat:
    uri-encoding: UTF-8
    max-threads: 1000
    min-spare-threads: 30
  # 端口号
  port: 8080
  # 超时时间
  connection-timeout: 5000

spring:
  application:
    # 应用名称。如果启用nacos，此值必填
    name: com.thinkdifferent.reportserver

  # 如果不使用数据库连接（不查数据库记录生成报表），则配置此项；
  # 如果使用数据库，则注释此项，并配置datasource中的参数
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure

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

- Nacos服务设置：设置是否启用、服务地址和端口。
- 线程参数设置：需要根据实际硬件的承载能力，调整线程池的大小。
- RabbitMQ设置：根据实际软件部署情况，控制是否启用RabbitMQ；如果启用RabbitMQ，一定要根据服务的配置情况修改地址、端口、用户名、密码等信息。
- 本服务设置：根据本服务所在服务器的实际情况，修改本地文件输出路径。



## 使用说明

本服务提供REST接口供外部系统调用，提供了直接转换接口和通过MQ异步转换的接口。

系统提供多个接口，满足多种报表使用场景

- 传入JSON，返回PDF报表给HTTP Reponse：http://host:port/api/getJson2Pdf
- 传入JSON，返回HTML报表给HTTP Reponse：http://host:port/api/getJson2Html
- 传入JSON，生成PDF报表文件，回写到指定位置：http://host:port/api/getJson2PdfFile
- 传入JSON，生成PDF报表文件（单个），直接返回文件的Base64字符串：http://host:port/api/getJson2PdfBase64
- 传入JSON，生成PDF报表文件（多个），通过JSON返回文件的Base64字符串：http://host:port/api/getJson2PdfsBase64
- 传入数据表查询参数（JSON格式），返回PDF报表给HTTP Reponse：http://host:port/api/getDb2Pdf
- 传入数据表查询参数（JSON格式），返回HTML报表给HTTP Reponse：http://host:port/api/getDb2Html
- 传入数据表查询参数（JSON格式），生成PDF报表文件，回写到指定位置：http://host:port/api/getDb2PdfFile
- 传入数据表查询参数（JSON格式），生成PDF报表文件（单个），直接返回文件的Base64字符串：http://host:port/api/getDb2PdfBase64
- 传入数据表查询参数（JSON格式），生成PDF报表文件（多个），通过JSON返回文件的Base64字符串：http://host:port/api/getDb2PdfsBase64
- 传入JSON，加入到MQ队列异步处理，生成单个或多个PDF文件，回写到指定位置：http://host:port/api/put2Mq



接口调用方式：POST

传入参数形式：JSON



### 传入参数示例

示例：

```JSON
{
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
    "writeBackType": "path",
    "writeBack":{
       "path":reportserver
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

系统支持JSON格式的数据输入，并将数据传入JasperReport报表模板，由其生成符合要求格式的PDF文件。

注意：在生成列表格式的报表时，JasperReport没有办法在末尾页添加空行，所以需要在输入的JSON中传入对应格式的空白对象。此时，报表的“自适应行高”功能则不可用，否则会导致行数计算错误，输出报表格式错乱。

以下是输入设置部分：

```json
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
```

或

```json
	"reportFile":"jzpz/jzpz",
    "fileName":"12345",
```

- reportFile：必填。报表文件。此处需要输入报表模板在本服务所在相对路径和文件名（不含扩展名）。
- fileNameKey 或 fileName：必填其一（不可同时存在）。
  - 本服务允许对一个报表模板，一次传入多组数据，对应生成多个PDF报表文件。所以，需要在此指定从data域中的哪个key取值作为pdf的文件名。
  - 如果只需要将所有数据生成到一个PDF报表文件中，则需要配置“fileName”，并指定一个文件名。

**注意**：如果调用的是“put2Mq”接口（传入MQ异步处理），则需要判断报表是否为“数据库查询”类型。如果是，则需要加入“dataSource”，并且值为“db”；数据源为“JSON”类型的，则不用添加此项。例如：

```json
	"dataSource":"db",
	"reportFile":"jzpz/jzpz",
    "fileName":"12345",
```



#### 回写信息

本服务支持以下回写方式：文件路径（path）、http协议上传（url）、FTP服务上传（ftp）。

注意：返回Base64接口无此部分回写信息。

当使用文件路径（Path）方式回写时，配置如下：

```json
	"writeBackType": "path",
	"writeBack": {
		"path": "D:/data2pdf/pdf/"
	},
```

- writeBackType：必填，值为“path”。
- writeBack：必填。JSON对象，path方式中，key为“path”，value为MP4文件回写的路径。

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

注意：返回Base64接口无此部分信息。

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



#### 数据域

data域为传送给报表模板的核心值，其中为JSON数组。数组中的内容按照报表中的实际设置而定。

如果使用“数据库查询”类型的报表模板，则data域中可以存放报表模板中设置的参数以及参数值。

例如：

```json
    "data":[
        {
            "table": "dat_archive_arc_jh",
            "where": "1=1",
            "orderBy": "id"
        }
    ]
```

注意：

- 返回单个文件的Base64的接口中，数据域中只有第一个报表文件的JSON对象有效，会生成PDF后返回Base64字符串。
- 返回多个文件的Base64的接口中，数据域中可以有多个JSON对象数据。



#### 返回信息

##### 流式接口返回

所有给HTTPReponse返回HTML、PDF流的接口，均可以在浏览器中直接显示。

##### 文件接口返回

所有PDF报表文件接口，在文件生成、回写过程处理完毕后，接口返回信息示例如下：

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

##### 单个PDF文件Base64接口返回

所有返回单个PDF文件Base64接口，在处理完毕后，返回信息示例如下：

```
JVBERi0xLjQKJeLjz9MKNCAwIG9iago8PC9TdWJ0eXBlL0Zvcm0vRmlsdGVyL0ZsYXRlRGVjb2RlL1R5………………
```

- 返回Base64编码后的PDF文件的内容。可供前端页面直接将其放入iframe的src属性中显示。

##### 多个PDF文件Base64接口返回

所有返回多个PDF文件Base64接口，在处理完毕后，返回信息示例如下：

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

