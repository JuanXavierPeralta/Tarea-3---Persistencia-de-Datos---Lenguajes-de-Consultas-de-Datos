
package com.latamairlines;

import com.latamairlines.dao.ReservaDAO;
import com.latamairlines.entities.Reserva;
import java.util.List;

public class PruebaConsultaDestino {

    public static void main(String[] args) {

        ReservaDAO dao = new ReservaDAO();

        try {
            String destino = "Madrid";
            int pagina = 1;
            int tamanoPagina = 5;

            List<Reserva> resultados =
                    dao.consultarPorDestino(
                            destino, pagina, tamanoPagina
                    );

            System.out.println("CONSULTA JPQL 1");
            System.out.println("Destino: " + destino);
            System.out.println("Pagina: " + pagina);
            System.out.println("-------------------------");

            if (resultados.isEmpty()) {
                System.out.println("No se encontraron reservas");
            } else {
                for (Reserva reserva : resultados) {
                    System.out.println(
                            "ID: " + reserva.getId()
                            + " | Cliente: "
                            + reserva.getCliente().getNombre()
                            + " | Vuelo: "
                            + reserva.getVuelo().getCodigo()
                            + " | Destino: "
                            + reserva.getVuelo().getDestino()
                    );
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());

        } finally {
            dao.cerrar();
        }
    }
}
