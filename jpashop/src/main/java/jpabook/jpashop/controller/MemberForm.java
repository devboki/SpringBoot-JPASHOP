package jpabook.jpashop.controller;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberForm {

	@NotEmpty(message = "회원 이름은 필수 입니다") //springboot 2.3.3 이상 : implementation 'org.springframework.boot:spring-boot-starter-validation'
	private String name;
	
	private String city;
	private String street;
	private String zipcode;
}
