package jpabook.jpashop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jpabook.jpashop.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	List<Member> findByName(String name); 
	//select m from Member m where m.name = ? 메서드에 따른 

}
