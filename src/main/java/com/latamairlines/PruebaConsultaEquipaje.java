
package com.latamairlines;

import com.latamairlines.dao.EquipajeDAO;
import com.latamairlines.entities.Equipaje;
import com.latamairlines.entities.Reserva;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;

public class PruebaConsultaEquipaje {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("LatamAirlinesPU");

        EquipajeDAO dao = new EquipajeDAO();

        try {
            // REGISTRAR EQUIPAJES SI LA TABLA ESTA VACIA
            EntityManager em = emf.createEntityManager();

            try {
                Long cantidad = em.createQuery(
                        "SELECT COUNT(e) FROM Equipaje e",
                        Long.class
                ).getSingleResult();

                if (cantidad == 0) {

                    Reserva reserva = em.find(Reserva.class, 3);

                    if (reserva == null) {
                        throw new IllegalArgumentException(
                                "No existe la reserva 3");
                    }

                    em.getTransaction().begin();

                    BigDecimal[] pesos = {
                        new BigDecimal("12.50"),
                        new BigDecimal("18.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("23.50"),
                        new BigDecimal("25.00"),
                        new BigDecimal("30.00")
                    };

                    for (BigDecimal peso : pesos) {
                        Equipaje equipaje = new Equipaje(
                                "Bodega", peso, reserva
                        );
                        em.persist(equipaje);
                    }

                    em.getTransaction().commit();

                    System.out.println(
                            "6 equipajes registrados correctamente");
                }

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;

            } finally {
                em.close();
            }

            // CONSULTA JPQL 3
            BigDecimal pesoMinimo = new BigDecimal("10.00");
            int tamanoPagina = 5;

            for (int pagina = 1; pagina <= 2; pagina++) {

                List<Equipaje> resultados =
                        dao.consultarPorPeso(
                                pesoMinimo, pagina, tamanoPagina
                        );

                System.out.println("\nCONSULTA JPQL 3");
                System.out.println("Peso minimo: " + pesoMinimo);
                System.out.println("Pagina: " + pagina);
                System.out.println("-------------------------");

                if (resultados.isEmpty()) {
                    System.out.println("No se encontraron equipajes");
                }

                for (Equipaje equipaje : resultados) {
                    System.out.println(
                            "ID: " + equipaje.getId()
                            + " | Tipo: " + equipaje.getTipo()
                            + " | Peso: " + equipaje.getPesoKg()
                            + " kg | Reserva: "
                            + equipaje.getReserva().getId()
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            dao.cerrar();
            emf.close();
        }
    }
}
