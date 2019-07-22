package com.lc.activiti;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * Minio java jdk test.
 *
 * @author zyz.
 */
public class MinioJdkTest {

    private static final Logger logger = LoggerFactory.getLogger(MinioJdkTest.class);

    // minio server url地址。
    private static final String endpoint = "http://39.96.54.109:9000";

    // minio server 访问key。
    private static final String accessKey = "AKIAIOSFODNN7EXAMPLE";

    // minio server 秘钥key。
    private static final String secretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    // minio server bucket.
    private static final String bucket = "countriesphotos";

    // minio server bucket test.
    private static final String bucketTestAdd = "test-bucket-folder";

    /**
     * Function: File upload.
     *
     * @throws Exception
     */
    @Test
    public void testFileUploader() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        // 检查存储桶是否已经存在。
        boolean exists = minioClient.bucketExists(bucketTestAdd);

        if (exists) {
            logger.info("Bucket already exists");
        } else {
            minioClient.makeBucket(bucketTestAdd);
        }

        // 使用putObject上传一个文件到存储桶中。
        minioClient.putObject(bucketTestAdd, "20190721/screen-0001.png",
                "/Users/zhuyangze/Documents/fork/activiti-spring-boot2-explorer/activiti-multi-instance/src/test/resources/images/screen-01.png");

        logger.info("images/screen-01.png is successfully uploaded as screen-0001.png to minio bucket");

    }

    /**
     * Function: bucket name.
     *
     * @throws Exception
     */
    @Test
    public void testListBuckets() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        List<Bucket> buckets = minioClient.listBuckets();

        for (Bucket bucket1 : buckets) {
            logger.info("bucket create time = {}, bucket name = {}", bucket1.creationDate(), bucket1.name());
        }

    }

    /**
     * Funtion: remove bucket.
     *
     * @throws Exception
     */
    @Test
    public void testRemoveBucket() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        boolean bucketExists = minioClient.bucketExists(bucketTestAdd);

        if (bucketExists) {
            // bucket里面为空可以删除，不为空会出错。
            minioClient.removeBucket(bucketTestAdd);
            logger.info("bucket remove successfully.");
        } else {
            logger.info("bucket does not exists.");
        }

    }

    /**
     * Function: bucket object listObjects.
     */
    @Test
    public void testListObjects() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        boolean bucketExists = minioClient.bucketExists(bucketTestAdd);

        if (bucketExists) {
            Iterable<Result<Item>> results = minioClient.listObjects(bucketTestAdd);
            for (Result<Item> result : results) {
                Item item = result.get();
                logger.info("item lastmodified = {}, item size = {}, item name = {}", item.lastModified(), item.size(), item.objectName());
            }
        } else {
            logger.info("bucket does not exists!");
        }
    }

    /**
     * Function: bucket object listObjects with prefix.
     */
    @Test
    public void testListObjectsPrefix() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        boolean bucketExists = minioClient.bucketExists(bucketTestAdd);

        if (bucketExists) {
            Iterable<Result<Item>> results = minioClient.listObjects(bucketTestAdd, "20190721/Desktop");
            for (Result<Item> result : results) {
                Item item = result.get();
                logger.info("item lastmodified = {}, item size = {}, item name = {}", item.lastModified(), item.size(), item.objectName());
            }
        } else {
            logger.info("bucket does not exists!");
        }
    }


    /**
     * Function: bucket object listObjects with prefix and recursive.
     * { 递归查询。 }
     */
    @Test
    public void testListObjectsPrefixRecursive() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        boolean bucketExists = minioClient.bucketExists(bucketTestAdd);

        if (bucketExists) {
            Iterable<Result<Item>> results = minioClient.listObjects(bucketTestAdd, "20190721", true);
            for (Result<Item> result : results) {
                Item item = result.get();
                logger.info("item lastmodified = {}, item size = {}, item name = {}", item.lastModified(), item.size(), item.objectName());
            }
        } else {
            logger.info("bucket does not exists!");
        }
    }

    /**
     * Function: getObject
     *
     * @throws Exception
     */
    @Test
    public void testGetObject() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        minioClient.statObject(bucketTestAdd, "20190721/Desktop-screenshot3.png");

        InputStream stream = minioClient.getObject(bucketTestAdd, "20190721/Desktop-screenshot3.png");

        byte[] buf = new byte[16384];
        int bytesRead;

        while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
            logger.info("bytesRead = {}", new String(buf, 0, bytesRead));
        }

        stream.close();

    }

    @Test
    public void testGetObjectUrl() throws Exception {

        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

        minioClient.statObject(bucketTestAdd, "20190721/Desktop-screenshot3.png");

        String objectUrl = minioClient.getObjectUrl(bucketTestAdd, "20190721/Desktop-screenshot3.png");
        logger.info("object url = {}", objectUrl);
    }



}
