package sw.study.community.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sw.study.community.service.PostService;

@RestController
@RequestMapping("/api/community/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    
}
