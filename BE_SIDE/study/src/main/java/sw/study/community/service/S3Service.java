package sw.study.community.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sw.study.exception.s3.FileUploadException;
import sw.study.exception.s3.S3UploadException;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // S3에 이미지 업로드
    public String upload(MultipartFile file, String location) {
        String fileName = location + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드 요청 생성
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata);
            // S3에 파일 업로드
            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 중 오류가 발생했습니다.", e);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 403) throw new S3UploadException("S3 접근 권한이 없습니다.", e);
            if (e.getStatusCode() == 404) throw new S3UploadException("S3 버킷 또는 객체가 존재하지 않습니다.", e);
            throw new S3UploadException("S3 요청 중 알 수 없는 오류가 발생했습니다.", e);
        }

        return getPublicUrl(fileName);
    }

    private String getPublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, amazonS3.getRegionName(), fileName);
    }
}
