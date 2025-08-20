package com.brendanavarro.bookcatalog;

import com.brendanavarro.bookcatalog.console.ConsoleMenu;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * App de consola: muestra menú e interactúa con la persona usuaria.
 * Perfil activo "api" -> sin servidor web ni DataSource.
 */
@SpringBootApplication
public class BookcatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookcatalogApplication.class, args);
	}

	@Bean
	CommandLineRunner runMenu(ConsoleMenu menu) {
		return args -> menu.start();
	}
}
