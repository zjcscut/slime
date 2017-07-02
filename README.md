[TOC]



# slime

**Slime**，史莱姆，一种虚构的生物。十分弱小，为了不被淘汰就必须改变自己，适应环境，时刻进化。

**Warn:整个体系依赖Springboot版本1.5.3.RELEASE。**

**Warn:这个markdown用Typora编写，如果用其他编辑器打开可能会导致布局出现错乱。**

## 安装

```
git clone https://github.com/zjcscut/slime.git
mvn clean install -Dmaven.test.skip=true
```

## slime-amqp-rabbitmq

**amqp协议下的rabbitmq模块，提供Rabbitmq多实例支持，添加数据库配置支持。**
基于对[Spring-Amqp源码的分析](https://github.com/zjcscut/Reading-Notes-Repository/blob/master/Spring%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/SpringAmqp%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/SpringAmqp%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)做了封装和改造。

导入依赖:

```xml
       <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-amqp-rabbitmq</artifactId>
            <version>1.0-RELEASE</version>
        </dependency>
```

Springboot主函数添加注解@**EnableAmqpRabbitmq:**

```java
@SpringBootApplication
@EnableAmqpRabbitmq
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

### 基于json的标准配置:

application.yaml:

```yaml
slime:
   amqp:
     rabbitmq:
      location: classpath:mq.json
      mode: json
      skipListenerClassNotFoundException： true
```

外置配置文件**mq.json**的标准格式:

```json
{
  "producers": [
    {
      "username": "guest",
      "password": "guest",
      "host": "localhost",
      "port": 5672,
      "virtualHost": "/",
      "useConfirmCallback": true,
      "mandatory": true,
      "useReturnCallback": true,
      "description": "本地生产者mq配置",
      "instanceSignature": "PRODUCER-1",
      "bindingParameters": [
        {
          "queueName": "queue-1",
          "exchangeName": "exchange1",
          "exchangeType": "DIRECT",
          "routingKey": "queue-key-1",
          "description": "Description"
        }
      ]
    }
  ],
  "consumers": [
    {
      "username": "guest",
      "password": "guest",
      "host": "localhost",
      "port": 5672,
      "virtualHost": "/",
      "description": "本地消费者mq配置",
      "instanceSignature": "LOCAL",
      "consumerBindingParameters": [
      ]
    },
    {
      "username": "throwable",
      "password": "admin",
      "host": "192.168.56.2",
      "port": 5672,
      "virtualHost": "/",
      "description": "远程消费者mq配置",
      "instanceSignature": "REMOTE",
      "consumerBindingParameters": [
      ]
    }
  ]
}
```

**WARN:**

* instanceSignature必须全局唯一。
* listenerClassName属性对应的类必须实现**MessageListener**和**ChannelAwareMessageListener**接口其中之一,必须确保此类为Spring容器的Bean(添加@Component)。
* 可以使用slime重写过的Listener注解注册消费者。

一个json配置的Listener的例子:

```java
@Component
public class Listener implements MessageListener {

	@Override
	public void onMessage(Message message) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("receive message --> " + new String(message.getBody()));
	}
}
```

外部配置文件mq.json中必须声明对应的listenerClass全类名:

```json
"consumers": [
    {
      "username": "guest",
      "password": "guest",
      "host": "localhost",
      "port": 5672,
      "virtualHost": "/",
      "description": "本地消费者mq配置",
      "instanceSignature": "LOCAL",
      "consumerBindingParameters": [
        {
          "queueName": "queue-1",
          "exchangeName": "exchange1",
          "exchangeType": "DIRECT",
          "routingKey": "queue-key-1",
          "description": "Description",
          "listenerClassName": "org.throwable.rabbitmq.support.Listener",
          "concurrentConsumers": 10,
          "maxConcurrentConsumers": 20
        }
      ]
    },
    {
      "username": "throwable",
      "password": "admin",
      "host": "192.168.56.2",
      "port": 5672,
      "virtualHost": "/",
      "description": "远程消费者mq配置",
      "instanceSignature": "REMOTE",
      "consumerBindingParameters": [
      ]
    }
  ]
```

通过注解配置的Listener的例子:

```java
@Component
public class LocalSlimeListener {

    @SlimeRabbitListener(instanceSignature = "LOCAL", bindings =
    @QueueBinding(
            value = @Queue(value = "queue-1", durable = "true"),
            exchange = @Exchange(value = "exchange1", durable = "true")
    ))
    public void onMessage(Message message) {
        System.out.println("LocalSlimeListener receive message --> " + newString(message.getBody()));
    }
}
```

```json
@Component
public class RemoteSlimeListener {

