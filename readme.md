**Json2PDF Service** 

**JSON数据生成PDF文件服务**

# 简介

本服务用于将JSON数据通过JasperReport报表模板生成PDF文件。

# 配置说明

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


# log4j2设置
logging:
  config: log4j2.xml
  level:
    com.thinkdifferent.core: trace

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


#对于rabbitMQ的支持
spring:
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


convert:
  data2pdf:
    outPutPath: D:/data2pdf/pdf/
```

可以根据服务器的实际情况进行修改。

重点需要修改的内容：

- 线程参数设置：需要根据实际硬件的承载能力，调整线程池的大小。
- RabbitMQ设置：根据实际软件部署情况，控制是否启用RabbitMQ；如果启用RabbitMQ，一定要根据服务的配置情况修改地址、端口、用户名、密码等信息。
- 本服务设置：根据本服务所在服务器的实际情况，修改本地文件输出路径。

# 使用说明

本服务提供REST接口供外部系统调用，提供了直接转换接口和通过MQ异步转换的接口。

直接生成PDF接口URL：[http://host:port/api/data2pdf]()

MQ异步生成PDF接口URL：http://host:port/api/data2mq

接口调用方式：POST

传入参数形式：JSON

传入参数示例：

```JSON
{
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
    "writeBackType": "path",
     "writeBack":
     {
        "path":"D:/data2pdf/pdf/"
     },
    "callBackURL": "http://1234.com/callback.do",
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

## 输入信息

系统支持JSON格式的数据输入，并将数据传入JasperReport报表模板，由其生成符合要求格式的PDF文件。

注意：在生成列表格式的报表时，JasperReport没有办法在末尾页添加空行，所以需要在输入的JSON中传入对应格式的空白对象。此时，报表的“自适应行高”功能则不可用，否则会导致行数计算错误，输出报表格式错乱。

以下是输入设置部分：

```json
	"reportFile":"jzpz/jzpz",
	"fileNameKey":"voucher_code",
```

- reportFile：必填。报表文件。此处需要输入报表模板在本服务所在相对路径和文件名（不含扩展名）。
- fileNameKey：必填。本服务允许对一个报表模板，一次传入多组数据，对应生成多个PDF报表文件。所以，需要在此指定从data域中的哪个key取值作为pdf的文件名。

## 回写信息

本服务支持以下回写方式：文件路径（path）、http协议上传（url）、FTP服务上传（ftp）。

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
		"host": "ftp://localhost",
         "port": "21",
         "username": "guest",
         "password": "guest",
         "basepath": "/mp4/",
         "filepath": "/2021/10/"
	},
```

- writeBackType：必填，值为“ftp”。
- writeBack：必填。JSON对象。
  - host：ftp服务的访问地址。
  - port：ftp服务的访问端口。
  - username：ftp服务的用户名。
  - password：ftp服务的密码。
  - basepath：ftp服务中，此用户的根路径。可用于存放上传时生成的临时文件。
  - filepath：文件所在的下级路径。最终存储的路径为：basepath + filepath 。

## 回调信息

业务系统可以提供一个GET方式的回调接口，在视频文件转换、回写完毕后，本服务可以调用此接口，传回处理的状态。

```json
	"callBackURL": "http://10.11.12.13/callback.do"
```

回调接口需要接收两个参数：

- file：处理后的文件名。本例为“001-online”。
- flag：处理后的状态，值为：success 或 error。

接口url示例：

```http
http://10.11.12.13/callback.do?file=001-online&flag=success
```

## 数据域

data域为传送给报表模板的核心值，其中为JSON数组。数组中的内容按照报表中的实际设置而定。

# 代码结构说明

本项目所有代码均在  com.thinkdifferent.data2pdf 之下，包含如下内容：

- config
  - Data2PDFConfig：本服务自有配置读取。
  - RabbitMQConfig：RabbitMQ服务配置读取。
- consumer
  - CreatePDFConsumer：MQ消费者，消费队列中传入的JSON参数，执行任务（Task）。
- controller
  - Data2Pdf：REST接口，提供直接生成PDF接口，和调用MQ异步生成PDF的接口。
- service
  - Data2PdfService：JSON数据生成PDF文件、文件回写上传、接口回调等核心逻辑处理。
  - RabbitMQService：将JSON消息加入到队列中的服务层处理。
- task
  - Task：异步多线程任务，供MQ消费者调用，最大限度的提升并行能力。
- utils
  - CreatePdfUtils：处理传入的JSON数据，调用JasperReport报表模板，生成报表PDF文件的工具类。
  - FtpUtil：FTP访问工具，包括上传、下载、删除等。
  - WriteBackUtil：回写文件、回调接口的工具类。