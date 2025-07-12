package com.orderagentservice.global.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment


@Configuration
class AwsS3Config @Autowired constructor(
    private val env: Environment
) {
    private val accessKey: String = env.getProperty("cloud.aws.credentials.access-key")!!
    private val secretKey: String = env.getProperty("cloud.aws.credentials.secret-key")!!
    private val region: String = env.getProperty("cloud.aws.credentials.region.static")!!

    @Bean
    fun amazonS3Client(): AmazonS3Client {
        val awsCredential = BasicAWSCredentials(accessKey, secretKey)
        return (AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(awsCredential))
            .build() as AmazonS3Client)
    }
}