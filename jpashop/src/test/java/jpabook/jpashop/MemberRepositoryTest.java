package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Member;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
class MemberRepositoryTest {
	
	/*
	@Autowired MemberRepositoryEX memberRepository;
	
	@Test
	@Transactional //javax 말고 springframework import
	@Rollback(false) //테스트 클래스는 자동으로 롤백되어 데이터가 남지 않는데, 데이터를 확인하고 싶다면 @Rollback(false) 어노테이션을 써주면 된다
	public void testMember() throws Exception {
		
		//given
		Member member = new Member();
		member.setName("memberA");
		
		//when
		Long saveId = memberRepository.save(member);
		Member findMember = memberRepository.find(saveId);
		
		//then
		Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
		Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());
		Assertions.assertThat(findMember).isEqualTo(member);
		System.out.println("findMember == member : " + (findMember == member));
		/* 같은 트랜잭션 안에서 저장하고 조회하면 같은 영속성 컨텍스트에 포함되기 때문에.
		 	그러므로, id가 같으면 같은 Entity로 식별.
		 	그러므로, findMember == member 는 true */
	}
