package at.technikum.genaiworker.messaging;

public record SummaryResultEvent(String documentId, String summary) {}