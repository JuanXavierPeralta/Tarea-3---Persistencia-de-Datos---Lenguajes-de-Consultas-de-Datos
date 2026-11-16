package com.latamairlines.dao;

import com.latamairlines.entities.Cliente;
import com.latamairlines.entities.Reserva;
import com.latamairlines.entities.Vuelo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.math.BigDecimal;

public class ReservaDAO {

    private final EntityManagerFactory emf
            = Persistence.createEntityManagerFactory("LatamAirlinesPU");

    // CREAR RESERVA
    public void crearReserva(Reserva reserva) {

        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria");
        }

        if (reserva.getId() != null) {
            throw new IllegalArgumentException("La reserva ya tiene un ID");
        }

        validarDatos(
                reserva.getCliente(),
                reserva.getVuelo(),
                reserva.getNumeroPasajeros()
        );

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // BUSCAR CLIENTE Y VUELO EN LA BASE DE DATOS
            Cliente cliente = em.find(
                    Cliente.class, reserva.getCliente().getId()
            );

            Vuelo vuelo = em.find(
                    Vuelo.class, reserva.getVuelo().getId()
            );

            if (cliente == null) {
                throw new IllegalArgumentException("Cliente no encontrado");
            }

            if (vuelo == null) {
                throw new IllegalArgumentException("Vuelo no encontrado");
            }

            // ASOCIAR OBJETOS ADMINISTRADOS
            reserva.setCliente(cliente);
            reserva.setVuelo(vuelo);

            em.persist(reserva);
            em.getTransaction().commit();

            System.out.println("Reserva creada correctamente");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    // BUSCAR RESERVA POR ID
    public Reserva buscarPorId(int id) {

        EntityManager em = emf.createEntityManager();

        try {
            List<Reserva> resultados = em.createQuery(
                    "SELECT r FROM Reserva r "
                    + "JOIN FETCH r.cliente "
                    + "JOIN FETCH r.vuelo "
                    + "LEFT JOIN FETCH r.pago "
                    + "WHERE r.id = :id",
                    Reserva.class
            )
                    .setParameter("id", id)
                    .getResultList();

            return resultados.isEmpty() ? null : resultados.get(0);

        } finally {
            em.close();
        }
    }

    // LISTAR RESERVAS CON CLIENTE, VUELO Y PAGO
    public List<Reserva> listarReservas() {

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM Reserva r "
                    + "JOIN FETCH r.cliente "
                    + "JOIN FETCH r.vuelo "
                    + "LEFT JOIN FETCH r.pago "
                    + "ORDER BY r.id",
                    Reserva.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // ACTUALIZAR RESERVA
    public void actualizarReserva(int id, Cliente cliente,
            Vuelo vuelo, int pasajeros) {

        validarDatos(cliente, vuelo, pasajeros);

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Reserva reserva = em.find(Reserva.class, id);

            if (reserva == null) {
                throw new IllegalArgumentException("Reserva no encontrada");
            }

            Cliente clienteRegistrado = em.find(
                    Cliente.class, cliente.getId()
            );

            Vuelo vueloRegistrado = em.find(
                    Vuelo.class, vuelo.getId()
            );

            if (clienteRegistrado == null) {
                throw new IllegalArgumentException("Cliente no encontrado");
            }

            if (vueloRegistrado == null) {
                throw new IllegalArgumentException("Vuelo no encontrado");
            }

            reserva.setCliente(clienteRegistrado);
            reserva.setVuelo(vueloRegistrado);
            reserva.setNumeroPasajeros(pasajeros);

            em.getTransaction().commit();

            System.out.println("Reserva actualizada correctamente");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    // ELIMINAR RESERVA
    public void eliminarReserva(int id) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Reserva reserva = em.find(Reserva.class, id);

            if (reserva == null) {
                throw new IllegalArgumentException("Reserva no encontrada");
            }

            // EVITAR ELIMINAR UNA RESERVA CON UN PAGO REGISTRADO
            if (reserva.getPago() != null) {
                throw new IllegalArgumentException(
                        "No se puede eliminar una reserva con pago registrado"
                );
            }

            em.remove(reserva);
            em.getTransaction().commit();

            System.out.println("Reserva eliminada correctamente");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    // VALIDAR DATOS DE LA RESERVA
    private void validarDatos(Cliente cliente, Vuelo vuelo,
            Integer pasajeros) {

        if (cliente == null || cliente.getId() == null) {
            throw new IllegalArgumentException(
                    "Seleccione un cliente registrado"
            );
        }

        if (vuelo == null || vuelo.getId() == null) {
            throw new IllegalArgumentException(
                    "Seleccione un vuelo registrado"
            );
        }

        if (pasajeros == null || pasajeros <= 0) {
            throw new IllegalArgumentException(
                    "El número de pasajeros debe ser mayor a cero"
            );
        }
    }

    // CONSULTA JPQL 1: RESERVAS POR DESTINO CON PAGINACION
    public List<Reserva> consultarPorDestino(
            String destino, int pagina, int tamanoPagina) {

        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingrese un destino");
        }

        if (pagina < 1 || tamanoPagina < 1) {
            throw new IllegalArgumentException(
                    "La pagina y su tamaño deben ser mayores a cero");
        }

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM Reserva r "
                    + "JOIN FETCH r.cliente "
                    + "JOIN FETCH r.vuelo "
                    + "WHERE LOWER(r.vuelo.destino) LIKE :destino "
                    + "ORDER BY r.id ASC",
                    Reserva.class
            )
                    .setParameter(
                            "destino",
                            "%" + destino.trim().toLowerCase(
                                    java.util.Locale.ROOT) + "%"
                    )
                    .setFirstResult((pagina - 1) * tamanoPagina)
                    .setMaxResults(tamanoPagina)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    // CONSULTA JPQL 2: RESERVAS POR MONTO PAGADO CON PAGINACION
    public List<Reserva> consultarPorMontoPagado(
            BigDecimal montoMinimo, int pagina, int tamanoPagina) {

        if (montoMinimo == null
                || montoMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El monto minimo no puede ser negativo");
        }

        if (pagina < 1 || tamanoPagina < 1) {
            throw new IllegalArgumentException(
                    "La pagina y su tamaño deben ser mayores a cero");
        }

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM Reserva r "
                    + "JOIN FETCH r.cliente "
                    + "JOIN FETCH r.vuelo "
                    + "JOIN FETCH r.pago "
                    + "WHERE r.pago.monto >= :montoMinimo "
                    + "ORDER BY r.id ASC",
                    Reserva.class
            )
                    .setParameter("montoMinimo", montoMinimo)
                    .setFirstResult((pagina - 1) * tamanoPagina)
                    .setMaxResults(tamanoPagina)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    // CERRAR RECURSOS
    public void cerrar() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
