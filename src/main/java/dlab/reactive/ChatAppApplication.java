package dlab.reactive;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Webflux Chat-App", version = "1.0", description = "Created 2024/04/24"))
public class ChatAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatAppApplication.class, args);
	}

}
