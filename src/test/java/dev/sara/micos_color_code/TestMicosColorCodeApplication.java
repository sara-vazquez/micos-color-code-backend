package dev.sara.micos_color_code;

import org.springframework.boot.SpringApplication;

public class TestMicosColorCodeApplication {

	public static void main(String[] args) {
		SpringApplication.from(MicosColorCodeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
