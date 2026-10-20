
package ui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import database.DBconnection;
import dbo.Orderdb;
public class ManageOrders extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public ManageOrders  () {
        setTitle("Smart Canteen - Manage Orders");
        setSize(750, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {
            "Order ID", "Student ID", "Date",
            "Total (₹)", "Status"
        }; 

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        JButton refreshButton = new JButton("Refresh");
        JButton statusButton = new JButton("Change Status");

        refreshButton.addActionListener(e -> loadOrders());

        statusButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                    this, "Please select an order first."
                );
                return;
            }

            int orderId = (int) model.getValueAt(selectedRow, 0);

            String[] statuses = { "PLACED", "PREPARING", "READY","COMPLETED", "CANCELLED"};
            String status = (String) JOptionPane.showInputDialog(
                this,
                "Select the new order status:",
                "Change Order Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                model.getValueAt(selectedRow, 4)
            );

            if (status == null) {
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Change order #" + orderId + " to " + status + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            Orderdb orderDB = new Orderdb();

            if (orderDB.updateOrderStatus(orderId, status)) {
                JOptionPane.showMessageDialog(
                    this, "Order status updated successfully."
                );
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not update order status.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(statusButton);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadOrders();
    }
    private void loadOrders() {
        model.setRowCount(0);

        String sql ="SELECT order_id, user_id, order_date, " +
            "total_amount, status FROM orders " +
            "ORDER BY order_date DESC";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("order_id"),
                    rs.getInt("user_id"),
                    rs.getTimestamp("order_date"),
                    rs.getDouble("total_amount"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Could not load orders.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}
