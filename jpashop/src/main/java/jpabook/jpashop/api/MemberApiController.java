package jpabook.jpashop.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

	private final MemberService memberService;
	
	//회원조회 API 1) 노출하고 싶지 않은 엔티티는 @JsonIgnore 그렇지만 역시 엔티티가 변경되면 API 스펙이 변경되므로 DTO 사용하기
	@GetMapping("/api/v1/members")
	public List<Member> membersV1(){
		return memberService.findMembers();
	}
	
	//회원조회 API 2)
	@GetMapping("/api/v2/members")
	public Result membersV2() {
		List<Member> findMembers = memberService.findMembers();
		List<MemberDto> collect = findMembers.stream().map(m -> new MemberDto(m.getName()))
													.collect(Collectors.toList());
		return new Result(collect.size(), collect);
	}
	
	@Data
	@AllArgsConstructor
	static class Result<T> { //조회할 값이 json[data:{...}]로 나가면 유연성이 떨어짐
		private int count;	 //요구사항이 추가되어도 OK. 유지보수성 높음
		private T data;
	}
	
	@Data
	@AllArgsConstructor
	static class MemberDto {
		private String name;
	}
	
	//회원등록 API 1) 엔티티 노출 X
	@PostMapping("/api/v1/members")
	public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}
	
	//회원등록 API 2) 엔티티가 변경이 되어도 API 스펙이 변경되지 않도록 DTO 사용하기
	@PostMapping("/api/v2/members")
	public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
		
		Member member = new Member();
		member.setName(request.getName());
		
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}
	
	//회원수정
	@PutMapping("/api/v2/members/{id}")
	public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, 
												@RequestBody @Valid UpdateMemberRequest request) {
		
		memberService.update(id, request.getName());
		Member findMember = memberService.findOne(id); //유지보수성 증대
		
		return new UpdateMemberResponse(findMember.getId(), findMember.getName());
	}
	
	@Data
	@AllArgsConstructor
	static class UpdateMemberResponse {
		private Long id;
		private String name;
	}
	
	@Data
	static class UpdateMemberRequest {
		private String name;
	}
	
	@Data
	static class CreateMemberRequest {
		private String name;
	}
	
	@Data
	static class CreateMemberResponse {
		private Long id;
		
		public CreateMemberResponse(Long id) {
			this.id = id;
		}
	}
}
