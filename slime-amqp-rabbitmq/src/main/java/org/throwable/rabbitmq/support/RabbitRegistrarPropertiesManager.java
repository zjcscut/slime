package org.throwable.rabbitmq.support;

import org.springframework.util.Assert;
import org.throwable.rabbitmq.configuration.BindingParameter;
import org.throwable.rabbitmq.configuration.ConsumerBindingParameter;
import org.throwable.rabbitmq.exception.RabbitmqInstanceCreationException;
import org.throwable.rabbitmq.exception.RabbitmqRegisterException;
import org.throwable.utils.ArrayUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:09
 */
abstract class RabbitRegistrarPropertiesManager {

	/**
	 * cache rabbitmq instance info
	 * key: instance signature
	 * value: instanceHolder
	 */
	private static final ConcurrentMap<String, InstanceHolder> instances = new ConcurrentHashMap<>();

	/**
	 * cache rabbitmq producer instance info
	 * key: instance signature
	 * value: instanceHolder
	 */
	private static final ConcurrentMap<String, InstanceHolder> producerInstances = new ConcurrentHashMap<>();

	/**
	 * cache rabbitmq consumer instance info
	 * key: instance signature
	 * value: instanceHolder
	 */
	private static final ConcurrentMap<String, InstanceHolder> consumerInstances = new ConcurrentHashMap<>();

	/**
	 * cache rabbitmq producer binding parameters
	 * key: instance signature
	 * value: List BindingParameter
	 */
	private static final ConcurrentMap<String, List<BindingParameter>> producerBindingParameters = new ConcurrentHashMap<>();

	/**
	 * cache rabbitmq listener binding parameters
	 * key: instance signature
	 * value: List ConsumerBindingParameter
	 */
	private static final ConcurrentMap<String, List<ConsumerBindingParameter>> consumerBindingParameters = new ConcurrentHashMap<>();

	/**
	 * merge listener binding parameters and producer binding parameters
	 * key: instance signature
	 * value: List BindingParameter
	 */
	private static final ConcurrentMap<String, List<BindingParameter>> bindingParameters = new ConcurrentHashMap<>();

	protected static boolean addProducerInstance(String instanceSignature, InstanceHolder instanceHolder) {
		return addInstance(instanceSignature, instanceHolder, producerInstances);
	}

	public static Map<String, InstanceHolder> getAllProducerInstances() {
		return Collections.unmodifiableMap(producerInstances);
	}

	public static InstanceHolder getProducerInstance(String instanceSignature) {
		return producerInstances.get(instanceSignature);
	}

	protected static boolean addConsumerInstance(String instanceSignature, InstanceHolder instanceHolder) {
		return addInstance(instanceSignature, instanceHolder, consumerInstances);
	}

	public static Map<String, InstanceHolder> getAllConsumerInstances() {
		return Collections.unmodifiableMap(consumerInstances);
	}

	public static InstanceHolder getConsumerInstance(String instanceSignature) {
		return consumerInstances.get(instanceSignature);
	}

	public static Map<String, InstanceHolder> getAllInstances() {
		return Collections.unmodifiableMap(instances);
	}

	public static InstanceHolder getInstance(String instanceSignature) {
		return instances.get(instanceSignature);
	}

	protected static void mergeProducerAndConsumerInstances() {
		instances.putAll(consumerInstances);
		instances.putAll(producerInstances);
	}

	protected static void mergetProducerAndConsumerBindingParameters() {
		bindingParameters.putAll(producerBindingParameters);
		for (Map.Entry<String, List<ConsumerBindingParameter>> entry : consumerBindingParameters.entrySet()) {
			if (null == bindingParameters.get(entry.getKey())) {
				bindingParameters.put(entry.getKey(), parseBindingParameter(entry.getValue()));
			} else {
				List<BindingParameter> target = parseBindingParameter(entry.getValue());
				List<BindingParameter> origin = bindingParameters.get(entry.getKey());
				bindingParameters.put(entry.getKey(), mergerBindingParameters(origin, target));
			}
		}
	}

	public static List<BindingParameter> parseBindingParameter(List<ConsumerBindingParameter> consumerBindingParameters) {
		List<BindingParameter> bindingParameters = new ArrayList<>(consumerBindingParameters.size());
		for (ConsumerBindingParameter consumerBindingParameter : consumerBindingParameters) {
			BindingParameter bindingParameter = new BindingParameter();
			bindingParameter.setQueueName(consumerBindingParameter.getQueueName());
			bindingParameter.setRoutingKey(consumerBindingParameter.getRoutingKey());
			bindingParameter.setExchangeType(consumerBindingParameter.getExchangeType());
			bindingParameter.setExchangeName(consumerBindingParameter.getExchangeName());
			bindingParameter.setDescription(consumerBindingParameter.getDescription());
			bindingParameters.add(bindingParameter);
		}
		return bindingParameters;
	}

	/**
	 * if two BindingParameter object has the same queueName,they will be merge as one BindingParameter(first one)
	 */
	private static List<BindingParameter> mergerBindingParameters(List<BindingParameter> origin, List<BindingParameter> target) {
		origin.addAll(target);
		return origin;
	}

	public static Map<String, List<BindingParameter>> getAllBindingParameters() {
		return Collections.unmodifiableMap(bindingParameters);
	}

	public static Map<String, List<ConsumerBindingParameter>> getAllConsumerBindingParameters() {
		return Collections.unmodifiableMap(consumerBindingParameters);
	}

	public static Map<String, List<BindingParameter>> getAllProducerBindingParameters() {
		return Collections.unmodifiableMap(producerBindingParameters);
	}

