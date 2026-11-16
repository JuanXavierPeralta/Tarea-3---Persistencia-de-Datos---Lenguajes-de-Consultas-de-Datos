
package com.latamairlines;

import com.latamairlines.dao.ReservaDAO;
import com.latamairlines.entities.Reserva;
import java.math.BigDecimal;
import java.util.List;

public class PruebaConsultaPago {

    public static void main(String[] args) {

        ReservaDAO dao = new ReservaDAO();

        try {
            BigDecimal montoMinimo = new BigDecimal("100.00");
            int pagina = 1;
            int tamanoPagina = 5;

            List<Reserva> resultados =
                    dao.consultarPorMontoPagado(
                            montoMinimo, pagina, tamanoPagina
                    );

            System.out.println("CONSULTA JPQL 2");
            System.out.println("Monto minimo: " + montoMinimo);
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
                            + " | Pago: $"
                            + reserva.getPago().getMonto()
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            dao.cerrar();
        }
    }
}
