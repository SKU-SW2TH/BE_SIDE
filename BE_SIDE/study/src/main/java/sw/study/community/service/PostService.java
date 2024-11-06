package sw.study.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sw.study.community.domain.Category;
import sw.study.community.domain.Post;
import sw.study.community.domain.PostFile;
import sw.study.community.dto.PostDTO;
import sw.study.community.repository.CategoryRepository;
import sw.study.community.repository.PostRepository;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.AreaNotFoundException;
import sw.study.exception.community.CategoryNotFoundException;
import sw.study.user.domain.InterestArea;
import sw.study.user.domain.Member;
import sw.study.user.repository.InterestAreaRepository;
import sw.study.user.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final InterestAreaRepository interestAreaRepository;
    private final S3Service s3Service;

    /**
     * 게시글 생성
     */
    public Long save(PostDTO postDto) {
        Category category = categoryRepository.findByName(postDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("해당하는 카테고리가 존재하지 않습니다."));
        Member member = memberRepository.findById(postDto.getMemberId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자가 존재하지 않습니다."));

        List<InterestArea> interestAreas = new ArrayList<>();
        for (String areaName : postDto.getInterests()) {
            interestAreas.add(interestAreaRepository.findByAreaName(areaName)
                    .orElseThrow(() -> new AreaNotFoundException("해당하는 분야가 존재하지 않습니다.")));
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : postDto.getFiles()) {
            String url = s3Service.upload(file, "post/");
            urls.add(url);
        }

        Post post = Post.createPost(postDto.getTitle(), postDto.getContent(), category, member, interestAreas, urls);
        return postRepository.save(post).getId();

    }
}
