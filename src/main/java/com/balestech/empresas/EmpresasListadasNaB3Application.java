package com.balestech.empresas;

import com.balestech.empresas.service.CVMSiteScraper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmpresasListadasNaB3Application {

	public static void main(String[] args) {
		SpringApplication.run(EmpresasListadasNaB3Application.class, args);
		(new CVMSiteScraper()).run();
	}

}
