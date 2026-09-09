# Ejercicio 1 - Inyección SQL (SQLi)

## Vulnerabilidad encontrada

La vulnerabilidad se encuentra en la linea 29 de la funcion "buscar_funciones", donde el valor ingresado por el usuario se incorpora dentro de la consulta SQL.

Esta vulnerabilidad tiene lugar ya que la entrada del usuario se inyecta en la consulta sin utilizar parametros; permitiendo ingresar caracteres con significado especial para SQL, modificando el comportamiento de la consulta.

## Prueba de concepto (PoC)

### Pasos para explotar vulnerabilidad
1. Ingresar '-- en el buscador
2. Ejecutar busqueda

### Payload utilizado

```bash
'--
```

### Resultado de la explotación

Como resultado se obtuvieron todas las funciones de la base de datos.

## Mitigación

Para mitigar esta vulnerabilidad, se parametrizo la consulta SQL vulnerable. Se reemplazo la entrada directa del usuario por un placeholder (?) y posteriormente se paso ese valor como parametro para ejecutar la consulta.
De esta manera, SQLite interpreta la entrada del usuario como un dato y no como parte del sintaxis SQL.

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

El ataque ya no funciona por el hecho de que se eliminó el filtro "safe" en el campo de Descripción, de esta manera, Jinja no asume que el código es seguro y aplica autoescaping, escapando los carcteres especiales utilizados en HTML, como '<' y '>' en este caso.

---
# Ejercicio 3 - File Upload

## Vulnerabilidad encontrada

Explicación de dónde está y por qué ocurre.

## Prueba de concepto (PoC)

### Pasos para explotar
1. ...
2. ...
3. ...

### Payload utilizado
...

### Resultado
...

## Mitigación

Explicación de los cambios realizados.

### Código vulnerable
...

### Código mitigado
...

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload.

### Resultado
El ataque ya no funciona porque...

---
# Ejercicio 4 - Server Side Template Injection

## Vulnerabilidad encontrada

Explicación de dónde está y por qué ocurre.

## Prueba de concepto (PoC)

### Pasos para explotar
1. ...
2. ...
3. ...

### Payload utilizado
...

### Resultado
...

## Mitigación

Explicación de los cambios realizados.

### Código vulnerable
...

### Código mitigado
...

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload.

### Resultado
El ataque ya no funciona porque...

---
# Ejercicio 5 - Almacenamiento inseguro

## Vulnerabilidad encontrada

Explicación de dónde está y por qué ocurre.

## Prueba de concepto (PoC)

### Pasos para explotar
1. ...
2. ...
3. ...

### Payload utilizado
...

### Resultado
...

## Mitigación

Explicación de los cambios realizados.

### Código vulnerable
...

### Código mitigado
...

## Verificación de la mitigación

Se repitió la PoC original con el mismo payload.

### Resultado
El ataque ya no funciona porque...

---
## Referencias

### Ejercicio 1
- https://owasp-uruguay.github.io/sqli-en-la-practica/
- https://portswigger.net/web-security/learning-paths/sql-injection
- https://bobby-tables.com/python 

### Ejercicio 2
- https://owasp.org/www-community/attacks/xss/
- https://flask.palletsprojects.com/es/stable/templating/