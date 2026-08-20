package be.kdg.keepdishesgoing.common.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * Listener side, deliberately without a JSON converter.
     *
     * <p>Everything on our queue comes from the delivery service, stamped with a
     * {@code __TypeId__} naming one of its own classes. Jackson reads that
     * header, fails to load a class that does not exist here, and rejects the
     * message before any listener runs. Handing the raw message over instead
     * lets the listener decide what it is from the routing key, which is the
     * part of the contract both sides agreed on.
     *
     * <p>Named {@code rabbitListenerContainerFactory} on purpose: that is the
     * bean {@code @RabbitListener} looks for. Under any other name Boot's own
     * factory is used instead, which quietly picks up the converter below.
     */
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        // Boot's own settings first, then the one thing we differ on.
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    /** Publishing side: our own events, serialised as JSON. */
    @Bean
    MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
