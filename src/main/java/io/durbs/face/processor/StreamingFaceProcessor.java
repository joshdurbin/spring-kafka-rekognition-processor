package io.durbs.face.processor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionAsync;
import com.amazonaws.services.rekognition.AmazonRekognitionAsyncClientBuilder;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import io.durbs.face.processor.stream.MessageChannels;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableBinding(MessageChannels.class)
@EnableCaching
public class StreamingFaceProcessor {

    public static void main(String[] args) {
        SpringApplication.run(StreamingFaceProcessor.class, args);
    }

    @Bean
    AWSCredentialsProvider awsCredentialsProvider(@Value("${aws.auth.accessKey}") String accessKey,
                                                  @Value("${aws.auth.secretKey}") String secretKey) {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }

    @Bean
    @ConditionalOnProperty(name = "aws.async", havingValue = "false", matchIfMissing = true)
    AmazonRekognition amazonRekognition(AWSCredentialsProvider awsCredentialsProvider) {

        return AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(awsCredentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "aws.async", havingValue = "true")
    AmazonRekognitionAsync amazonRekognitionAsync(@Value("${aws.pool.workers}") Integer numberOfWorkers,
                                                  @Value("${aws.pool.maxWorkers}") Integer maxNumberOfWorkers,
                                                  @Value("${aws.pool.threadPoolKeepLiveTime}") Long threadPoolKeepAliveTime,
                                                  @Value("${aws.pool.queueSize}") Integer queueSize,
                                                  AWSCredentialsProvider awsCredentialsProvider) {

        return AmazonRekognitionAsyncClientBuilder
                .standard()
                .withExecutorFactory(() -> new ThreadPoolExecutor(numberOfWorkers,
                        maxNumberOfWorkers,
                        threadPoolKeepAliveTime,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(queueSize),
                        new ThreadPoolExecutor.CallerRunsPolicy()))
                .withRegion(Regions.US_EAST_1)
                .withCredentials(awsCredentialsProvider)
                .build();
    }
}
