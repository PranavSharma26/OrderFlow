package com.orderflow.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JMb2NhbFRlc3RpbmcxMjM0NTY=",
		"jwt.expiration=3600000"
})
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
