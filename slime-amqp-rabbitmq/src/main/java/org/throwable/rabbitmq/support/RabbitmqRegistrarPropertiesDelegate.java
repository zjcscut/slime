package org.throwable.rabbitmq.support;

import lombok.extern.slf4j.Slf4j;
import org.throwable.rabbitmq.configuration.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 17:05
 */
@Slf4j
public class RabbitmqRegistrarPropertiesDelegate extends RabbitmqRegistrarPropertiesManager {

    private static final Map<Object, String> factoryNames = new ConcurrentHashMap<>();

    public static void parseSlimeRabbitmqProperties(SlimeRabbitmqProperties sp) {
        List<RabbitmqProducerProperties> producers = sp.getProducers();
        if (null == producers || producers.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("Slime rabbitmq configuration of producers is empty");
            }
        } else {
            parseProducerProperties(producers);
        }
        List<RabbitmqConsumerProperties> consumers = sp.getConsumers();
        if (null == consumers || consumers.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("Slime rabbitmq configuration of consumers is empty");
            }
        } else {
            parseConsumerProperties(consumers);
        }
        mergeProducerAndConsumerInstances();
        mergetProducerAndConsumerBindingParameters();
    }

    private static void parseConsumerProperties(List<RabbitmqConsumerProperties> consumers) {
        for (RabbitmqConsumerProperties bcp : consumers) {
            InstanceHolder<RabbitmqConsumerInstanceProperties> instanceHolder = new InstanceHolder<>();
            instanceHolder.setinstanceSignature(bcp.getinstanceSignature());
            instanceHolder.setInstance(bcp);
            addConsumerInstance(bcp.getinstanceSignature(), instanceHolder);
            addConsumerBindingParameters(bcp.getinstanceSignature(), bcp.getConsumerBindingParameters());
        }
    }

    private static void parseProducerProperties(List<RabbitmqProducerProperties> producers) {
        for (RabbitmqProducerProperties bpp : producers) {
            InstanceHolder<RabbitmqProducerInstanceProperties> instanceHolder = new InstanceHolder<>();
            instanceHolder.setinstanceSignature(bpp.getinstanceSignature());
            instanceHolder.setInstance(bpp);
            addProducerInstance(bpp.getinstanceSignature(), instanceHolder);
            addProducerBindingParameters(bpp.getinstanceSignature(), bpp.getBindingParameters());
        }
    }

    public static void cacheRegisteredConnectionFactory(Object key, String name) {
        factoryNames.put(key, name);
    }

    public static Map<Object, String> getConnectionFactoryNames() {
        return Collections.unmodifiableMap(factoryNames);
    }

}
