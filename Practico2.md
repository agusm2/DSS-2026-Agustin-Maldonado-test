# Ejercicio 1 - Inyección SQL (SQLi)

## Vulnerabilidad encontrada

La vulnerabilidad se encuentra en la linea 29 de la funcion "buscar_funciones", donde el valor ingresado por el usuario se incorpora dentro de la consulta SQL.

Esta vulnerabilidad tiene lugar ya que la entrada del usuario se inyecta en la consulta sin utilizar parametros; permitiendo ingresar caracteres con significado especial para SQL, modificando el comportamiento de la consulta.

## Prueba de concepto (PoC)

### Pasos para explotar vulnerabilidad
1. Ingresar '-- en el buscador
2. Ejecutar busqueda

### Payload utilizado

```SQL
'--
```

### Resultado de la explotación

Como resultado se obtuvieron todas las funciones de la base de datos.

## Mitigación

Para mitigar esta vulnerabilidad, se parametrizo la consulta SQL vulnerable. Se reemplazo la entrada directa del usuario por un placeholder (?) y posteriormente se paso ese valor como parametro para ejecutar la consulta.
De esta manera, SQLite interpreta la entrada del usuario como un dato y no como parte de la sintaxis SQL.

### Código vulnerable
```bash
def buscar_funciones(query, sort_by='nombre', sort_dir='ASC'):
    db = get_db()
    sql = f"SELECT peliculas.nombre as pelicula, funciones.fecha_hora, " \
          f"(funciones.asientos_totales - funciones.asientos_ocupados) as disponibles " \
          f"FROM funciones " \
          f"JOIN peliculas ON funciones.pelicula_id = peliculas.id " \
          f"WHERE peliculas.nombre LIKE '%{query}%' " \
          f"ORDER BY {'peliculas.nombre' if sort_by == 'nombre' else 'funciones.fecha_hora'} " \
          f"{sort_dir}"
    return db.execute(sql).fetchall()
```


### Código mitigado
```bash
def buscar_funciones(query, sort_by='nombre', sort_dir='ASC'):
    db = get_db()
    sql = f"SELECT peliculas.nombre as pelicula, funciones.fecha_hora, " \
          f"(funciones.asientos_totales - funciones.asientos_ocupados) as disponibles " \
          f"FROM funciones " \
          f"JOIN peliculas ON funciones.pelicula_id = peliculas.id " \
          f"WHERE peliculas.nombre LIKE ? " \
          f"ORDER BY {'peliculas.nombre' if sort_by == 'nombre' else 'funciones.fecha_hora'} " \
          f"{sort_dir}"
    return db.execute(sql, (f'%{query}%',)).fetchall()
```

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload; no se obtuvo ninguna funcion de todas las obtenidas previamente.

El ataque ya no funciona porque la consulta está parametrizada. De esta forma, la entrada del usuario es tratada como un dato y no como parte de la sintaxis SQL, evitando que pueda modificar el comportamiento de la consulta.

---
# Ejercicio 2 - Cross Site Scripting (XSS)

## Vulnerabilidad encontrada

La vulnerabilidad se encuentra en el archivo edit.html, especificamente en la línea 86. 
Al implementar el filtro safe de Jinja en el campo de descripción, no se aplica el autoescapado sobre ese valor; permitiendo que un atacante pueda ingresar código HTML que va a ser renderizado posteriormente.


## Prueba de concepto (PoC)

### Pasos para explotar vulnerabilidad
1. Buscar una película, por ejemplo "Dune"
2. En el apartado de "Descripción", clickear "Editar"
3. Ingresar payload (código HTML) en la sección de descripción
4. Hacer click en "Guardar cambios"
5. Buscar nuevamente la película editada
6. Pasar el cursor sobre el texto ingresado

### Payload utilizado

```html
<b onmouseover=alert("Hola!")>Esto tendría que describir a la película.</b>
```

### Resultado de la explotación

Como resultado, cada vez que el cursor pasa por encima del texto en la descripción, la página ejecuta código JavaScript, emitiendo un cartel con la alerta ingresada previamente en la misma descripción.

## Mitigación

Para mitigar esta vulnerabilidad, podemos eliminar el filtro de "safe" dentro del campo de descripción; de esta forma, Jinja aplica el mecanismo de autoescaping en esta sección, por lo que el código ingresado no se va a renderizar, sino que se va a mostrar como si fuera texto plano.

### Código vulnerable
```html
<label for="descripcion">Descripción</label>
{% if pelicula['descripcion'] %}
    <div class="prev-descripcion">
        <strong>Descripción actual:</strong><br>
        {{ pelicula['descripcion'] | safe }}
    </div>
{% endif %}
<textarea id="descripcion" name="descripcion">{{ pelicula['descripcion'] }}</textarea>
```

