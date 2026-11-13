package com.mysite.sbb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// BaseEntity 의 @CreatedDate / @LastModifiedDate 를 동작시킨다
// modifyOnCreate = false : 처음 저장할 때는 수정일시를 비워 둔다 (기본값 true 면 생성 시에도 수정일시가 채워짐)
@EnableJpaAuditing(modifyOnCreate = false)
public class SbbApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbbApplication.class, args);
	}

}
