package at.technikum.ocrworker;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class OcrWorkerApplicationTests {

    @MockitoBean
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Test
    void contextLoads() {
    }
}