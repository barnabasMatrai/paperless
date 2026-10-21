
package at.technikum.paperless.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadDocumentEvent {
    private String documentId;
}
