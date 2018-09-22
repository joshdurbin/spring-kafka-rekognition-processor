package io.durbs.face.processor.stream;

public final class StreamConstants {

    private StreamConstants() {

    }

    public static final String INDEX_INPUT = "indexFaceInput";
    public static final String SEARCH_INPUT = "searchFaceInput";
    public static final String SEARCH_OUTPUT = "searchFaceOutput";

    public static final String INDEX_ASQYNC_DLQ = "indexAsyncDeadLetterQueue";
    public static final String SEARCH_ASYNC_DLQ = "searchAsyncDeadLetterQueue";
}