    @SlimeRabbitListener(instanceSignature = "REMOTE", bindings =
    @QueueBinding(
            value = @Queue(value = "queue-1", durable = "true"),
            exchange = @Exchange(value = "exchange1", durable = "true")
    ))
    public void onMessage(Message message) {
        System.out.println("RemoteSlimeListener receive message --> " + new String(message.getBody()));
    }
}
```

**WARN：**

* **配置文件中必须包含@SlimeRabbitListener指定的instanceSignature对应的mq实例，否则会注册失败抛出异常。**
* 注解@SlimeRabbitListener和@SlimeRabbitHandler的使用方式和@RabbitListener、@RabbitHandler的使用方式类似，instanceSignature字段是必须字段，用于指定mq实例。

Producer的例子:

```java
    @Autowired
	private RabbitTemplate rabbitTemplate;
	
	public void service(){
         SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), "PRODUCER-1");
		try {
			rabbitTemplate.convertAndSend("exchange1", "queue-key-1", "hello world!!!");
		} finally {
			SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
		}
	}
```

**WARN:**

1. slime-amqp-rabbitmq只注册了一个RabbitTemplate,绑定到一个命名为routingConnectionFactory的SimpleRoutingConnectionFactory,上面的方法就是使用SimpleRoutingConnectionFactory,通过传入PRODUCER-1这个instanceSign发送消息到对应Mq实例的交换器和路由键绑定的队列。不自动创建多个

   RabbitTemplate的原因是容易造成HardCode。你可以显式创建自定义的RabbitTemplate如下:

   ```java
   @Bean("rabbitTemplate#PRODUCER-1")
   public RabbitTemplate rabbitTemplate(@Qualifier("cachingConnectionFactory#PRODUCER-1") 
                                         CachingConnectionFactory factory) {
      return new RabbitTemplate(factory);                                        
   }
   ```

   使用的时候:@Autowired @Qualifier("rabbitTemplate#PRODUCER-1") RabbitTemplate rabbitTemplate。

   (上面的创建已经有HardCode的嫌疑,这样的编码很容易会为项目带来灾难，慎用)。


为了避免硬编码带来的危害，可以选用多mq实例RabbitTemplate适配器MultiInstanceRabbitTemplateAdapter,

用法大概如下：

```java
@Autowired
private MultiInstanceRabbitTemplateAdapter multiInstanceRabbitTemplateAdapter;

public void process(User u){
   multiInstanceRabbitTemplateAdapter.multiSendJson("your-exchange","your-routingKey",
      user, "your-mqInstanceSignature");
}
```

**Warn:** 同理，"your-mqInstanceSignature"必须在配置文件中指定。

为保证producer在发送消息的时候不丢失，slime-amqp-rabbitmq也对Spring-Amqp的消息确认和消息返回做了一定的封装，详细使用看本人写的一篇文章[Spring-Amqp源码的分析](https://github.com/zjcscut/Reading-Notes-Repository/blob/master/Spring%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/SpringAmqp%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/SpringAmqp%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md)的最后一个小节，以及源码中的下面几个类:

* ```
  AbstractRabbitConfirmCallback
  ```

* ```
  RabbitConfirmCallbackListener
  ```

* ```
  RabbitReturnCallbackListener
  ```

**另外，slime-amqp-rabbitmq也提供数据库配置支持。**

### 基于database的标准配置:

application.yaml:

```yaml
slime:
   amqp:
     rabbitmq:
      mode: database
      skipListenerClassNotFoundException：true
      dataSourceBeanName: dataSource
```

数据库的schema如下:

```sql
CREATE TABLE `rabbit_instance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instance_signature` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实例签名',
  `username` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'guest',
  `password` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'guest',
  `host` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'localhost',
  `port` int(11) DEFAULT '5672',
  `virtualHost` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '/',
  `description` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `suffix` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实例下所有的queue、exchange、routingKey的后缀',
  `useConfirmCallback` tinyint(1) DEFAULT 0,
  `mandatory` tinyint(1) DEFAULT 0,
  `useReturnCallback` tinyint(1) DEFAULT 0,
  `instanceType` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'PRODUCER',
  `isEnabled` tinyint(4) DEFAULT 1,
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP(),
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP(),
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uniq_instance` (`instance_signature`,`instanceType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `rabbit_binding_parameter` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instance_signature` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实例签名',
  `queueName` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL ,
  `exchangeName` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `exchangeType` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL ,
  `routingKey` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `listenerClassName` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bindingType` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'PRODUCER',
  `concurrentConsumers` INT DEFAULT 1,
  `maxConcurrentConsumers` INT DEFAULT 10,
  `acknowledgeMode` VARCHAR(10) DEFAULT 'AUTO',
  `isEnabled` tinyint(4) DEFAULT 1,
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP(),
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP(),
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uniq_binding_parameter` (`instance_signature`,`bindingType`,`queueName`,`listenerClassName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


