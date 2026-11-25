package at.technikum.paperless;

import at.technikum.paperless.search.OcrDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PaperlessApplicationTests {

    @MockitoBean
    private OcrDocumentRepository ocrDocumentRepository;

    @MockitoBean
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Test
    void contextLoads() {
    }

}