### Código mitigado
```html
<label for="descripcion">Descripción</label>
{% if pelicula['descripcion'] %}
    <div class="prev-descripcion">
        <strong>Descripción actual:</strong><br>
        {{ pelicula['descripcion'] }}
    </div>
{% endif %}
<textarea id="descripcion" name="descripcion">{{ pelicula['descripcion'] }}</textarea>
```

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload; ahora el mismo aparece como texto plano y no figura ninguna alerta, evitando la ejecución de código JavaScript.

El ataque ya no funciona por el hecho de que se eliminó el filtro "safe" en el campo de Descripción, de esta manera, Jinja no asume que el código es seguro y aplica autoescaping, escapando los caracteres especiales utilizados en HTML, como '<' y '>' en este caso.

---
# Ejercicio 3 - File Upload

## Vulnerabilidad encontrada

La vulnerabilidad se encuentra en los archivos upload.html y PeliculaController.java.
Por el lado de upload.html, se puede detectar que la vulnerabilidad se encuentra dentro del form que sube el afiche a la aplicación en la línea 43; específicamente a la hora de permitir cualquier tipo de archivo como entrada.
En cuanto a PeliculaController, podemos detectar la vulnerabilidad dentro del POST con la ruta "/upload/{id}" en la línea 88; específicamente cuando la aplicación toma el nombre del archivo y no aplica ninguna validación, simplemente se asegura de que el mismo exista.

## Prueba de concepto (PoC)

### Pasos para explotar vulnerabilidad
1. Crear archivo .html con una alerta dentro
2. Buscar película
3. Ingresar a "Subir afiche"
4. Seleccionar archivo .html creado previamente
5. Hacer click en "Subir afiche"
6. Ingresar a la siguiente ruta: "http://127.0.0.1:8080/uploads/nombreArchivo.html"

### Payload utilizado
afiche.html
```html
<script>alert("Aca no esta el afiche")</script>
```
### Resultado de la explotación

Como resultado, al ingresar a "http://127.0.0.1:8080/uploads/afiche.html" nos salta la alerta mencionada en el payload.

## Mitigación

En el archivo de upload.html, se modificó el campo de accept en el form que carga el afiche, de esta manera, solamente permite archivos de tipo imagen como pueden ser .png, .jpg y .jpeg.
Por otra parte, en PeliculaController.java, se introducieron diversas validaciones:
- Se creó una allowlist con extensiones permitidas, siendo las mismas que las introducidas en upload.html
- Se validó el tipo MIME del archivo para comprobar que corresponda a un formato de imagen permitido
- Se verificó que el contenido real del archivo pueda ser interpretado como una imagen válida, evitando confiar únicamente en su nombre o extensión
- Se generó un nombre de forma aleatoria, para evitar riesgos asociados a nombres manipulados.

### Código vulnerable
- upload.html
```html
<div class="upload-form">
    <form method="post"
          th:action="@{/upload/{id}(id=${pelicula.id})}" enctype="multipart/form-data">
        <label for="afiche">Seleccionar archivo:</label>
        <input type="file" name="afiche" id="afiche" accept="*/*" required>
        <br><br>
        <button type="submit">Subir afiche</button>
    </form>

    <a href="/" class="back-link">&#8592; Volver al buscador</a>
</div>
```

- PeliculaController.java
```java
    @PostMapping("/upload/{id}")
    public String uploadFile(@PathVariable Integer id,
                             @RequestParam("afiche") MultipartFile archivo) throws IOException {
        Pelicula pelicula = peliculaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pelicula no encontrada"));

        String filename = archivo.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.copy(archivo.getInputStream(), uploadPath.resolve(filename));

        pelicula.setAfichePath(filename);
        peliculaRepo.save(pelicula);

        return "redirect:/";
    }
```

### Código mitigado
- upload.html
```html
<div class="upload-form">
    <form method="post"
          th:action="@{/upload/{id}(id=${pelicula.id})}" enctype="multipart/form-data">
        <label for="afiche">Seleccionar archivo:</label>
        <input type="file" name="afiche" id="afiche" accept=".png, .jpg, .jpeg" required>
        <br><br>
        <button type="submit">Subir afiche</button>
    </form>

    <a href="/" class="back-link">&#8592; Volver al buscador</a>
</div>
```

