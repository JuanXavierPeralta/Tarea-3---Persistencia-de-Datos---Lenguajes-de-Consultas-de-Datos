
package com.latamairlines.dao;

import com.latamairlines.entities.Equipaje;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;

public class EquipajeDAO {

    private final EntityManagerFactory emf
            = Persistence.createEntityManagerFactory("LatamAirlinesPU");

    // CONSULTA JPQL 3: EQUIPAJES POR PESO CON PAGINACION
    public List<Equipaje> consultarPorPeso(
            BigDecimal pesoMinimo, int pagina, int tamanoPagina) {

        if (pesoMinimo == null
                || pesoMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El peso minimo no puede ser negativo");
        }

        if (pagina < 1 || tamanoPagina < 1) {
            throw new IllegalArgumentException(
                    "La pagina y su tamaño deben ser mayores a cero");
        }

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Equipaje e "
                    + "JOIN FETCH e.reserva "
                    + "WHERE e.pesoKg >= :pesoMinimo "
                    + "ORDER BY e.pesoKg ASC, e.id ASC",
                    Equipaje.class
            )
                    .setParameter("pesoMinimo", pesoMinimo)
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
