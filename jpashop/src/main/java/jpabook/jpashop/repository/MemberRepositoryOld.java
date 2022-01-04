package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

//자동으로 spring bean으로 등록해주는 어노테이션
@Repository
@RequiredArgsConstructor
public class MemberRepositoryOld {

	private final EntityManager em;
	
	public void save(Member member) { 
		em.persist(member);
	} 
	
	public Member findOne(Long id) {
		return em.find(Member.class, id);
	}
	
	public List<Member> findAll(){
		return em.createQuery("select m from Member m", Member.class) //JPQL : Entity 객체를 대상으로 처리
					.getResultList();
	}
	
	public List<Member> findByName(String name){
		return em.createQuery("select m from Member m where m.name = :name", Member.class)
				.setParameter("name", name)
				.getResultList();
	}
}