	private static boolean addInstance(String instanceSignature, InstanceHolder instanceHolder, ConcurrentMap<String, InstanceHolder> instances) {
		Assert.hasText(instanceSignature, "Rabbitmq instance sign must not be empty");
		Assert.hasText(instanceHolder.getInstance().getHost(), "Rabbitmq instance host must not be empty");
		Assert.notNull(instanceHolder.getInstance().getPort(), "Rabbitmq instance port must not be null");
		Assert.hasText(instanceHolder.getInstance().getUsername(), "Rabbitmq instance username must not be empty");
		Assert.hasText(instanceHolder.getInstance().getPassword(), "Rabbitmq instance password must not be empty");
		if (null != instances.get(instanceSignature)) {
			throw new RabbitmqInstanceCreationException("Instance sign has already been existed!!Please make sure that instance sign must be unique globally");
		}
		if (isInstanceHolderExisted(instanceHolder, instances)) {
			throw new RabbitmqInstanceCreationException(String.format("Instance has already been existed!!Host:%s,port:%s",
					instanceHolder.getInstance().getHost(), instanceHolder.getInstance().getPort()));
		}
		return null != instances.put(instanceSignature, instanceHolder);
	}

	private static boolean isInstanceHolderExisted(InstanceHolder instanceHolder, ConcurrentMap<String, InstanceHolder> instances) {
		return instances.size() > 0
				&& instances.values().stream().anyMatch(
				instance -> instance.getInstance().getHost().equals(instanceHolder.getInstance().getHost())
						&& instance.getInstance().getPort().equals(instanceHolder.getInstance().getPort()));
	}

	private static InstanceHolder getInstanceHolder(String instanceSignature) {
		return instances.get(instanceSignature);
	}

	private static String getinstanceSignature(InstanceHolder instanceHolder) {
		for (Map.Entry<String, InstanceHolder> entry : instances.entrySet()) {
			InstanceHolder holder = entry.getValue();
			if (holder.getInstance().getHost().equals(instanceHolder.getInstance().getHost())
					&& holder.getInstance().getPort().equals(instanceHolder.getInstance().getPort())) {
				return entry.getKey();
			}
		}
		return null;
	}

	private static String getinstanceSignature(String host, Integer port) {
		InstanceHolder instanceHolder = new InstanceHolder();
		instanceHolder.getInstance().setHost(host);
		instanceHolder.getInstance().setPort(port);
		return getinstanceSignature(instanceHolder);
	}

	protected static boolean addProducerBindingParameters(String instanceSignature, List<BindingParameter> bindingParameters) {
		checkProducerBindingParameters(instanceSignature, bindingParameters);
		return null != producerBindingParameters.put(instanceSignature, bindingParameters);
	}

	private static void checkProducerBindingParameters(String instanceSignature, List<BindingParameter> bindingParameters) {
		if (null != bindingParameters && !bindingParameters.isEmpty()) {
			bindingParameters.forEach(p -> Assert.hasText(p.getQueueName(), "Producer queue name must not be empty"));
			List<String> queues = bindingParameters.stream().map(BindingParameter::getQueueName).collect(Collectors.toList());
			List<String> duplicatedQueueNames = ArrayUtils.getDuplicatedElements(queues);
			if (null != queues && !duplicatedQueueNames.isEmpty()) {
				throw new RabbitmqRegisterException(String.format("Producer queues %s of instance [%s] has been defined!Please check your configuration.", duplicatedQueueNames, instanceSignature));
			}
		}
	}

	public static List<BindingParameter> getProducerBindingParameters(String instanceSignature) {
		return Collections.unmodifiableList(producerBindingParameters.get(instanceSignature));
	}

	protected static boolean addConsumerBindingParameters(String instanceSignature, List<ConsumerBindingParameter> bindingParameters) {
		checkConsumerBindingParameters(instanceSignature, bindingParameters);
		return null != consumerBindingParameters.put(instanceSignature, bindingParameters);
	}

	public static void addConsumerBindingParameter(String instanceSignature, ConsumerBindingParameter bindingParameter) {
		Assert.notNull(instances.get(instanceSignature), String.format("Rabbitmq instance of instanceSignature [%s] must not be null!", instanceSignature));
		List<ConsumerBindingParameter> parameters = consumerBindingParameters.get(instanceSignature);
		if (null != parameters) {
			List<ConsumerBindingParameter> destList = new ArrayList<>(parameters.size());
			Collections.copy(destList, parameters);
			destList.add(bindingParameter);
			checkConsumerBindingParameters(instanceSignature, destList);
			destList.clear();
			parameters.add(bindingParameter);
		} else {
			parameters = new ArrayList<>();
			parameters.add(bindingParameter);
		}
	}

	public static List<ConsumerBindingParameter> getConsumerBindingParameters(String instanceSignature) {
		return Collections.unmodifiableList(consumerBindingParameters.get(instanceSignature));
	}

	private static void checkConsumerBindingParameters(String instanceSignature, List<ConsumerBindingParameter> bindingParameters) {
		if (null != bindingParameters && !bindingParameters.isEmpty()) {
			bindingParameters.forEach(p -> {
				Assert.hasText(p.getListenerClassName(), "Listener class name must not be empty");
				Assert.hasText(p.getQueueName(), "Listener queue name must not be empty");
			});
			Set<ConsumerBindingParameter> filter = new HashSet<>();
			bindingParameters.forEach(consumerBindingParameter -> {
				if (filter.contains(consumerBindingParameter)) {
					throw new RabbitmqRegisterException(String.format("Rabbitmq instance of instanceSignature [%s] has duplicated listener configuration property,className:%s,queue:%s",
							instanceSignature, consumerBindingParameter.getListenerClassName(), consumerBindingParameter.getQueueName()));
				} else {
					filter.add(consumerBindingParameter);
				}
			});
		}
	}


}