```

WARN:

* instanceType是枚举，只有PRODUCER和CONSUMER两种选择，如果填写其他会默认使用PRODUCER。
* bindingType和instanceType的语义是相同的。
* instanceType和instance_signature是聚合唯一约束。

只是外部配置不一样，其他用法是相同的。

## slime-mybatis-mapper

**mybatis组件，通过接口继承提供无sql基础CRUD操作，通过condition等组件实现无sql拼写操作，这部分的设计参考了[通用mapper](http://git.oschina.net/free/Mapper/)的设计，做了改良和优化。**

添加依赖:

```xml
        <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-mybatis-mapper</artifactId>
            <version>1.0-RELEASE</version>
        </dependency>
```

Springboot主函数添加注解**@EnableMybatisMapper:**

```json
@SpringBootApplication
@EnableMybatisMapper
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

application.yaml:

```yaml
slime:
  mybatis:
    mapper:
      mapper-locations: mappings/*.xml
      type-aliases-package: com.throwable.common.entity
      base-packages:
      - com.throwable.dao
      ognlIdentityStrategy: "\\@java.util.UUID@randomUUID().toString().replace(\"-\", \"\")"
      configLocation: classpath:mybatis/config.xml
```

这个模块默认使用javax.sql.DataSource接口的实现作为数据源。

### 实体配置

```java
@Table(value = "user")
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    protected Long id;
    @Column(name = "creator")
    protected String creator;
    @Column(name = "create_date")
    protected Date createDate;
    省略getter和setter
}
```

* 当 @GeneratedValue指定generator为JDBC将会使用自增策略，请确保数据库主键列支持自增。
* 当 @GeneratedValue指定generator为UUID将会判断ognlIdentityStrategy是否有效，有效直接使用OGNL写入主键，无效则使用数据库使用数据库的主键回写方言(目前只支持Mysql的SELECT LAST_INSERT_ID()，这一项暂时不允许配置) 。
* 注意实体使用的jpa注解仅仅在slime-mybatis-mapper的所有api使用时生效，因为原生的mybatis并不是jpa的实现。

### SmartMapper接口

实体的mapper只要继承SmartMapper接口就能继承获得所有基础通用CRUD方法，这个接口提供如下方法：

```java
int insert(T t);
int insertNoneSkipPrimaryKey(T t);
int insertIngore(T t);
updateByPrimaryKey(T t);
int updateByPrimaryKey(T t,boolean allowUpdateToNull);
List<T> selectByConditionLimit(Condition condition, int limit);
PageModel<T> selectByConditionPage(Condition condition, int pageNumber, int pageSize);
PageModel<T> selectByConditionPage(Condition condition, Pager pager);
T selectOneByCondition(Condition condition);
long countByCondition(Condition condition);
List<T> selectByCondition(Condition condition);
```

### Condition组件

Condition组件主要提供无Sql条件拼写，并且和Condition相关的api使用，具体的条件子句有and、gt、or、in、lt等，使用方式如下：

```java
Condition condition = Condition.create(User.class);
		condition.gt("id", 1).like("name", "%throwable%").desc("id").or("name", "like", "%z%");
		List<User> users = userMapper.selectByCondition(condition);
		assertNotNull(users);
		for (User u : users) {
			System.out.println(u);
		}
		long count = userMapper.countByCondition(condition);
		System.out.println(count);
		PageModel<User> userPage = userMapper.selectByConditionPage(condition, new Pager(1, 10));
		assertNotNull(userPage);
```

详细的条件子句和使用方式见Condition的源码，比较简单的。

### 高级接口BatchExecutorService

不需依赖接口继承的实现，提供条件更新，批处理等方法。使用如下:

```java
@Autowired
private BatchExecutorService batchExecutorService;

public void batchUpdateUser(List<User> user){
     //改变值的操作
  
     //更新入库,每100条提交一次，跳过null值
     batchExecutorService.executeBatchUpdate(user,100, true);
}

```

详细的使用方式见BatchExecutorService的源码，比较简单的。

## slime-nosql-redis

