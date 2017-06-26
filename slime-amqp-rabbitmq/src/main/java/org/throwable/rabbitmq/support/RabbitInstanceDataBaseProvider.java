package org.throwable.rabbitmq.support;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.throwable.rabbitmq.common.constants.RabbitInstanceTypeEnum;
import org.throwable.rabbitmq.common.entity.BindingParameterEntity;
import org.throwable.rabbitmq.common.entity.RabbitInstanceEntity;
import org.throwable.rabbitmq.configuration.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/24 13:18
 */
class RabbitInstanceDataBaseProvider {

	private static final String[] PRODUCER_BINDING_PARAMETER_IGNORE_FIELDS =
			{"bindingType", "instanceSignature", "listenerClassName", "concurrentConsumers",
					"maxConcurrentConsumers", "acknowledgeMode","isEnabled","createTime","updateTime"};

	private static final String[] CONSUMER_BINDING_PARAMETER_IGNORE_FIELDS =
			{"bindingType", "instanceSignature", "isEnabled","createTime","updateTime"};

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@SuppressWarnings("unchecked")
	public SlimeRabbitmqProperties convertDataToSlimeRabbitmqProperties() {
		List<RabbitInstanceEntity> instanceEntities = jdbcTemplate.query("SELECT * FROM `rabbit_instance` WHERE isEnabled = ?",
				new BeanPropertyRowMapper(RabbitInstanceEntity.class),
				1);
		List<BindingParameterEntity> bindingParameterEntities = jdbcTemplate.query("SELECT * FROM `rabbit_binding_parameter` WHERE isEnabled = ?",
				new BeanPropertyRowMapper(BindingParameterEntity.class),
				1);
		SlimeRabbitmqProperties sp = new SlimeRabbitmqProperties();
		if (null != instanceEntities && !instanceEntities.isEmpty()) {
			sp = mergeResolveRabbitInstanceAndBindingData(instanceEntities, bindingParameterEntities);
		}
		return sp;
	}

	private SlimeRabbitmqProperties mergeResolveRabbitInstanceAndBindingData(List<RabbitInstanceEntity> instanceEntities,
																			 List<BindingParameterEntity> bindingParameterEntities) {
		List<RabbitmqProducerProperties> producers = new ArrayList<>();
		List<RabbitmqConsumerProperties> consumers = new ArrayList<>();
		SlimeRabbitmqProperties sp = new SlimeRabbitmqProperties();
		sp.setProducers(producers);
		sp.setConsumers(consumers);
		for (RabbitInstanceEntity instanceEntity : instanceEntities) {
			switch (RabbitInstanceTypeEnum.valueOf(instanceEntity.getInstanceType())) {
				case PRODUCER:
					resolveProducerInstancePair(producers, instanceEntity, bindingParameterEntities);
					break;
				case CONSUMER:
					resolveConsumerInstancePair(consumers, instanceEntity, bindingParameterEntities);
					break;
				default: {
					resolveProducerInstancePair(producers, instanceEntity, bindingParameterEntities);
				}
			}
		}
		return sp;
	}

	private void resolveProducerInstancePair(List<RabbitmqProducerProperties> producers,
											 RabbitInstanceEntity instanceEntity,
											 List<BindingParameterEntity> bindingParameterEntities) {
		RabbitmqProducerProperties producer = new RabbitmqProducerProperties();
		producers.add(producer);
		producer.setMandatory(instanceEntity.getMandatory());
		producer.setUseReturnCallback(instanceEntity.getUseReturnCallback());
		producer.setUseConfirmCallback(instanceEntity.getUseConfirmCallback());
		producer.setUsername(instanceEntity.getUsername());
		producer.setPassword(instanceEntity.getPassword());
		producer.setHost(instanceEntity.getHost());
		producer.setPort(instanceEntity.getPort());
		producer.setVirtualHost(instanceEntity.getVirtualHost());
		producer.setInstanceSignature(instanceEntity.getInstanceSignature());
		producer.setDescription(instanceEntity.getDescription());
		producer.setSuffix(instanceEntity.getSuffix());
		List<BindingParameter> bindingParameters = new ArrayList<>();
		producer.setBindingParameters(bindingParameters);
		if (null != bindingParameterEntities && !bindingParameterEntities.isEmpty()) {
			bindingParameterEntities.forEach(bindingParameterEntity -> {
				if (bindingParameterEntity.getInstanceSignature().equals(instanceEntity.getInstanceSignature())
						&& bindingParameterEntity.getBindingType().equals(instanceEntity.getInstanceType())) {
					BindingParameter bindingParameter = new BindingParameter();
					BeanUtils.copyProperties(bindingParameterEntity, bindingParameter, PRODUCER_BINDING_PARAMETER_IGNORE_FIELDS);
					bindingParameters.add(bindingParameter);
				}
			});
		}
	}

	private void resolveConsumerInstancePair(List<RabbitmqConsumerProperties> consumers,
											 RabbitInstanceEntity instanceEntity,
											 List<BindingParameterEntity> bindingParameterEntities) {
		RabbitmqConsumerProperties consumer = new RabbitmqConsumerProperties();
		consumers.add(consumer);
		consumer.setUsername(instanceEntity.getUsername());
		consumer.setPassword(instanceEntity.getPassword());
		consumer.setHost(instanceEntity.getHost());
		consumer.setPort(instanceEntity.getPort());
		consumer.setVirtualHost(instanceEntity.getVirtualHost());
		consumer.setInstanceSignature(instanceEntity.getInstanceSignature());
		consumer.setDescription(instanceEntity.getDescription());
		consumer.setSuffix(instanceEntity.getSuffix());
		List<ConsumerBindingParameter> consumerBindingParameters = new ArrayList<>();
		consumer.setConsumerBindingParameters(consumerBindingParameters);
		if (null != bindingParameterEntities && !bindingParameterEntities.isEmpty()) {
			bindingParameterEntities.forEach(bindingParameterEntity -> {
				if (bindingParameterEntity.getInstanceSignature().equals(instanceEntity.getInstanceSignature())
						&& bindingParameterEntity.getBindingType().equals(instanceEntity.getInstanceType())) {
					ConsumerBindingParameter consumerBindingParameter = new ConsumerBindingParameter();
					BeanUtils.copyProperties(bindingParameterEntity, consumerBindingParameter, CONSUMER_BINDING_PARAMETER_IGNORE_FIELDS);
					consumerBindingParameters.add(consumerBindingParameter);
				}
			});
		}
	}
}
