package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true) //import-spring. 데이터 변경이 없는 조회(읽기)의 경우 (readOnly = true) 옵션
@RequiredArgsConstructor //fial의 생성자를 만들어줌
public class MemberService {

	private final MemberRepository memberRepository;
	
	//회원 가입
	@Transactional //데이터 변경이 필요하므로 옵션 빼고 넣기
	public Long join(Member member) { 
		
		validateDuplicateMember(member);
		memberRepository.save(member);
		return member.getId();
	}

	//중복 회원 검증
	private void validateDuplicateMember(Member member) {
		
		List<Member> findMembers = memberRepository.findByName(member.getName());
		if (!findMembers.isEmpty()) {
			throw new IllegalStateException("이미 존재하는 회원입니다.");
		}
	}
	
	//전체 회원 조회
	public List<Member> findMembers(){
		return memberRepository.findAll();
	}
	
	//한명 조회
	public Member findOne(Long memberId) {
		return memberRepository.findById(memberId).get();
	}
	
	//회원 수정(API)
	@Transactional
	public void update(Long id, String name) {
		Member member = memberRepository.findById(id).get(); //영속성 컨텍스트에서 가져와서
		member.setName(name); //업데이트
	}
	
}
