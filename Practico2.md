# Ejercicio 1 - Inyección SQL (SQLi)

## Vulnerabilidad encontrada

La vulnerabilidad se encuentra en la linea 29 de la funcion "buscar_funciones", donde el valor ingresado por el usuario se incorpora dentro de la consulta SQL.

Esta vulnerabilidad tiene lugar ya que la entrada del usuario se inyecta en la consulta sin utilizar parametros; permitiendo ingresar caracteres con significado especial para SQL, modificando el comportamiento de la consulta.

## Prueba de concepto (PoC)

### Pasos para explotar
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

