package org.throwable.rabbitmq.common.constants;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 11:28
 */
public interface RebbitmqConstants {

	String COLON = ":";

	String RABBIT_MANAGEMENT_TEMPLATE_URI_CONTEXT = "/api";

	String DEFAULT_RABBIT_VIRTUAL_HOST = "/";

	String DEFAULT_NAMEKEY_SUFFIX = "#";

	String RABBITADMIN_NAME_PREFIX = "rabbitAdmin#";

	String RABBIT_CONNECTION_FACTORY_NAME_PREFIX = "cachingConnectionFactory#";

	String ROUTING_RABBIT_CONNECTION_FACTORY_NAME_PREFIX = "routingConnectionFactory";

	String RABBIT_MANAGEMENT_TEMPLATE_NAME_PREFIX = "rabbitManagementTemplate#";

	String RABBIT_TEMPLATE_NAME_PREFIX = "rabbitTemplate";

	String MULTIINSTANCE_RABBIT_TEMPLATE_ADAPTER_NAME_PREFIX = "multiInstanceRabbitTemplateAdapter";

	String RABBIT_MESSAGE_LISTENER_CONTAINER_NAME_PREFIX = "simpleMessageListenerContainer#";

	String RABBIT_MESSAGE_LISTENER_CONTAINER_FACTORY_NAME_PREFIX = "rabbitListenerContainerFactory#";

	String DEFAULT_CHARSET_ENCODING = "UTF-8";

	String RABBITINSTANCE_DATABASE_PROVIDER_NAME_PREFIX = "rabbitInstanceDataBaseProvider";
}
