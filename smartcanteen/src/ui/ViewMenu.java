package ui;
import javax.swing.*;
import java.awt.*;
import dbo.menudb;
import model.menu;
public class ViewMenu extends JFrame {
    private menudb menudb;
    public ViewMenu() {
        menudb= new menudb();
        menu[] items = menudb.getAvailableMenuItems();
        
 }

}
