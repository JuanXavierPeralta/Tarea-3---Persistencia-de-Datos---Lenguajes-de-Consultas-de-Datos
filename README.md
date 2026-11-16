
# Tarea 3 - Persistencia de Datos

## Unidad 3: Lenguajes de Consultas de Datos

**Universidad Politécnica Salesiana**  
**Carrera:** Ingeniería de Software  
**Asignatura:** Persistencia de Datos  
**Estudiante:** Juan Xavier Peralta León  

## Descripción

Desarrollo de un sistema de gestión de reservas de LATAM Airlines mediante Java, JPA, Hibernate y PostgreSQL, aplicando el mapeo objeto-relacional (ORM) y consultas JPQL.

## Tecnologías utilizadas

- Java
- Jakarta Persistence (JPA)
- Hibernate ORM
- PostgreSQL
- Java Swing
- Apache Maven
- NetBeans

## Modelo ORM

El sistema está compuesto por cinco entidades:

- Cliente
- Vuelo
- Reserva
- Pago
- Equipaje

## Consultas JPQL implementadas

1. **Reservas por destino:** búsqueda de reservas según el destino del vuelo.
2. **Reservas por monto pagado:** consulta de reservas con un monto mínimo de pago.
3. **Equipajes por peso:** búsqueda de equipajes según su peso mínimo.

Las consultas incluyen filtros, ordenamiento y paginación mediante `setFirstResult()` y `setMaxResults()`.

## Funcionalidades

- Operaciones CRUD de reservas.
- Relaciones entre entidades mediante JPA.
- Consultas JPQL con parámetros.
- Visualización de resultados mediante Java Swing.
- Paginación de resultados.

## Base de datos

**Motor:** PostgreSQL  
**Base de datos:** `bd_LatamAirlines`

## Autor

**Juan Xavier Peralta León**  
Universidad Politécnica Salesiana
