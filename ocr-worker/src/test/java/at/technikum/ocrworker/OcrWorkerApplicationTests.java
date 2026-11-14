package at.technikum.ocrworker;

import at.technikum.ocrworker.search.OcrDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class OcrWorkerApplicationTests {

    @MockitoBean
    private OcrDocumentRepository ocrDocumentRepository;

    @Test
    void contextLoads() {
    }
}