package at.technikum.genaiworker.messaging;

public record OcrDocumentEvent(String documentId, String content) {}