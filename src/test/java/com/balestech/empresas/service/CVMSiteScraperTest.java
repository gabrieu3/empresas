package com.balestech.empresas.service;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class CVMSiteScraperTest {

    @Test
    public void testGerouArquivo() {
        CVMSiteScraper scraper = new CVMSiteScraper();
        scraper.run();
        // Verificar se o arquivo CSV foi gerado
        assertTrue("Arquivo CSV gerado", new File("DadosCVM.csv").exists());
    }

    @Test
    public void testRun() {
        CVMSiteScraper scraper = new CVMSiteScraper();
        scraper.run();

        // Carregar os arquivos pré-gerado e gerado pelo scraper
        byte[] expectedBytes = loadFileBytesClassPath("ExpectedDadosCVM.csv");
        byte[] generatedBytes = loadFileBytes("DadosCVM.csv");

        // Verificar se o arquivo gerado é igual ao arquivo pré-gerado
        assertArrayEquals("Arquivo CSV gerado igual ao arquivo pré-gerado", expectedBytes, generatedBytes);
    }

    private byte[] loadFileBytes(String fileName) {
        try {
            return Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] loadFileBytesClassPath(String fileName) {
        try {
            // Carregar o arquivo do classpath
            return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
