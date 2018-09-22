package io.durbs.face.processor.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MessageChannels {

    @Input(StreamConstants.INDEX_INPUT)
    MessageChannel indexInput();

    @Output(StreamConstants.SEARCH_INPUT)
    MessageChannel searchInput();

    @Output(StreamConstants.SEARCH_OUTPUT)
    MessageChannel searchOutput();

    @Output(StreamConstants.INDEX_ASQYNC_DLQ)
    MessageChannel indexAsyncDLQ();

    @Output(StreamConstants.SEARCH_ASYNC_DLQ)
    MessageChannel searchAsyncDLQ();
}
