package org.throwable.rabbitmq.support;

import org.springframework.util.Assert;
import org.throwable.rabbitmq.configuration.BindingParameter;
import org.throwable.rabbitmq.configuration.ConsumerBindingParameter;
import org.throwable.rabbitmq.exception.RabbitmqInstanceCreationException;

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
public abstract class RabbitmqRegistrarPropertiesManager {

    /**
     * cache rabbitmq instance info
     * key: instance sign
     * value: instanceHolder
     */
    private static final ConcurrentMap<String, InstanceHolder> instances = new ConcurrentHashMap<>();

    /**
     * cache rabbitmq producer instance info
     * key: instance sign
     * value: instanceHolder
     */
    private static final ConcurrentMap<String, InstanceHolder> producerInstances = new ConcurrentHashMap<>();

    /**
     * cache rabbitmq consumer instance info
     * key: instance sign
     * value: instanceHolder
     */
    private static final ConcurrentMap<String, InstanceHolder> consumerInstances = new ConcurrentHashMap<>();

    /**
     * cache rabbitmq producer binding parameters
     * key: instance sign
     * value: List BindingParameter
     */
    private static final ConcurrentMap<String, List<BindingParameter>> producerBindingParameters = new ConcurrentHashMap<>();

    /**
     * cache rabbitmq listener binding parameters
     * key: instance sign
     * value: List ConsumerBindingParameter
     */
    private static final ConcurrentMap<String, List<ConsumerBindingParameter>> consumerBindingParameters = new ConcurrentHashMap<>();

    /**
     * merge listener binding parameters and producer binding parameters
     * key: instance sign
     * value: List BindingParameter
     */
    private static final ConcurrentMap<String, List<BindingParameter>> bindingParameters = new ConcurrentHashMap<>();

    /**
     * Producer queue names that has been decleared
     * key:instanceSign
     * value:queueName
     */
    private static final Map<String, Set<String>> declearedProducerQueueNames = new ConcurrentHashMap<>();

    /**
     * Consumer queue names that has been decleared
     * key:instanceSign
     * value:queueName
     */
    private static final Map<String, Set<String>> declearedConsumerQueueNames = new ConcurrentHashMap<>();


    protected static boolean addProducerInstance(String instanceSign, InstanceHolder instanceHolder) {
        return addInstance(instanceSign, instanceHolder, producerInstances);
    }

    public static Map<String, InstanceHolder> getAllProducerInstances() {
        return Collections.unmodifiableMap(producerInstances);
    }

    public static InstanceHolder getProducerInstance(String instanceSign) {
        return producerInstances.get(instanceSign);
    }

    protected static boolean addConsumerInstance(String instanceSign, InstanceHolder instanceHolder) {
        return addInstance(instanceSign, instanceHolder, consumerInstances);
    }

    public static Map<String, InstanceHolder> getAllConsumerInstances() {
        return Collections.unmodifiableMap(consumerInstances);
    }

    public static InstanceHolder getConsumerInstance(String instanceSign) {
        return consumerInstances.get(instanceSign);
    }

    public static Map<String, InstanceHolder> getAllInstances() {
        return Collections.unmodifiableMap(instances);
    }

