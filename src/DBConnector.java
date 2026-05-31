// DBConnector.java - Fixed Version with Public Key Retrieval
import java.sql.*;
import java.util.*;

class DBConnector {

    // 🔥 FIXED: Added allowPublicKeyRetrieval=true
    private static final String URL = "jdbc:mysql://localhost:3306/logistics_dashboard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "admin123";  // 🔥 Apna password daalo

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Database Connected Successfully!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found!");
            System.out.println("Error: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("❌ Database Connection Failed!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Check: URL, Username, Password, and if MySQL is running");
            return null;
        }
    }

    public static ArrayList<Order> getOrders() {
        ArrayList<Order> list = new ArrayList<>();
        String sql = "SELECT f.fact_id, c.customer_name, p.product_name, f.order_quantity, f.revenue, f.order_status, d.date " +
                "FROM fact_logistics f " +
                "JOIN dim_customer c ON f.customer_id = c.customer_id " +
                "JOIN dim_product p ON f.product_id = p.product_id " +
                "JOIN dim_date d ON f.date_id = d.date_id " +
                "ORDER BY f.fact_id DESC";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("fact_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_name"),
                        rs.getInt("order_quantity"),
                        rs.getDouble("revenue"),
                        rs.getString("order_status"),
                        rs.getString("date")
                ));
            }
            System.out.println("✅ Loaded " + list.size() + " orders");
        } catch (Exception e) {
            System.out.println("❌ Error in getOrders: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public static KPIData getKPIData() {
        KPIData kpi = new KPIData();
        String sql = "SELECT " +
                "COUNT(*) as total_orders, " +
                "SUM(CASE WHEN order_status='Delivered' THEN 1 ELSE 0 END) as delivered_count, " +
                "SUM(CASE WHEN order_status='Pending' THEN 1 ELSE 0 END) as pending_count, " +
                "SUM(CASE WHEN order_status='Delayed' THEN 1 ELSE 0 END) as delayed_count, " +
                "SUM(revenue) as total_revenue, " +
                "SUM(profit) as total_profit " +
                "FROM fact_logistics";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                kpi.totalOrders = rs.getInt("total_orders");
                kpi.delivered = rs.getInt("delivered_count");
                kpi.pending = rs.getInt("pending_count");
                kpi.delayed = rs.getInt("delayed_count");
                kpi.totalRevenue = rs.getDouble("total_revenue");
                kpi.totalProfit = rs.getDouble("total_profit");
                System.out.println("✅ KPI Data: Orders=" + kpi.totalOrders + ", Revenue=" + kpi.totalRevenue);
            }
        } catch (Exception e) {
            System.out.println("❌ Error in getKPIData: " + e.getMessage());
            e.printStackTrace();
        }
        return kpi;
    }

    public static HashMap<String, Double> getDailyRevenue() {
        HashMap<String, Double> dailyRevenue = new LinkedHashMap<>();
        String sql = "SELECT d.date, SUM(f.revenue) as revenue " +
                "FROM fact_logistics f " +
                "JOIN dim_date d ON f.date_id = d.date_id " +
                "GROUP BY d.date " +
                "ORDER BY d.date ASC";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                dailyRevenue.put(rs.getString("date"), rs.getDouble("revenue"));
            }
            System.out.println("✅ Loaded " + dailyRevenue.size() + " daily revenue records");
        } catch (Exception e) {
            System.out.println("❌ Error in getDailyRevenue: " + e.getMessage());
            e.printStackTrace();
        }
        return dailyRevenue;
    }

    public static class Order {
        public int id;
        public String customer;
        public String product;
        public int qty;
        public double revenue;
        public String status;
        public String date;

        public Order(int id, String c, String p, int q, double r, String s, String d) {
            this.id = id;
            this.customer = c;
            this.product = p;
            this.qty = q;
            this.revenue = r;
            this.status = s;
            this.date = d;
        }
    }

    public static class KPIData {
        public int totalOrders, delivered, pending, delayed;
        public double totalRevenue, totalProfit;
    }
}