**redis组件，提供redis单客户端以及集群支持。**

添加依赖:

```xml
        <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-nosql-redis</artifactId>
            <version>1.0-RELEASE</version>
        </dependency>
```

Springboot主函数添加注解@EnableRedisClient或者@EnableRedisCluster

```java
@SpringBootApplication
//@EnableRedisCluster
@EnableRedisClient
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

## slime-distributed-lock-redisson

**redisson组件，提供基于redisson的分布式锁支持,扩展了注解@RedissonDistributedLock以及模板方法RedissonLockTemplate。**

添加依赖:

```xml
        <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-distributed-lock-redisson</artifactId>
            <version>1.0-RELEASE</version>
        </dependency>
```
Springboot主函数添加注解@EnableRedissonDistributedLock

```java
@EnableRedissonDistributedLock
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```
application.yaml:

```yaml
slime:
  distributed:
    lock:
      redisson:
        location: redisson/config.yaml
```

redisson/config.yaml大概如下，详细可以参考redisson的github上的wiki，有详细的配置说明：

```yaml
---
singleServerConfig:
  idleConnectionTimeout: 10000
  pingTimeout: 1000
  connectTimeout: 10000
  timeout: 3000
  retryAttempts: 3
  retryInterval: 1500
  reconnectionTimeout: 3000
  failedAttempts: 3
  password: null
  subscriptionsPerConnection: 5
  clientName: null
  address: "redis://127.0.0.1:6379"
  subscriptionConnectionMinimumIdleSize: 1
  subscriptionConnectionPoolSize: 50
  connectionMinimumIdleSize: 10
  connectionPoolSize: 64
  database: 0
  dnsMonitoring: false
  dnsMonitoringInterval: 5000
threads: 0
nettyThreads: 0
codec: !<org.redisson.codec.JsonJacksonCodec> {}
useLinuxNativeEpoll: false
```

@RedissonDistributedLock注解描述如下：

```java
public @interface RedissonDistributedLock {

    //锁路径前缀
	String lockPathPrefix() default "REDISSON_LOCK_KEY_";
    //多个key的分隔符
	String keySeparator() default "_";
    //方法入参匹配的属性的属性名
	String[] keyNames();
    //锁等待时间
	long waitTime() default 5000;
    //锁持有的最大时间
	long leaseTime() default 15000;
    //TimeUnit
	TimeUnit unit() default TimeUnit.MILLISECONDS;
    //如果存在多个注解，通过此值排序，值越小越先执行
	int order() default 1;
    //是否使用公平锁
	boolean isFair() default false;
}
```

RedissonLockTemplate的使用方式:

```java
@Autowired
private RedissonLockTemplate redissonLockTemplate;

public void process(User u){
    String lockPath = "key_" + u.getAccount();
    redissonLockTemplate.execute(lockPath，waitTime，leaseTime，TimeUnit，new    RedissonLockCallback<T>(){
            
           public T doInLock(){
               //需要加锁的业务逻辑
               
               return t;
           }
    } );
}
```



## slime-dynamic-druid-datasource

**基于Druid和SpringJdbc实现多数据源动态切换。**

添加依赖:

```xml
        <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-dynamic-druid-datasource</artifactId>
            <version>1.0-RELEASE</version>
        </dependency>
```

Springboot主函数添加注解@EnableDynamicDruidDataSource

```java
@EnableDynamicDruidDataSource
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

application.yaml:

```yaml
slime:
  druid:
    location: druid/druid.json
```

一个标准的外部druid.json的配置大概如下:

```json
{
  "druids": [
    {
      "signature": "local",
      "url": "jdbc:mysql://localhost:3306/slime",
      "username": "root",
      "password": "root",
      "driverClassName": "com.mysql.jdbc.Driver",
      "primary":true
    },
    {
      "signature": "remote",
      "url": "jdbc:mysql://192.168.56.101:3306/remote",
      "username": "throwable",
      "password": "admin",
      "driverClassName": "com.mysql.jdbc.Driver",
      "primary":false
    }
  ]
}
```

* 注意上面的json数组中每个元素一共有6个k-v，这6个属性是必须的，其他属性见下面的属性表。
* signature是数据源区别的唯一标识，需要全局唯一，在使用注解@AutoDynamicDataSource或者模板方法DynamicDruidTemplate时就是通过signature切换到目标数据源的。

数据源配置的支持参数(更详细见DruidAbstractDataSource):

