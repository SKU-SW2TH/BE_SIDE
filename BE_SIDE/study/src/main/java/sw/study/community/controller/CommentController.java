package sw.study.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sw.study.community.service.CommentService;

@Slf4j
@RestController
@RequestMapping("/api/community/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
}
