package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    // 클래스 이름에 커맨드 + 시프트 + T 하면 자동 테스트 클래스 생성

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 중복 회원 검증
     */
    private void validateDuplicateMember(Member member)  {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     */
    public Optional<Member> findOne(Long memberId) {
        Optional<Member> byId = memberRepository.findById(memberId);
        return byId;
    }

    @Transactional
    public void update(Long id, String name) {
        Optional<Member> member = memberRepository.findById(id);
        //member.setName(name);
    }
}