    public static InstanceHolder getInstance(String instanceSign) {
        return instances.get(instanceSign);
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

    private static boolean addInstance(String instanceSign, InstanceHolder instanceHolder, ConcurrentMap<String, InstanceHolder> instances) {
        Assert.hasText(instanceSign, "Rabbitmq instance sign must not be empty");
        Assert.hasText(instanceHolder.getInstance().getHost(), "Rabbitmq instance host must not be empty");
        Assert.notNull(instanceHolder.getInstance().getPort(), "Rabbitmq instance port must not be null");
        Assert.hasText(instanceHolder.getInstance().getUsername(), "Rabbitmq instance username must not be empty");
        Assert.hasText(instanceHolder.getInstance().getPassword(), "Rabbitmq instance password must not be empty");
        if (null != instances.get(instanceSign)) {
            throw new RabbitmqInstanceCreationException("Instance sign has already been existed!!Please make sure that instance sign must be unique globally");
        }
        if (isInstanceHolderExisted(instanceHolder, instances)) {
            throw new RabbitmqInstanceCreationException(String.format("Instance has already been existed!!Host:%s,port:%s",
                    instanceHolder.getInstance().getHost(), instanceHolder.getInstance().getPort()));
        }
        return null != instances.put(instanceSign, instanceHolder);
    }

    private static boolean isInstanceHolderExisted(InstanceHolder instanceHolder, ConcurrentMap<String, InstanceHolder> instances) {
        return instances.size() > 0
                && instances.values().stream().anyMatch(
                instance -> instance.getInstance().getHost().equals(instanceHolder.getInstance().getHost())
                        && instance.getInstance().getPort().equals(instanceHolder.getInstance().getPort()));
    }

    private static InstanceHolder getInstanceHolder(String instanceSign) {
        return instances.get(instanceSign);
    }

    private static String getInstanceSign(InstanceHolder instanceHolder) {
        for (Map.Entry<String, InstanceHolder> entry : instances.entrySet()) {
            InstanceHolder holder = entry.getValue();
            if (holder.getInstance().getHost().equals(instanceHolder.getInstance().getHost())
                    && holder.getInstance().getPort().equals(instanceHolder.getInstance().getPort())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static String getInstanceSign(String host, Integer port) {
        InstanceHolder instanceHolder = new InstanceHolder();
        instanceHolder.getInstance().setHost(host);
        instanceHolder.getInstance().setPort(port);
        return getInstanceSign(instanceHolder);
    }

    protected static boolean addProducerBindingParameter(String instanceSign, List<BindingParameter> bindingParameters) {
        checkProducerBindingParameters(instanceSign, bindingParameters);
        return null != producerBindingParameters.put(instanceSign, bindingParameters);
    }

    private static void checkProducerBindingParameters(String instanceSign, List<BindingParameter> bindingParameters) {
        bindingParameters.forEach(p -> Assert.hasText(p.getQueueName(), "Producer queue name must not be empty"));
        bindingParameters.forEach(p -> {
            if (null != declearedProducerQueueNames.get(instanceSign)) {
                Set<String> queueNames = declearedProducerQueueNames.get(instanceSign);
                if (queueNames.contains(p.getExchangeName())) {
                    throw new RabbitmqInstanceCreationException(String.format("Producer queue [%s] has been decleared!!!Please check your configuration", p.getQueueName()));
                } else {
                    queueNames.add(p.getExchangeName());
                }
            } else {
                declearedProducerQueueNames.put(instanceSign, bindingParameters.stream().map(BindingParameter::getExchangeName).collect(Collectors.toSet()));
            }
        });

    }

    public static List<BindingParameter> getProducerBindingParameter(String instanceSign) {
        return Collections.unmodifiableList(producerBindingParameters.get(instanceSign));
    }

    protected static boolean addConsumerBindingParameter(String instanceSign, List<ConsumerBindingParameter> bindingParameters) {
        checkConsumerBindingParameters(instanceSign, bindingParameters);
        return null != consumerBindingParameters.put(instanceSign, bindingParameters);
    }

    public static List<ConsumerBindingParameter> getConsumerBindingParameter(String instanceSign) {
        return Collections.unmodifiableList(consumerBindingParameters.get(instanceSign));
    }

    private static void checkConsumerBindingParameters(String instanceSign, List<ConsumerBindingParameter> bindingParameters) {
        bindingParameters.forEach(p -> {
            Assert.hasText(p.getListenerClassName(), "Listener class name must not be empty");
            Assert.hasText(p.getQueueName(), "Listener queue name must not be empty");
        });
        bindingParameters.forEach(p -> {
            if (null != declearedConsumerQueueNames.get(instanceSign)) {
                Set<String> queueNames = declearedConsumerQueueNames.get(instanceSign);
                if (queueNames.contains(p.getExchangeName())) {
                    throw new RabbitmqInstanceCreationException(String.format("Producer queue [%s] has been decleared!!!Please check your configuration", p.getQueueName()));
                } else {
                    queueNames.add(p.getExchangeName());
                }
            } else {
                declearedConsumerQueueNames.put(instanceSign, bindingParameters.stream().map(BindingParameter::getExchangeName).collect(Collectors.toSet()));
            }
        });
    }


}
