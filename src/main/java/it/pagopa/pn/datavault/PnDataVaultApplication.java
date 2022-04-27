package it.pagopa.pn.datavault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class PnDataVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnDataVaultApplication.class, args);
	}


	@Controller
	@RequestMapping("/")
	public static class RootController {

		@GetMapping("/")
		public String home() {
			return "OK";
		}
	}

}
