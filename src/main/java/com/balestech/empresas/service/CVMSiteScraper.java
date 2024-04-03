package com.balestech.empresas.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CVMSiteScraper {

    public void run() {
        // Defina a URL base
        String baseUrl = "https://cvmweb.cvm.gov.br/SWB/Sistemas/SCW/CPublica/CiaAb/FormBuscaCiaAbOrdAlf.aspx?LetraInicial=";

        // Defina as letras e números a percorrer
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        // Crie um arquivo CSV
        String csvFileName = "DadosCVM.csv";
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(csvFileName)));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(csvFileName))) {

            // Escreva o cabeçalho no arquivo CSV
            bufferedWriter.write("CNPJ;Nome;Tipo de Participante;Código CVM;Data;Status");
            bufferedWriter.newLine();

            // Percorra todas as letras e números
            for (int i = 0; i < letras.length(); i++) {
                // Construa a URL com a letra ou número atual
                String url = baseUrl + letras.charAt(i);

                // Faça a solicitação HTTP e leia o conteúdo da página
                String content = getContentFromUrl(url);

                // Analise o HTML para extrair os dados da tabela
                extractAndWriteData(content, bufferedWriter);

                // Aguarde um segundo antes de fazer a próxima solicitação para evitar sobrecarga do servidor
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*private String getContentFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Defina a codificação de caracteres correta
        connection.setRequestProperty("Accept-Charset", "UTF-8");

        // Ler o conteúdo da página
        try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }*/

    public String getContentFromUrl(String urlString) throws IOException {
        String resultContent = null;
        HttpGet httpGet = new HttpGet(urlString);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                // Get status code
                HttpEntity entity = response.getEntity();
                // Get response information
                resultContent = EntityUtils.toString(entity);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultContent;
    }

    private void extractAndWriteData(String content, BufferedWriter writer) {
        Pattern tablePattern = Pattern.compile("<table id=\"dlCiasCdCVM\" class=\"BodyPP\"[^>]*>([\\s\\S]*?)</table>");
        Pattern rowPattern = Pattern.compile("<tr[^>]*>([\\s\\S]*?)</tr>");
        Pattern cellPattern = Pattern.compile("<td[^>]*>([\\s\\S]*?)</td>");

        Matcher tableMatcher = tablePattern.matcher(content);
        if (tableMatcher.find()) {
            String tableHtml = tableMatcher.group(0);
            Matcher rowMatcher = rowPattern.matcher(tableHtml);
            int countLinha = 0;
            while (rowMatcher.find()) {
                countLinha++;
                String rowData = rowMatcher.group(1);
                Matcher cellMatcher = cellPattern.matcher(rowData);
                StringBuilder line = new StringBuilder();
                while (cellMatcher.find()) {
                    String cellData = cellMatcher.group(1).trim().replaceAll("<[^>]+>", "");
                    line.append(cellData).append(";");
                }
                if (line.length() > 0 && countLinha > 1) {

                    String[] parts = line.toString().split(";");
                    if (parts.length == 5) {
                        String[] statusData = parts[4].split(" em ");
                        String status = statusData[0];
                        String data = statusData.length > 1 ? statusData[1] : "";
                        try {
                            writer.write(parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + data + ";" + status);
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
