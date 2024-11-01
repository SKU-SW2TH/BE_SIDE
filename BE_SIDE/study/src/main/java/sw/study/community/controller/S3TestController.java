package sw.study.community.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sw.study.community.service.S3Service;
import sw.study.exception.s3.FileUploadException;
import sw.study.exception.s3.S3UploadException;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3TestController {

    private final S3Service s3Service;

    @PostMapping("/upload/{location}")
    public ResponseEntity<String> uploadFile(@PathVariable("location") String location, @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.upload(file, location + "/");
            return ResponseEntity.ok(imageUrl);

        } catch (FileUploadException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 업로드 실패: " + e.getMessage());

        } catch (S3UploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("S3 업로드 실패: " + e.getMessage());
        }
    }
}
