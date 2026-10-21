package dbo;
import database.DBconnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Orderdb {
    public int createOrder(Connection con, int userId)
            throws SQLException {
        String sql =
            "INSERT INTO orders (user_id, total_amount, status) " +
            "VALUES (?, 0.00, 'PLACED')";

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
    
            ps.setInt(1, userId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Order ID could not be generated.");
    }
    public boolean addOrderItem(Connection con, int orderId,
                                int itemId, int quantity)
            throws SQLException {
        if (quantity <= 0) {
            throw new SQLException(
                "Quantity must be greater than zero."
            );
        }
        String sql ="INSERT INTO order_items " +
            "(order_id, item_id, quantity, item_price) " +
            "SELECT ?, item_id, ?, price " +
            "FROM menu_items " +
            "WHERE item_id = ? AND is_available = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, quantity);
            ps.setInt(3, itemId);
            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new SQLException(
                    "Food item not found or unavailable: " + itemId
                );
            }
            return true;
        }
    }
    public boolean updateOrderTotal(Connection con, int orderId)
            throws SQLException {

        String sql ="UPDATE orders SET total_amount = (" +
            "SELECT COALESCE(SUM(quantity * item_price), 0) " +
            "FROM order_items WHERE order_id = ?) " +
            "WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, orderId);
            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new SQLException(
                    "Could not update total for order: " + orderId
                );
            }
            System.out.println("yaay");
            return true;
        }
    }
    private double getOrderTotal(int orderId) throws SQLException {
        String sql =
            "SELECT total_amount FROM orders WHERE order_id = ?";
        try (Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_amount");
                }
            }
        }
        return 0.0;
    }
    public boolean placeOrder(int userId, int[] itemIds, int[] quantities) {
        if (itemIds == null || quantities == null ||
            itemIds.length == 0 ||
            itemIds.length != quantities.length) {
            System.out.println("Invalid order details.");
            return false;
        }
        try (Connection con = DBconnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int orderId = createOrder(con, userId);

                for (int i = 0; i < itemIds.length; i++) {
                    addOrderItem(
                        con, orderId, itemIds[i], quantities[i]
                    );
                }
                updateOrderTotal(con, orderId);
                con.commit();
                System.out.println("Order placed successfully!");
                System.out.println("Order ID: " + orderId);
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.out.println(
                    "Order failed. All changes were rolled back."
                );
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
            return false;
        }
    }
    public void viewOrderDetails(int orderId) {
        String sql ="SELECT o.order_id, o.user_id, o.order_date, " +
            "o.total_amount, o.status, m.item_name, " +
            "oi.quantity, oi.item_price " +
            "FROM orders o " +
            "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
            "LEFT JOIN menu_items m ON oi.item_id = m.item_id " +
            "WHERE o.order_id = ?";
        try (Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    if (!found) {
                        found = true;
                        System.out.println(" ORDER DETAILS ");
                        System.out.println("Order ID: " + rs.getInt("order_id"));
                        System.out.println("Student ID: " + rs.getInt("user_id"));
                        System.out.println("Order Date: " + rs.getTimestamp("order_date"));
                        System.out.println("Status: " + rs.getString("status"));
                        System.out.println("");
                    }
                    String itemName = rs.getString("item_name");

                    if (itemName != null) {
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("item_price");

                        System.out.println(itemName +"  Quantity: " + quantity +
                            " Price each: Rs. " + price +
                            " Subtotal: Rs. " + (quantity * price));
                    }
                }

                if (found) {
                    System.out.println("hmm");
        
                    System.out.println("Total: Rs. " + getOrderTotal(orderId));
                } else {
                    System.out.println("Order not found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve order details.");
            e.printStackTrace();
        }
    }

    public void viewStudentOrders(int userId) {
        String sql ="SELECT order_id, order_date, total_amount, status " +
            "FROM orders WHERE user_id = ? " +
            "ORDER BY order_date DESC";

        try (Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Order ID: " + rs.getInt("order_id") +
                        " Date: " + rs.getTimestamp("order_date") +
                        " Total: ₹" + rs.getDouble("total_amount") +
                        " Status: " + rs.getString("status"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve student orders.");
            e.printStackTrace();
        }
    }
        
    public boolean updateOrderStatus(int orderId, String status) {
            String sql ="UPDATE orders SET status = ? WHERE order_id = ?";
            try (Connection con = DBconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, orderId);
                return ps.executeUpdate() == 1;
            } 
            catch (SQLException e) {
                System.out.println("Could not update order status.");
                e.printStackTrace();
                return false;
            }
        }

}