- PeliculaController.java
```java
    private boolean extensionPermitida(String filename) {
        if(filename == null || filename.isBlank()) {
            return false;
        }

        String[] extensionesPermitidas = {".jpg", ".jpeg", ".png"};
        for (String ext : extensionesPermitidas) {
            if (filename.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean mimePermitido(MultipartFile archivo) {
        String contentType = archivo.getContentType();

        if(contentType == null) {
            return false;
        }

        String[] mimesPermitidos = {"image/jpeg", "image/png"};
        for (String mime : mimesPermitidos) {
            if (contentType.equalsIgnoreCase(mime)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean contenidoImagenValido(MultipartFile archivo) throws IOException {
        try (ImageInputStream imageStream = 
                ImageIO.createImageInputStream(archivo.getInputStream())) {
            
            if(imageStream == null) {
                return false;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);

            if(!readers.hasNext()) {
                return false;
            }

            ImageReader reader = readers.next();

            try{
                // Configura el ImageReader para leer la imagen
                reader.setInput(imageStream);
                reader.read(0); // Intenta leer la primera imagen
                return true; // Si no lanza excepción, es una imagen válida
            } catch (IOException e) {
                return false; // No es una imagen válida
            } finally {
                reader.dispose();
            }
        }
    }

    private String generarNombreSeguro(String filename){
        int ultimoPunto = filename.lastIndexOf('.');

        String extension = filename.substring(ultimoPunto).toLowerCase(Locale.ROOT);

        return UUID.randomUUID().toString() + extension;
    }

    @PostMapping("/upload/{id}")
    public String uploadFile(@PathVariable Integer id,
                             @RequestParam("afiche") MultipartFile archivo) throws IOException {
        Pelicula pelicula = peliculaRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pelicula no encontrada"));

        String filename = archivo.getOriginalFilename();

        if(!extensionPermitida(filename)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida");
        }

        if(!mimePermitido(archivo)) {
            throw new IllegalArgumentException("Tipo MIME no permitido");
        }

        if(!contenidoImagenValido(archivo)) {
            throw new IllegalArgumentException("Contenido del archivo no es una imagen válida");
        }

        String filenameSeguro = generarNombreSeguro(filename);

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.copy(archivo.getInputStream(), uploadPath.resolve(filenameSeguro));

        pelicula.setAfichePath(filenameSeguro);
        peliculaRepo.save(pelicula);

        return "redirect:/";
    }
```

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload.

El ataque ya no funciona ya que por parte del frontend no se aceptan archivos que no correspondan a un tipo de imagen. Además, en caso de que se consiga cargar una imagen corrupta, el backend se encarga de validar que la imagen subida pase las diferentes validaciones mencionadas anteriormente.

---
# Ejercicio 4 - Server Side Template Injection

## Vulnerabilidad encontrada

La vulnerabilidad se encuentra dentro de la búsqueda de FuncionController.java. El input ingresado por el usuario es enviado a spelEval.evaluate(buscar); dentro de SpelEvaluator.java, el valor es interpretado como una expresión SpEL en lugar de ser interpretado como texto.

## Prueba de concepto (PoC)

### Pasos para explotar vulnerabilidad
1. Ingresar el payload en el buscador
2. Hacer click en "Buscar"
3. Observar "Resultados buscando por:"

### Payload utilizado

```md
7*7
```

### Resultado de la explotación

Al ingresar 7*7 el servidor interpreta la entrada como una expresión y devuelve 49 en lugar de tratarlo como un string; confirmando que el input está siendo evaluado en el servidor.

## Mitigación

Se modificó FuncionController.java para que por parte del servidor no se evalúe el input del usuario, sino que solamente se interprete como texto.

### Código vulnerable
```java
@GetMapping("/")
public String search(@RequestParam(required = false) String buscar, Model model) {

    model.addAttribute("query", buscar != null ? buscar : "");

    if (buscar == null || buscar.isBlank()) {
        System.out.println("buscar es: " + buscar);
        // Mostrar todas las funciones si no hay busqueda
        List<Funcion> todas = funcionRepo.findAll();
        model.addAttribute("resultados", todas);
        model.addAttribute("mensaje", "Mostrando todas las funciones.");
        return "index";
    }

    String spelResultado = spelEval.evaluate(buscar);
```

### Código mitigado
```java
@Controller
public class FuncionController {

    private final FuncionRepository funcionRepo;

    @Autowired
    public FuncionController(FuncionRepository funcionRepo) {
        this.funcionRepo = funcionRepo;
    }

    @GetMapping("/")
    public String search(@RequestParam(required = false) String buscar, Model model) {

        model.addAttribute("query", buscar != null ? buscar : "");

        if (buscar == null || buscar.isBlank()) {
            List<Funcion> todas = funcionRepo.findAll();

            model.addAttribute("resultados", todas);
            model.addAttribute("mensaje", "Mostrando todas las funciones.");

            return "index";
        }

        List<Funcion> resultados = funcionRepo.findAll().stream()
            .filter(f -> f.getNombreFuncion() != null &&
                         f.getNombreFuncion().toLowerCase().contains(buscar.toLowerCase()))
            .collect(Collectors.toList());

            if(!resultados.isEmpty()) {
                model.addAttribute("resultados", resultados);
                model.addAttribute("mensaje", "Resultados buscando por: " + buscar);
            } else {
                model.addAttribute("resultados", new ArrayList<Funcion>());
                model.addAttribute("mensaje", "No se encontraron coincidencias.");
            }

        return "index";
    }
}
```

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload.

