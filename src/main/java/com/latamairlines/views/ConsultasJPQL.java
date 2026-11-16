package com.latamairlines.views;

import com.latamairlines.dao.ReservaDAO;
import com.latamairlines.dao.EquipajeDAO;
import com.latamairlines.entities.Reserva;
import com.latamairlines.entities.Equipaje;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ConsultasJPQL extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConsultasJPQL.class.getName());
    // ACCESO A DATOS
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final EquipajeDAO equipajeDAO = new EquipajeDAO();

// CONTROL DE PAGINACION
    private static final int TAMANO_PAGINA = 5;
    private int paginaActual = 1;

// CONSULTA Y FILTRO EJECUTADOS
    private int consultaActual = -1;
    private String filtroActual = "";

    public ConsultasJPQL() {
        initComponents();

        // CONFIGURACION DE VENTANA
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // CONFIGURACION DE TABLA
        tblRESULTADOSDECONSULTA.setRowHeight(28);
        tblRESULTADOSDECONSULTA.getTableHeader()
                .setReorderingAllowed(false);

        // CONFIGURACION DE AREA JPQL
        taJPQL.setEditable(false);
        taJPQL.setLineWrap(true);
        taJPQL.setWrapStyleWord(true);
        taJPQL.setFont(new java.awt.Font(
                "Monospaced", java.awt.Font.PLAIN, 14));

        // CONFIGURACION INICIAL
        configurarTipoConsulta();

        // CERRAR RECURSOS AL CERRAR LA VENTANA
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                reservaDAO.cerrar();
                equipajeDAO.cerrar();
            }
        });
    }

    // CONFIGURAR TIPO DE CONSULTA
    private void configurarTipoConsulta() {

        int tipo = jComboBoxTIPODECONSULTA.getSelectedIndex();

        switch (tipo) {
            case 0:
                jLabelVALORDEFILTRO.setText("Destino del vuelo");
                break;

            case 1:
                jLabelVALORDEFILTRO.setText("Monto minimo ($)");
                break;

            case 2:
                jLabelVALORDEFILTRO.setText("Peso minimo (kg)");
                break;
        }

        // REINICIAR RESULTADOS
        paginaActual = 1;
        consultaActual = -1;
        filtroActual = "";

        jTextFieldVALORDEFILTRO.setText("");
        taJPQL.setText("");

        jLabelPAGINA.setText("Pagina 1");
        jLabelSINRESULTADOS.setText("Sin consulta ejecutada");

        jButtonANTERIOR.setEnabled(false);
        jButtonSIGUIENTE.setEnabled(false);

        configurarColumnas(tipo);
    }

    // CONFIGURAR COLUMNAS SEGUN LA CONSULTA
    private void configurarColumnas(int tipo) {

        String[] columnas;

        switch (tipo) {
            case 0:
                columnas = new String[]{
                    "ID", "Cliente", "Vuelo",
                    "Origen", "Destino", "Pasajeros"
                };
                break;

            case 1:
                columnas = new String[]{
                    "ID", "Cliente", "Vuelo",
                    "Destino", "Pago ($)"
                };
                break;

            case 2:
                columnas = new String[]{
                    "ID", "Tipo", "Peso (kg)", "Reserva"
                };
                break;

            default:
                columnas = new String[]{};
        }

        DefaultTableModel modelo = new DefaultTableModel(
                columnas, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tblRESULTADOSDECONSULTA.setModel(modelo);
    }

    // OBTENER CODIGO JPQL
    private String obtenerCodigoJPQL(int tipo) {

        switch (tipo) {

            case 0:
                return "SELECT r FROM Reserva r\n"
                        + "JOIN FETCH r.cliente\n"
                        + "JOIN FETCH r.vuelo\n"
                        + "WHERE LOWER(r.vuelo.destino) LIKE :destino\n"
                        + "ORDER BY r.id ASC";

            case 1:
                return "SELECT r FROM Reserva r\n"
                        + "JOIN FETCH r.cliente\n"
                        + "JOIN FETCH r.vuelo\n"
                        + "JOIN FETCH r.pago\n"
                        + "WHERE r.pago.monto >= :montoMinimo\n"
                        + "ORDER BY r.id ASC";

            case 2:
                return "SELECT e FROM Equipaje e\n"
                        + "JOIN FETCH e.reserva\n"
                        + "WHERE e.pesoKg >= :pesoMinimo\n"
                        + "ORDER BY e.pesoKg ASC, e.id ASC";

            default:
                return "";
        }
    }

    // EJECUTAR CONSULTA JPQL Y MOSTRAR RESULTADOS
    private void ejecutarConsulta(int pagina) {

        try {
            if (consultaActual < 0) {
                throw new IllegalArgumentException(
                        "Seleccione una consulta");
            }

            if (filtroActual.isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingrese un valor de filtro");
            }

            configurarColumnas(consultaActual);

            DefaultTableModel modelo
                    = (DefaultTableModel) tblRESULTADOSDECONSULTA.getModel();

            boolean haySiguiente = false;

            switch (consultaActual) {

                // CONSULTA 1: RESERVAS POR DESTINO
                case 0: {

                    List<Reserva> reservas
                            = reservaDAO.consultarPorDestino(
                                    filtroActual,
                                    pagina,
                                    TAMANO_PAGINA);

                    for (Reserva r : reservas) {
                        modelo.addRow(new Object[]{
                            r.getId(),
                            r.getCliente().getNombre(),
                            r.getVuelo().getCodigo(),
                            r.getVuelo().getOrigen(),
                            r.getVuelo().getDestino(),
                            r.getNumeroPasajeros()
                        });
                    }

                    haySiguiente
                            = !reservaDAO.consultarPorDestino(
                                    filtroActual,
                                    pagina + 1,
                                    TAMANO_PAGINA).isEmpty();
                    break;
                }

                // CONSULTA 2: RESERVAS POR MONTO PAGADO
                case 1: {

                    BigDecimal monto = new BigDecimal(
                            filtroActual.replace(",", "."));

                    List<Reserva> reservas
                            = reservaDAO.consultarPorMontoPagado(
                                    monto,
                                    pagina,
                                    TAMANO_PAGINA);

                    for (Reserva r : reservas) {
                        modelo.addRow(new Object[]{
                            r.getId(),
                            r.getCliente().getNombre(),
                            r.getVuelo().getCodigo(),
                            r.getVuelo().getDestino(),
                            r.getPago().getMonto()
                        });
                    }

                    haySiguiente
                            = !reservaDAO.consultarPorMontoPagado(
                                    monto,
                                    pagina + 1,
                                    TAMANO_PAGINA).isEmpty();
                    break;
                }

                // CONSULTA 3: EQUIPAJES POR PESO
                case 2: {

                    BigDecimal peso = new BigDecimal(
                            filtroActual.replace(",", "."));

                    List<Equipaje> equipajes
                            = equipajeDAO.consultarPorPeso(
                                    peso,
                                    pagina,
                                    TAMANO_PAGINA);

                    for (Equipaje e : equipajes) {
                        modelo.addRow(new Object[]{
                            e.getId(),
                            e.getTipo(),
                            e.getPesoKg(),
                            e.getReserva().getId()
                        });
                    }

                    haySiguiente
                            = !equipajeDAO.consultarPorPeso(
                                    peso,
                                    pagina + 1,
                                    TAMANO_PAGINA).isEmpty();
                    break;
                }
            }

            // ACTUALIZAR PAGINACION
            paginaActual = pagina;

            jLabelPAGINA.setText("Pagina " + paginaActual);

            jButtonANTERIOR.setEnabled(paginaActual > 1);
            jButtonSIGUIENTE.setEnabled(haySiguiente);

            // MOSTRAR CANTIDAD DE RESULTADOS
            int cantidad = modelo.getRowCount();

            if (cantidad == 0) {
                jLabelSINRESULTADOS.setText(
                        "No se encontraron resultados");
            } else {
                jLabelSINRESULTADOS.setText(
                        "Mostrando " + cantidad + " registros");
            }

            // MOSTRAR CONSULTA JPQL
            taJPQL.setText(
                    obtenerCodigoJPQL(consultaActual));

            taJPQL.setCaretPosition(0);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al ejecutar consulta: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelCONSULTASJQPL = new javax.swing.JLabel();
        jLabelTIPODECONSULTA = new javax.swing.JLabel();
        jComboBoxTIPODECONSULTA = new javax.swing.JComboBox<>();
        jLabelVALORDEFILTRO = new javax.swing.JLabel();
        jTextFieldVALORDEFILTRO = new javax.swing.JTextField();
        jButtonEJECUTARCONSULTA = new javax.swing.JButton();
        jLabelRESULTADOSDECONSULTA = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRESULTADOSDECONSULTA = new javax.swing.JTable();
        jLabelCONSULTAJQPLEJECUTADA = new javax.swing.JLabel();
        jButtonANTERIOR = new javax.swing.JButton();
        jButtonSIGUIENTE = new javax.swing.JButton();
        jLabelPAGINA = new javax.swing.JLabel();
        jLabelSINRESULTADOS = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taJPQL = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        labelCONSULTASJQPL.setBackground(new java.awt.Color(51, 102, 255));
        labelCONSULTASJQPL.setFont(new java.awt.Font("Bahnschrift", 1, 24)); // NOI18N
        labelCONSULTASJQPL.setForeground(new java.awt.Color(0, 0, 0));
        labelCONSULTASJQPL.setText("LATAM Airlines - Consultas JPQL");

        jLabelTIPODECONSULTA.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabelTIPODECONSULTA.setText("Tipo de consulta");

        jComboBoxTIPODECONSULTA.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Reservas por destino", "Reservas por monto pagado", "Equipajes por peso" }));
        jComboBoxTIPODECONSULTA.addActionListener(this::jComboBoxTIPODECONSULTAActionPerformed);

        jLabelVALORDEFILTRO.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabelVALORDEFILTRO.setText("Valor de filtro");

        jButtonEJECUTARCONSULTA.setFont(new java.awt.Font("Bahnschrift", 0, 12)); // NOI18N
        jButtonEJECUTARCONSULTA.setText("Ejecutar Consulta");
        jButtonEJECUTARCONSULTA.setActionCommand("Ejecutar");
        jButtonEJECUTARCONSULTA.addActionListener(this::jButtonEJECUTARCONSULTAActionPerformed);

        jLabelRESULTADOSDECONSULTA.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabelRESULTADOSDECONSULTA.setText("Resultados de consulta");

        tblRESULTADOSDECONSULTA.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        tblRESULTADOSDECONSULTA.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Cliente", "Vuelo", "Origen", "Destino", "Pasajeros", "Pago"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblRESULTADOSDECONSULTA.setToolTipText("");
        jScrollPane1.setViewportView(tblRESULTADOSDECONSULTA);

        jLabelCONSULTAJQPLEJECUTADA.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabelCONSULTAJQPLEJECUTADA.setText("Consulta JPQL ejecutada");

        jButtonANTERIOR.setFont(new java.awt.Font("Bahnschrift", 0, 12)); // NOI18N
        jButtonANTERIOR.setText("Anterior");
        jButtonANTERIOR.setActionCommand("Ejecutar");
        jButtonANTERIOR.addActionListener(this::jButtonANTERIORActionPerformed);

        jButtonSIGUIENTE.setFont(new java.awt.Font("Bahnschrift", 0, 12)); // NOI18N
        jButtonSIGUIENTE.setText("Siguiente\n");
        jButtonSIGUIENTE.setActionCommand("Ejecutar");
        jButtonSIGUIENTE.addActionListener(this::jButtonSIGUIENTEActionPerformed);

        jLabelPAGINA.setFont(new java.awt.Font("Bahnschrift", 1, 12)); // NOI18N
        jLabelPAGINA.setText("Pagina 1");

        jLabelSINRESULTADOS.setFont(new java.awt.Font("Bahnschrift", 1, 12)); // NOI18N
        jLabelSINRESULTADOS.setText("Sin resultados");

        taJPQL.setEditable(false);
        taJPQL.setColumns(20);
        taJPQL.setLineWrap(true);
        taJPQL.setRows(5);
        taJPQL.setWrapStyleWord(true);
        jScrollPane2.setViewportView(taJPQL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 705, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labelCONSULTASJQPL))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelSINRESULTADOS)
                                    .addGap(44, 44, 44)
                                    .addComponent(jButtonANTERIOR)
                                    .addGap(7, 7, 7)
                                    .addComponent(jLabelPAGINA)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jButtonSIGUIENTE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelRESULTADOSDECONSULTA)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 701, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(38, 38, 38)
                                            .addComponent(jComboBoxTIPODECONSULTA, javax.swing.GroupLayout.PREFERRED_SIZE, 628, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(39, 39, 39)
                                            .addComponent(jTextFieldVALORDEFILTRO, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jButtonEJECUTARCONSULTA))
                                        .addComponent(jLabelVALORDEFILTRO)
                                        .addComponent(jLabelTIPODECONSULTA)
                                        .addComponent(jLabelCONSULTAJQPLEJECUTADA)))))
                        .addGap(0, 9, Short.MAX_VALUE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(labelCONSULTASJQPL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTIPODECONSULTA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxTIPODECONSULTA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelVALORDEFILTRO)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldVALORDEFILTRO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonEJECUTARCONSULTA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelRESULTADOSDECONSULTA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonANTERIOR)
                            .addComponent(jLabelPAGINA)
                            .addComponent(jButtonSIGUIENTE))
                        .addGap(33, 33, 33))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSINRESULTADOS)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(jLabelCONSULTAJQPLEJECUTADA)
                        .addGap(5, 5, 5)))
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTIPODECONSULTAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTIPODECONSULTAActionPerformed
        configurarTipoConsulta();
    }//GEN-LAST:event_jComboBoxTIPODECONSULTAActionPerformed

    private void jButtonEJECUTARCONSULTAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEJECUTARCONSULTAActionPerformed
        consultaActual
                = jComboBoxTIPODECONSULTA.getSelectedIndex();

        filtroActual
                = jTextFieldVALORDEFILTRO.getText().trim();

        paginaActual = 1;

        ejecutarConsulta(paginaActual);
    }//GEN-LAST:event_jButtonEJECUTARCONSULTAActionPerformed

    private void jButtonANTERIORActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonANTERIORActionPerformed
        if (paginaActual > 1) {
            ejecutarConsulta(paginaActual - 1);
        }
    }//GEN-LAST:event_jButtonANTERIORActionPerformed

    private void jButtonSIGUIENTEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSIGUIENTEActionPerformed
        ejecutarConsulta(paginaActual + 1);
    }//GEN-LAST:event_jButtonSIGUIENTEActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ConsultasJPQL().setVisible(true));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonANTERIOR;
    private javax.swing.JButton jButtonEJECUTARCONSULTA;
    private javax.swing.JButton jButtonSIGUIENTE;
    private javax.swing.JComboBox<String> jComboBoxTIPODECONSULTA;
    private javax.swing.JLabel jLabelCONSULTAJQPLEJECUTADA;
    private javax.swing.JLabel jLabelPAGINA;
    private javax.swing.JLabel jLabelRESULTADOSDECONSULTA;
    private javax.swing.JLabel jLabelSINRESULTADOS;
    private javax.swing.JLabel jLabelTIPODECONSULTA;
    private javax.swing.JLabel jLabelVALORDEFILTRO;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldVALORDEFILTRO;
    private javax.swing.JLabel labelCONSULTASJQPL;
    private javax.swing.JTextArea taJPQL;
    private javax.swing.JTable tblRESULTADOSDECONSULTA;
    // End of variables declaration//GEN-END:variables
}
