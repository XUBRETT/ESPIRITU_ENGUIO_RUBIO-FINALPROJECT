import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class XUMenu extends JFrame {
    private static Connection connection;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> itemComboBox;
    private JTextField amountTextField;
    private JTextArea orderTextArea;
    private JLabel totalLabel;
    private JTextField dateTextField;

    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<Integer> amounts = new ArrayList<>();
    private ArrayList<String> orders = new ArrayList<>();

    public XUMenu() {
        setTitle("XU Bookstore Menu");
        setSize(1700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents(); // Initialize GUI components
        setupDatabase(); // Setup database connection
        eventHandlers(); // Setup event handlers for components
        login(); // Prompt login dialog
    }

    // Initialize GUI components and layout
    private void initComponents() {
        Font font = new Font("Arial", Font.PLAIN, 16);

        // Initialize combo boxes and set font
        categoryComboBox = new JComboBox<>(new String[]{
                "Ink-based Supplies",
                "General-use Papers",
                "Arts and Crafts Supplies",
                "Color Materials",
                "Engineering/Graphing/Measurement Tools",
                "Clothes and Merchandise"
        });
        categoryComboBox.setFont(font);

        itemComboBox = new JComboBox<>();
        itemComboBox.setFont(font);

        // Initialize text fields and set font
        amountTextField = new JTextField(5);
        amountTextField.setFont(font);
        dateTextField = new JTextField(10);
        dateTextField.setFont(font);
        dateTextField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        // Initialize buttons and set font
        JButton addButton = new JButton("Add to Order");
        JButton removeButton = new JButton("Remove Order");
        JButton resetButton = new JButton("Reset Order");
        JButton calculateButton = new JButton("Calculate Total");
        JButton orderButton = new JButton("ORDER");

        addButton.setFont(font);
        removeButton.setFont(font);
        resetButton.setFont(font);
        calculateButton.setFont(font);
        orderButton.setFont(font);

        // Initialize text area and set font and colors
        orderTextArea = new JTextArea(15, 30);
        orderTextArea.setFont(font);
        orderTextArea.setEditable(false);
        orderTextArea.setBackground(Color.LIGHT_GRAY);
        orderTextArea.setForeground(Color.BLACK);

        // Initialize label and set font
        totalLabel = new JLabel("Total: P0.00");
        totalLabel.setFont(font);

        // Setup main panel with BorderLayout
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        // Setup input panel with Grid layout
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.GRAY);
        inputPanel.add(new JLabel("Category:")).setForeground(Color.WHITE);
        inputPanel.add(categoryComboBox);
        inputPanel.add(new JLabel("Item:")).setForeground(Color.WHITE);
        inputPanel.add(itemComboBox);
        inputPanel.add(new JLabel("Amount:")).setForeground(Color.WHITE);
        inputPanel.add(amountTextField);
        inputPanel.add(new JLabel("Date:")).setForeground(Color.WHITE);
        inputPanel.add(dateTextField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);
        inputPanel.add(resetButton);
        inputPanel.add(calculateButton);
        inputPanel.add(orderButton);

        // Add input panel to the top, order text area to the center, and total label to the bottom of main panel
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(orderTextArea), BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);

        // Add main panel to the frame
        add(panel);
    }

    // Setup database connection
    private void setupDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/xumenu", "root", "Brett#2004");

            // Insert admin user if not exists
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO admin (username, password) VALUES (?, ?) ON DUPLICATE KEY UPDATE username=username")) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Prompt login dialog and authenticate user
    private void login() {
        JPanel loginPanel = new JPanel();
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        // Show login dialog
        int result = JOptionPane.showConfirmDialog(null, loginPanel, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticate(username, password)) {
                // If authentication successful, show the main application
                setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                // If authentication fails, retry login
                login();
            }
        } else {
            // If cancel button pressed, exit the application
            System.exit(0);
        }
    }

    // Authenticate user against the database
    private boolean authenticate(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM admin WHERE username = ? AND password = ?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Setup event handlers for components
    private void eventHandlers() {
        categoryComboBox.addActionListener(e -> updateItemComboBox());

        // Retrieve buttons from the input panel
        JButton addButton = (JButton) ((JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0)).getComponent(8);
        JButton removeButton = (JButton) ((JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0)).getComponent(9);
        JButton resetButton = (JButton) ((JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0)).getComponent(10);
        JButton calculateButton = (JButton) ((JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0)).getComponent(11);
        JButton orderButton = (JButton) ((JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0)).getComponent(12);

        // Add event listeners to buttons
        addButton.addActionListener(e -> addItemToOrder());
        removeButton.addActionListener(e -> removeItemFromOrder());
        resetButton.addActionListener(e -> resetOrder());
        calculateButton.addActionListener(e -> calculateTotal());
        orderButton.addActionListener(e -> placeOrder());
    }

    // Update item combo box based on selected category
    private void updateItemComboBox() {
        itemComboBox.removeAllItems();
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT item_name, price FROM items WHERE category_id = (SELECT category_id FROM categories WHERE category_name = ?)")) {
            ps.setString(1, selectedCategory);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemComboBox.addItem(rs.getString("item_name") + " - P" + rs.getDouble("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add selected item to the order
    private void addItemToOrder() {
        String selectedItem = (String) itemComboBox.getSelectedItem();
        if (selectedItem == null || selectedItem.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an item!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String itemName = selectedItem.split(" - ")[0];
        double price = getPrice(itemName);

        if (!orders.contains(itemName)) {
            orders.add(itemName);
            prices.add(0.0);
            amounts.add(0);
        }

        int index = orders.indexOf(itemName);
        amounts.set(index, amounts.get(index) + amount);
        prices.set(index, prices.get(index) + (price * amount));

        updateOrderTextArea();
    }

    // Remove last item from the order
    private void removeItemFromOrder() {
        if (orders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items to remove!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int lastIndex = orders.size() - 1;
        orders.remove(lastIndex);
        prices.remove(lastIndex);
        amounts.remove(lastIndex);

        updateOrderTextArea();
    }

    // Get price of the specified item from the database
    private double getPrice(String item) {
        double price = 0.0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT price FROM items WHERE item_name = ?")) {
            ps.setString(1, item);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    // Reset the order
    private void resetOrder() {
        orders.clear();
        prices.clear();
        amounts.clear();
        orderTextArea.setText("");
        totalLabel.setText("Total: P0.00");
    }

    // Calculate the total price of the order
    private void calculateTotal() {
        double total = 0;
        for (double price : prices) total += price;
        totalLabel.setText(String.format("Total: P%.2f", total));
    }

    // Place the order and insert it into the database
    private void placeOrder() {
        String date = dateTextField.getText();
        try {
            Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());

            for (int i = 0; i < orders.size(); i++) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO orders (order_date, item_name, amount, price) VALUES (?, ?, ?, ?)")) {
                    ps.setDate(1, sqlDate);
                    ps.setString(2, orders.get(i));
                    ps.setInt(3, amounts.get(i));
                    ps.setDouble(4, prices.get(i));
                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Order placed successfully!");
            resetOrder();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid date format! Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update the order text area to display current orders
    private void updateOrderTextArea() {
        orderTextArea.setText("");
        for (int i = 0; i < orders.size(); i++) {
            orderTextArea.append(String.format("%d.) %s[%d] = P%.2f\n", i + 1, orders.get(i), amounts.get(i), prices.get(i)));
        }
    }

    // Main method to start the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new XUMenu().setVisible(true);
        });
    }
}