Ahora, al momento de la búsqueda e interpretar al parámetro de buscar solamente como texto, la aplicación muestra literalmente el resultado de buscar 7*7, mostrando como resultado que no hay coincidencias; por lo que la entrada del usuario ya no se toma como una expresión a evaluar por el servidor.

---
# Ejercicio 5 - Almacenamiento inseguro

## Vulnerabilidad encontrada

La vulnerabilidad se da debido a que la aplicación guarda las contraseñas con un cifrado reversible; por lo que cualquier atacante con acceso al código fuente puede acceder a las contraseñas cifradas mediante decrypt().
Podemos ver esto específicamente en las líneas 36 y 68 de AuthController.java; además de en la línea 22 de EncryptionService.java

## Prueba de concepto (PoC)

### Pasos para explotar vulnerabilidad
1. Registrar un usuario con contraseña conocida
2. Obtener el valor de cifrado mostrado por la aplicación
3. Localizar clave de cifrado
4. Obtener clave efectiva utilizada por AES
5. Descifrar contraseña con OpenSSL

### Payload utilizado
```bash
printf '%s' 'xwJeAskCpxdDfGLFoCrAEQ==' \
| openssl enc -aes-256-ecb -d -a -A \
-K 4D7953757033725333637233744B3379213230323443696E654275736361646F
```

### Resultado de la explotación

Como resultado de la explotación obtenemos la contraseña conocida registrada por el usuario.

## Mitigación

Se dejó de utilizar la clase de EncryptionService.java para cifrar las contraseñas y se reemplazó el cifrado AES por hashing mediante BCrypt.
Además, en AuthController.java, se compara la contraseña ingresada con el hash almacenado.
También se agregó un encoder para que no sea necesario descifrar ni recuperar la contraseña original.


### Código vulnerable
- AuthController.java
```java
String decryptedPassword = EncryptionService.decrypt(user.getPassword());
```

```java
nuevoUsuario.setPassword(EncryptionService.encrypt(password));
```

- EncryptionService.java
```java
private static final String SECRET_KEY = "MySup3rS3cr3tK3y!2024CineBuscadorAES";
```

### Código mitigado
Se dejó de utilizar la clase EncryptionService.java

```java
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==================== LOGIN PAGE ====================
    @GetMapping("/")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("registerForm", new RegisterForm());
        return "index";
    }

    // ==================== LOGIN ====================
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            //Descifrar la contraseña almacenada y comparar con la ingresada
            //String decryptedPassword = EncryptionService.decrypt(user.getPassword());
            if (passwordEncoder.matches(password, user.getPassword())) {
                model.addAttribute("loginSuccess", true);
                model.addAttribute("welcomeUser", username);
                addForms(model);
                return "index";
            }
        }
        model.addAttribute("loginError", "Usuario o contraseña incorrecta");
        addForms(model);
        return "index";
    }

    // ==================== REGISTER ====================
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                           @RequestParam String confirmPwd, Model model) {
        if (!password.equals(confirmPwd)) {
            model.addAttribute("registerError", "Las contraseñas no coinciden");
            addForms(model);
            return "index";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("registerError", "El usuario ya existe");
            addForms(model);
            return "index";
        }

        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));

        userRepository.save(nuevoUsuario);

        model.addAttribute("registerSuccess", true);
        model.addAttribute("registeredUsername", username);

        addForms(model);
        return "index";
    }
}
```

## Verificación de la mitigación

Se registró nuevamente un usuario y se verificó que la contraseña almacenada corresponde a un hash BCrypt.
También se probó que el inicio de sesión continúa funcionando mediante passwordEncoder.matches().

---
## Referencias

### Ejercicio 1
- https://owasp-uruguay.github.io/sqli-en-la-practica/
- https://portswigger.net/web-security/learning-paths/sql-injection
- https://bobby-tables.com/python 

### Ejercicio 2
- https://owasp.org/www-community/attacks/xss/
- https://flask.palletsprojects.com/es/stable/templating/

### Ejercicio 3
- https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html
- https://cwe.mitre.org/data/definitions/434.html
- https://developer.mozilla.org/es/docs/Web/HTML/Reference/Attributes/accept

### Ejercicio 4
- https://cwe.mitre.org/data/definitions/917.html
- https://portswigger.net/web-security/server-side-template-injection

### Ejercicio 5
- https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
