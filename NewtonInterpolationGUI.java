import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

public class NewtonInterpolationGUI extends JFrame {
    private JTextField txtNumPoints;
    private JButton btnCalcular, btnOrdenar;
    private JTable tablePoints, tableFdd;
    private DefaultTableModel modelPoints, modelFdd;
    private JTextArea txtPolinomio;
    private int n;

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Si Nimbus no está disponible, se usa el look and feel por defecto
        }
        SwingUtilities.invokeLater(() -> new NewtonInterpolationGUI().setVisible(true));
    }

    public NewtonInterpolationGUI() {
        setTitle("Método de diferencias divididas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior para controles de puntos
        JPanel panelTop = new JPanel(new GridBagLayout());
        panelTop.setBorder(BorderFactory.createTitledBorder("Configuración de puntos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNum = new JLabel("Numero de puntos");
        lblNum.setFont(lblNum.getFont().deriveFont(Font.BOLD, 14f));
        panelTop.add(lblNum, gbc);
        gbc.gridx = 1;
        txtNumPoints = new JTextField(5);
        txtNumPoints.setToolTipText("Ingrese el número de puntos para la interpolación");
        panelTop.add(txtNumPoints, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        panelTop.add(Box.createVerticalStrut(10), gbc);

        // Actualizar tabla automáticamente al cambiar el número de puntos
        txtNumPoints.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarTablaPuntos(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarTablaPuntos(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarTablaPuntos(); }
        });

        // Panel para tabla de puntos y botón Calcular
        JPanel panelPoints = new JPanel(new BorderLayout(5, 5));
        panelPoints.setBorder(BorderFactory.createTitledBorder("Puntos (x, f(x))"));
        modelPoints = new DefaultTableModel(new Object[]{"X", "F(x)"}, 0);
        tablePoints = new JTable(modelPoints) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return new CustomColumnColorRenderer();
            }
        };
        JScrollPane spPoints = new JScrollPane(tablePoints);
        spPoints.setPreferredSize(new Dimension(150, 120));
        panelPoints.add(spPoints, BorderLayout.CENTER);
        btnCalcular = new JButton("Calcular");
        btnCalcular.setToolTipText("Calcula la tabla de diferencias divididas con el orden actual");
        panelPoints.add(btnCalcular, BorderLayout.SOUTH);

        // Panel para tabla de diferencias divididas
        JPanel panelFdd = new JPanel(new BorderLayout(5, 5));
        panelFdd.setBorder(BorderFactory.createTitledBorder("Tabla de diferencias divididas"));
        modelFdd = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableFdd = new JTable(modelFdd);
        JScrollPane spFdd = new JScrollPane(tableFdd);
        spFdd.setPreferredSize(new Dimension(400, 200));
        panelFdd.add(spFdd, BorderLayout.CENTER);
        btnOrdenar = new JButton("Ordenar");
        btnOrdenar.setToolTipText("Ordena los puntos por X y muestra la tabla y el polinomio");
        JPanel panelOrdenar = new JPanel();
        panelOrdenar.add(btnOrdenar);
        panelFdd.add(panelOrdenar, BorderLayout.SOUTH);

        // Panel central con tablas
        JPanel panelCenter = new JPanel(new BorderLayout(10, 10));
        panelCenter.add(panelPoints, BorderLayout.WEST);
        panelCenter.add(panelFdd, BorderLayout.CENTER);

        // Panel inferior para polinomio
        JPanel panelBottom = new JPanel(new BorderLayout(5, 5));
        panelBottom.setBorder(BorderFactory.createTitledBorder("Polinomio de Newton"));
        txtPolinomio = new JTextArea(3, 40);
        txtPolinomio.setEditable(false);
        txtPolinomio.setLineWrap(true);
        txtPolinomio.setWrapStyleWord(true);
        txtPolinomio.setFont(new Font("Consolas", Font.BOLD, 16));
        txtPolinomio.setBackground(new Color(245, 245, 245));
        txtPolinomio.setForeground(new Color(0, 70, 140));
        JScrollPane spPolinomio = new JScrollPane(txtPolinomio);
        panelBottom.add(spPolinomio, BorderLayout.CENTER);

        // Agregar paneles al frame
        add(panelTop, BorderLayout.NORTH);
        add(panelCenter, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);

        btnCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                calcularFdd();
            }
        });
        btnOrdenar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ordenarTabla();
            }
        });
    }

    private void actualizarTablaPuntos() {
        try {
            int nuevoN = Integer.parseInt(txtNumPoints.getText());
            n = nuevoN;
            modelPoints.setRowCount(0);
            for (int i = 0; i < n; i++) modelPoints.addRow(new Object[]{"", ""});
            modelFdd.setRowCount(0);
            modelFdd.setColumnCount(0);
            txtPolinomio.setText("");
        } catch (Exception ex) {
            modelPoints.setRowCount(0);
            modelFdd.setRowCount(0);
            modelFdd.setColumnCount(0);
            txtPolinomio.setText("");
        }
    }

    private boolean hayRepetidos(double[] x) {
        for (int i = 0; i < x.length; i++) {
            for (int j = i + 1; j < x.length; j++) {
                if (x[i] == x[j]) return true;
            }
        }
        return false;
    }

    private void calcularFdd() {
        try {
            if (tablePoints.isEditing()) {
                tablePoints.getCellEditor().stopCellEditing();
            }
            double[] x = new double[n];
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                x[i] = Double.parseDouble(modelPoints.getValueAt(i, 0).toString());
                y[i] = Double.parseDouble(modelPoints.getValueAt(i, 1).toString());
            }
            if (hayRepetidos(x)) {
                JOptionPane.showMessageDialog(this, "No puede haber valores repetidos en X", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            mostrarTablaYPolinomio(x, y, false); // Solo tabla
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verifique los datos ingresados");
        }
    }

    private void ordenarTabla() {
        try {
            if (tablePoints.isEditing()) {
                tablePoints.getCellEditor().stopCellEditing();
            }
            Double[][] puntos = new Double[n][2];
            for (int i = 0; i < n; i++) {
                puntos[i][0] = Double.parseDouble(modelPoints.getValueAt(i, 0).toString());
                puntos[i][1] = Double.parseDouble(modelPoints.getValueAt(i, 1).toString());
            }
            // Validar repetidos
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (puntos[i][0].equals(puntos[j][0])) {
                        JOptionPane.showMessageDialog(this, "No puede haber valores repetidos en X", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            Arrays.sort(puntos, Comparator.comparingDouble(a -> a[0]));
            double[] xOrd = new double[n];
            double[] yOrd = new double[n];
            for (int i = 0; i < n; i++) {
                xOrd[i] = puntos[i][0];
                yOrd[i] = puntos[i][1];
            }
            mostrarTablaYPolinomio(xOrd, yOrd, true); // Tabla y polinomio
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verifique los datos para ordenar");
        }
    }

    private void mostrarTablaYPolinomio(double[] x, double[] y, boolean mostrarPolinomio) {
        int n = x.length;
        double[][] fdd = new double[n][n];
        for (int i = 0; i < n; i++) fdd[i][0] = y[i];
        for (int j = 1; j < n; j++)
            for (int i = 0; i < n - j; i++)
                fdd[i][j] = (fdd[i+1][j-1] - fdd[i][j-1]) / (x[i+j] - x[i]);
        // Mostrar tabla
        modelFdd.setRowCount(0);
        modelFdd.setColumnCount(0);
        modelFdd.addColumn("x");
        for (int j = 0; j < n; j++) modelFdd.addColumn("y"+j);
        for (int i = 0; i < n; i++) {
            Object[] row = new Object[n+1];
            row[0] = String.format("%.0f", x[i]);
            for (int j = 0; j < n - i; j++) row[j+1] = String.format("%.0f", fdd[i][j]);
            modelFdd.addRow(row);
        }
        // Mostrar polinomio solo si se indica
        if (mostrarPolinomio) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%.0f", fdd[0][0]));
            for (int order = 1; order < n; order++) {
                sb.append(" + (" + String.format("%.0f", fdd[0][order]) + ")");
                for (int k = 0; k < order; k++) sb.append("*(x-" + String.format("%.0f", x[k]) + ")");
            }
            txtPolinomio.setText(sb.toString());
        } else {
            txtPolinomio.setText("");
        }
    }

    // Renderer personalizado para colorear columnas
    private static class CustomColumnColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Solo cambiar color si NO se está editando esta celda
            if (!isSelected && !(table.isCellEditable(row, column) && table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column)) {
                if (column == 0) {
                    c.setBackground(new Color(210, 230, 255)); // Azul suave
                } else if (column == 1) {
                    c.setBackground(new Color(230, 210, 255)); // Morado claro suave
                } else {
                    c.setBackground(Color.WHITE);
                }
            } else {
                c.setBackground(table.getSelectionBackground());
            }
            return c;
        }
    }
}
