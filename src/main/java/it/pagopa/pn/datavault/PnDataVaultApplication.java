package it.pagopa.pn.datavault;

import it.pagopa.pn.commons.configs.PnSpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.RedirectView;

@PnSpringBootApplication
public class PnDataVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnDataVaultApplication.class, args);
	}


	@RestController
	@RequestMapping("/")
	public static class RootController {

		@GetMapping("/")
		public String home() {
			return "OK";
		}
	}

}
