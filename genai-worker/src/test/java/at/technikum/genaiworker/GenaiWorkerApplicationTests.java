package at.technikum.genaiworker;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class GenaiWorkerApplicationTests {

    @MockitoBean
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Test
    void contextLoads() {
    }
}