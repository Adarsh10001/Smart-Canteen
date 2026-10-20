
package ui;

import javax.swing.*;
import java.awt.*;
import model.Student;
import dbo.menudb;
import model.menu;
public class StudentDashboard extends JFrame {
    private Student student;

    public StudentDashboard(Student student) {
        this.student = student;
        setTitle("Smart Canteen - Student Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JLabel welcomeLabel = new JLabel(
            "Welcome, " + student.getName(),
            SwingConstants.CENTER
        );
        JButton menuButton = new JButton("View Menu");
        JButton orderButton = new JButton("Place Order");
        JButton logoutButton = new JButton("Logout");
        JButton myOrdersButton = new JButton("View My Orders");

        JPanel panel = new JPanel(new GridLayout(10, 1, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(40, 80, 40, 80)
        );
        
        panel.add(welcomeLabel);
        panel.add(menuButton);
        panel.add(orderButton);
        panel.add(myOrdersButton);
        panel.add(logoutButton);
        add(panel);
        menuButton.addActionListener(e -> {
            menudb menuDB = new menudb();
            menu[] items = menuDB.getAvailableMenuItems();

            JList<menu> menuList = new JList<>(items);
            menuList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
            );

            JScrollPane scrollPane = new JScrollPane(menuList);

            int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Available Food Items",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                menu selectedItem = menuList.getSelectedValue();

                if (selectedItem != null) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Selected: " + selectedItem.getItemName()
                        + "\nPrice: ₹" + selectedItem.getPrice()
                    );
                }
            }
        });
        orderButton.addActionListener(e -> {
            Orderscreen orderFrame = new Orderscreen(student);
            orderFrame.setVisible(true);
            });
        logoutButton.addActionListener(e -> {
                new Login().setVisible(true);
                dispose();
            });
        myOrdersButton.addActionListener(e -> {
            Myorders frame = new Myorders(student);
            frame.setVisible(true);
        });
        }

    }