```java
private volatile Boolean defaultAutoCommit;
	private volatile Boolean defaultReadOnly;
	private volatile Integer defaultTransactionIsolation;
	private volatile String defaultCatalog;
	
	private volatile String username;
	private volatile String password;
	private volatile String url;
	private volatile String driverClassName;


	private volatile Integer initialSize;
	private volatile Integer maxActive;
	private volatile Integer minIdle;
	private volatile Integer maxIdle;
	private volatile Long maxWait;
	private Integer notFullTimeoutRetryCount;

	private volatile String validationQuery;
	private volatile Integer validationQueryTimeout;
	private volatile Boolean testOnBorrow;
	private volatile Boolean testOnReturn;
	private volatile Boolean testWhileIdle;
	private volatile Boolean poolPreparedStatements;
	private volatile Boolean sharePreparedStatements;
	private volatile Integer maxPoolPreparedStatementPerConnectionSize;


	private volatile Integer queryTimeout;
	private volatile Integer transactionQueryTimeout;


	private Boolean clearFiltersEnable;
	private volatile Integer maxWaitThreadCount;

	private volatile Boolean accessToUnderlyingConnectionAllowed;

	private volatile Long timeBetweenEvictionRunsMillis;

	private volatile Integer numTestsPerEvictionRun;

	private volatile Long minEvictableIdleTimeMillis;
	private volatile Long maxEvictableIdleTimeMillis;

	private volatile Long phyTimeoutMillis;

	private volatile Boolean removeAbandoned;

	private volatile Long removeAbandonedTimeoutMillis;

	private volatile Boolean logAbandoned;

	private volatile Integer maxOpenPreparedStatements;

	private volatile String dbType;

	private volatile Long timeBetweenConnectErrorMillis;

	private Integer connectionErrorRetryAttempts;

	private Boolean breakAfterAcquireFailure;

	private Long transactionThresholdMillis;

	private Boolean failFast;
	private Integer maxCreateTaskCount;
	private Boolean asyncCloseConnectionEnable;
	private Boolean initVariants;
	private Boolean initGlobalVariants;

	private Boolean useUnfairLock;

	private Boolean useLocalSessionState;

	private Long timeBetweenLogStatsMillis;
	private String exceptionSorter;
	
	//filter配置
	private String filters;
	private String signature;  //唯一标识,同时作为name属性
	private Boolean primary; //是否主数据源,只有一个
```

使用例子:

```java
@Service
public class DataSourceService {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private DynamicDruidTemplate dynamicDruidTemplate;

	@AutoDynamicDataSource(value = "remote")
	public void process() throws Exception {
		System.out.println("process");
		DataSource dataSource = context.getBean(DataSource.class);
		Connection connection = dataSource.getConnection();
		Statement st = connection.createStatement();
		ResultSet resultSet = st.executeQuery("SELECT 1");
		while (resultSet.next()) {
			System.out.println(resultSet.getString(1));
		}
		//spring aop代理的局限,自调用是无法命中切点的,@Aspect注解声明的切点没有这个局限
		context.getBean(DataSourceService.class).process2();
		context.getBean(DataSourceService.class).process3();
	}

	@AutoDynamicDataSource(value = "local")
	public void process2() throws Exception {
		System.out.println("process2");
		DataSource dataSource = context.getBean(DataSource.class);
		Connection connection = dataSource.getConnection();
		Statement st = connection.createStatement();
		ResultSet resultSet = st.executeQuery("SELECT 1");
		while (resultSet.next()) {
			System.out.println(resultSet.getString(1));
		}
	}

	public void process3() throws Exception {
		System.out.println(dynamicDruidTemplate.execute("local", () -> {
			System.out.println("process2");
			DataSource dataSource = context.getBean(DataSource.class);
			Connection connection;
			try {
				connection = dataSource.getConnection();
				Statement st = connection.createStatement();
				ResultSet resultSet = st.executeQuery("SELECT 1");
				while (resultSet.next()) {
					System.out.println(resultSet.getString(1));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return "process3 success!";
		}));
	}
}
```

* 值得注意的是，多数据源的事务属于分布式事务的范畴，因此像spring-jdbc包的**DataSourceTransactionManager**以及Hibernate自带的**HibernateTransactionManager**是无法保证多数据源的事务性，更详细的分析请自行搜索分布式事务。

## slime-distributed-scheduler

**基于Spring和Quartz实现的分布式任务调度器，有两种选择：中心化(触发器集中管理，客户端任务等待服务端触发)和去中心化(触发器交由客户端自身管理，任务由客户端自身触发，服务端只负责注册、监控和收集数据)。**

## 未完待续...





