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

