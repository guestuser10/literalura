package com.aluracursos.literalura.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsumoAPI {
    public String obtenerDatos(String url) {
        // Configurar el cliente para seguir redirecciones automáticamente
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS) //seguir redirecciones
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("sec-ch-ua-platform", "Windows")
                .header("User-Agent", "JavaHttpClient/1.0") //establecer un User-Agent
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al realizar la solicitud HTTP", e);
        }

        // Verificar el código de respuesta
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.err.println("Error: Código de estado " + response.statusCode());
            return null;
        }
    }
}
