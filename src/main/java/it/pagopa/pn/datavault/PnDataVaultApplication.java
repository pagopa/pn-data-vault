package it.pagopa.pn.datavault;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Slf4j
public class PnDataVaultApplication {

	public static void main(String[] args) {
		// impostazione cache ttl
		java.security.Security.setProperty("networkaddress.cache.ttl" , "1");
		java.security.Security.setProperty("networkaddress.cache.negative.ttl", "1");
		SpringApplication.run(PnDataVaultApplication.class, args);
		log.debug("set network cache");
	}

	@RestController
	@RequestMapping("/")
	public static class RootController {

		@GetMapping("/")
		public String home() {
			return "";
		}
	}
}
