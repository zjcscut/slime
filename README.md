# slime

#### Slime，史莱姆，一种虚构的生物。十分弱小，为了不被淘汰就必须改变自己，适应环境，时刻进化。

### Warn:整个体系依赖Springboot的最新版本1.5.3RELEASE

#### slime-amqp-rabbitmq

amqp协议下的rabbitmq模块，提供Rabbitmq多实例支持。

导入依赖:

```xml 
       <dependency>
            <groupId>org.throwable</groupId>
            <artifactId>slime-amqp-rabbitmq</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

使用指南:

入口函数Application添加注解**@EnableAmqpRabbitmq: **

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

```java
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
      "instanceSign": "PRODUCER-1",
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
      "instanceSign": "CONSUMER-1",
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

**WARN: **

 1.instanceSign必须保证和host、port绑定并且全局唯一;

 2.listenerClassName属性对应的类必须实现**MessageListener**和**ChannelAwareMessageListener**接口其中之一,必须确保此类为Spring容器的Bean(添加@Component)

一个Listener的例子:

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

基于Zookeeper和Redisson实现分布式锁。



####未完待续...





