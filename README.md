# slime

#### Slime，史莱姆，一种虚构的生物。十分弱小，为了不被淘汰就必须改变自己，适应环境，时刻进化。

### Warn:整个体系依赖Springboot版本1.5.3.RELEASE

### 安装

```
git clone https://github.com/zjcscut/slime.git
maven clean install
```

#### slime-amqp-rabbitmq

**amqp协议下的rabbitmq模块，提供Rabbitmq多实例支持。**

导入依赖:

```xml
       <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-amqp-rabbitmq</artifactId>
            <version>1.0-SNAPSHOT</version>
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

标准配置:

application.yaml:

```yaml
slime:
   amqp:
     rabbitmq:
      location: classpath:mq.json
      mode: json
```

外置配置文件mq.json的标准格式:

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
      "instanceSignature": "CONSUMER-1",
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
    }
  ]
}
```

**WARN:**

* instanceSignature必须保证和host、port绑定并且全局唯一。
* listenerClassName属性对应的类必须实现**MessageListener**和**ChannelAwareMessageListener**接口其中之一,必须确保此类为Spring容器的Bean(添加@Component)。
* 可以使用slime重写过的Listener注解注册消费者。

一个配置的Listener的例子:

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

一个通过注解配置的Listener的例子:

```java
@Component
public class SlimeListener {

    @SlimeRabbitListener(instanceSignature = "CONSUMER-1", bindings =
    @QueueBinding(
            value = @Queue(value = "queue-1", durable = "true"),
            exchange = @Exchange(value = "exchange1", durable = "true")
    ))
    public void onMessage(Message message) {
        System.out.println("SlimeListener receive message --> " + newString(message.getBody()));
    }
}
```

**WARN：**

* **配置文件中必须包含@SlimeRabbitListener指定的instanceSignature对应的mq实例，否则会注册失败抛出异常。**
* 注解@SlimeRabbitListener和@SlimeRabbitHandler的使用方式和@RabbitListener、@RabbitHandler的使用方式类似，instanceSignature字段是必须字段，用于指定mq实例。

一个Producer的例子:

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

   (上面的创建已经有HardCode的嫌疑,这样的编码很容易会为项目带来灾难,慎用)。



### slime-distributed-lock

**基于Zookeeper和Redisson实现分布式锁。**

导入maven依赖:

```xml
       <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-distributed-lock</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

application.yaml配置:

```yaml
slime:
   distrubited:
        lock:
          zookeeperConfigurationLocation: classpath:zookeeper.yaml
```

zookeeper.yaml:

```yaml
zookeeperClientProperties:
        connectString: 127.0.0.1:2184
        sessionTimeoutMs: 300000
        connectionTimeoutMs: 300000
        baseSleepTimeMs: 5000
        maxRetries: 3
        baseLockPath: /zk/lock
```

Springboot主函数添加@EnableDistributedLock注解:

```java
@SpringBootApplication
@EnableDistributedLock
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

实体类:

```java
@Data
@NoArgsConstructor
public class User {
	
	private Long id;
	private String name;
	private String account;
	private Integer age;

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", account='" + account + '\'' +
				", age=" + age +
				'}';
	}
}
```

服务类:

```java
@Service
@Slf4j
public class LockService {
    
    @DistributedLock(policy = LockPolicyEnum.ZOOKEEPER, target = User.class, keyName =    "account", waitSeconds = 11)
	public void processTarget(User user) {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.warn("process user :" + user.toString());
	}
}
```

测试类:

```java
    @Autowired
    private LockService lockService;

    @Test
    public void testProcess()throws Exception{
        final User user = new User();
        final String name = "throwable";
        user.setId(10086L);
        user.setAge(24);
        user.setName(name);
        user.setAccount("throwable-10086");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(() -> lockService.processTarget(user)));
        }
        threads.forEach(Thread::start);

        Thread.sleep(Integer.MAX_VALUE);
    }
```

#### 未完待续...





