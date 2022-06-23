package it.pagopa.pn.datavault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PnDataVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnDataVaultApplication.class, args);
	}

	public PnDataVaultApplication(){
		// impostazione cache ttl
		java.security.Security.setProperty("networkaddress.cache.ttl" , "1");
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
