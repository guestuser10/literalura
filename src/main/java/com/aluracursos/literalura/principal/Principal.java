package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.model.DatosLibro;
import com.aluracursos.literalura.model.Libro;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {
    private LibroRepository librosRepositorio;
    private AutorRepository autorRepositorio;

    private Scanner scanner = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private String URL_BASE = "https://gutendex.com/books?search=";
    private ConvierteDatos conversor = new ConvierteDatos();

    public Principal(LibroRepository repository, AutorRepository autorRepositorio) {
        this.librosRepositorio = repository;
        this.autorRepositorio = autorRepositorio;
    }
    public void MuestraElMenu() {

        var opcion = -1;
        while (opcion!=0){
            var menu = """
                **************************
                1. REgistrar por título
                2. Listar libros registrados
                3. Listar autores registrados
                4. listar autores vivos en un año
                5. listar libros por idioma
                
                0. Salir
                """;
            System.out.println(menu);
            opcion = scanner.nextInt();
            scanner.nextLine();
            switch (opcion){
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    getLibrosRegistrados();
                    break;
                case 3:
                    getAutoresRegistrados();
                    break;
                case 4:
                    getAutoresVivosEnUnAno();
                    break;
                case 5:
                    getLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Hasta luego");
                    break;
                default:
                    System.out.println("Opción no válida");
                break;
            }
        }
    }
    private DatosLibro getDatosLibro(){
        System.out.println("Ingrese el título del libro a buscar");
        var titulo = scanner.nextLine();
        Optional<Libro> book = librosRepositorio.findByTituloContainsIgnoreCase(titulo);
        if (book.isPresent()){
            System.out.println("El libro ya está registrado");
            return null;
        }
        var json = consumoAPI.obtenerDatos(URL_BASE+titulo.replace(" ","%20"));
        var jsonObject = new JSONObject(json);
        var primerResultado = jsonObject.getJSONArray("results").getJSONObject(0);
        //convitiendo arrays en objetos
        var authorsArray = primerResultado.getJSONArray("authors");
        if (authorsArray.length() > 0) {
            var primerAutor = authorsArray.getJSONObject(0);
            primerResultado.put("authors", primerAutor);
        }
        var languagesArray = primerResultado.getJSONArray("languages");
        if (languagesArray.length() > 0) {
            var primerIdioma = languagesArray.getString(0);
            primerResultado.put("languages", primerIdioma);
        }
        //convitiendo json en objeto de tipo libro
        var raw = primerResultado.toString();
        DatosLibro datos = conversor.obtenerDatos(raw, DatosLibro.class);
        Libro libro = new Libro(datos);
        return datos;
    }
    private void buscarLibro() {
        var datos = getDatosLibro();
        if (datos == null) {
            return;
        }
        // Busca si el autor ya existe en la base de datos
        Autor autor = autorRepositorio.findByNombreContainsIgnoreCase(datos.autor().nombre())
                .orElseGet(() -> new Autor(datos.autor()));

        // Crea el libro y asocia el autor encontrado o creado
        Libro libro = new Libro(datos);
        libro.setAutor(autor);

        // Guarda el autor si es nuevo (sólo si no tiene ID)
        if (autor.getId() == null) {
            autorRepositorio.save(autor);
        }
        imprimirlibro(libro);

        // Guarda el libro
        librosRepositorio.save(libro);
    }

    private void imprimirlibro(Libro l){
        System.out.printf("----------LIBRO----------%n" +
                "Título: %s%n" +
                "Autor: %s%n" +
                "Idioma: %s%n"+
                "descargas: %d%n" +
                "------------------------%n",
                l.getTitulo(), l.getAutor().getNombre(), l.getIdioma(), l.getNumeroDeDescargas());
    }
    private void imprimirAutor(Autor a) {
        System.out.printf("----------AUTOR----------%n" +
                        "Nombre: %s%n" +
                        "Nacimiento: %s%n" +
                        "Muerte: %s%n" +
                        "------------------------%n",
                a.getNombre(), a.getFechaDeNacimiento(), a.getFechaDeFallecimiento());
    }
    private void getLibrosRegistrados () {
        List<Libro> libros = librosRepositorio.findAll();
        libros.forEach(this::imprimirlibro);
    }
    private void getAutoresRegistrados () {
         List<Autor> autores = autorRepositorio.findAll();
         autores.forEach(this::imprimirAutor);
    }
    private void getAutoresVivosEnUnAno() {
        System.out.println("Ingrese el año");
        var anio = scanner.nextInt();
        var autores = autorRepositorio.autoresVivosEnUnaFecha(anio);
        autores.forEach(this::imprimirAutor);
    }
    private void getLibrosPorIdioma(){
        var print = """
                escriba el idioma
                ********************************
                1 -> inglés
                2 -> español
                """;
        System.out.println(print);
        var idioma = scanner.nextLine();
        switch (idioma){
            case "1":
                idioma = "en";
                break;
            case "2":
                idioma = "es";
                break;
            default:
                idioma = "en";
                break;
        }
        var libros = librosRepositorio.findByIdioma(idioma);
        libros.forEach(this::imprimirlibro);
    }

}
