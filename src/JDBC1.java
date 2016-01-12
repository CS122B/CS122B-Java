import com.mysql.jdbc.*;

import javax.xml.crypto.Data;
import java.net.ConnectException;
import java.sql.*;                              // Enable SQL processing
import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

public class JDBC1
{
    private static Connection connection = null;
    private static Scanner in = null;

    public static int getUserChoice(String[] choices) {
        int choice = -1;

        System.out.println("Please enter a choice: ");
        for (int i = 0; i < choices.length; ++i) {
            System.out.format("%d: %s\n", i, choices[i]);
        }

        if (in.hasNextInt()) {
            choice = in.nextInt();
        }

        while (choice < 0 || choice > choices.length) {
            System.out.println("Invalid choice. Please enter a choice: ");

            in.next();
            if (in.hasNextInt()) {
                choice = in.nextInt();
            }
        }

        return choice;
    }

    public static void printResults(ResultSet result, String[] colNames) throws SQLException {
        System.out.println("\n------------------------");
        System.out.println("Query Results");
        System.out.println("------------------------\n");

        int rowCount = 0;

        while (result.next())
        {
            for (int i = 0; i < colNames.length; ++i) {
                if (!result.getString(i+1).equals("")) {
                    System.out.format("%s = %s\n", colNames[i], result.getString(i+1));
                }
            }
            System.out.println();
            ++rowCount;
        }

        System.out.println("Total Rows: " + rowCount);
        System.out.println("------------------------");
    }

    public static void getStar() throws Exception {
        String[] choices = {
            "Find by first name AND last name",
            "Find by first name OR last name",
            "Find by ID"
        };
        int choice = getUserChoice(choices);
        String query;
        PreparedStatement statement = connection.prepareStatement("");

        System.out.format("Choice Selected: %d [%s]\n", choice, choices[choice]);

        switch (choice) {
        case 0:
            query = (
                "SELECT * FROM movies as m " +
                "JOIN stars_in_movies as sim " +
                "ON m.id = sim.movie_id " +
                "WHERE sim.star_id = ( " +
                "    SELECT id " +
                "    FROM stars s " +
                "    WHERE s.first_name = ? " +
                "    AND s.last_name = ? " +
                "    LIMIT 1 " +
                ")"
            );
            statement = connection.prepareStatement(query);

            System.out.print("First name: ");
            statement.setString(1, in.next());
            System.out.print("Last name: ");
            statement.setString(2, in.next());
            break;
        case 1:
            query = (
                "SELECT * FROM movies as m " +
                "JOIN stars_in_movies as sim " +
                "ON m.id = sim.movie_id " +
                "WHERE sim.star_id = ( " +
                "    SELECT id " +
                "    FROM stars s " +
                "    WHERE s.first_name = ? " +
                "    OR s.last_name = ? " +
                "    LIMIT 1 " +
                ")"
            );
            statement = connection.prepareStatement(query);

            System.out.print("First name: ");
            statement.setString(1, in.next());
            System.out.print("Last name: ");
            statement.setString(2, in.next());
            break;
        case 2:
            query = (
                "SELECT * FROM movies as m " +
                "JOIN stars_in_movies as sim " +
                "ON m.id = sim.movie_id " +
                "WHERE sim.star_id = ?"
            );
            statement = connection.prepareStatement(query);

            System.out.print("ID: ");
            statement.setString(1, in.next());
            break;
        default:
        }

        ResultSet result = statement.executeQuery();
        String[] colNames = {
                "Id",
                "Title",
                "Year",
                "Director",
                "Banner URL",
                "Trailer URL"
        };
        printResults(result, colNames);
    }

    public static void addNewStar() throws Exception {
        System.out.println("Please enter the actor's name: ");
        String name = in.nextLine();
        String[] names = name.split(" ");
        String firstName = "";
        String lastName;
        if (names.length == 1) {
            lastName = names[0];
        }
        else {
            firstName = names[0];
            lastName = names[1];
        }

        // Create an execute an SQL statement to insert new record into stars
        String insertString = "insert into stars (first_name, last_name) values (?, ?)";
        PreparedStatement insertStar = connection.prepareStatement(insertString);
        insertStar.setString(1, firstName);
        insertStar.setString(2, lastName);
        insertStar.executeUpdate();
    }

    public static void addNewCustomer() throws Exception{
        System.out.println("First name:");
        String firstName = in.nextLine();
        System.out.println("Last name:");
        String lastName = in.nextLine();
        System.out.println("Credit Card #:");
        String creditCard = in.nextLine();
        System.out.println("Address: ");
        String address = in.nextLine();
        System.out.println("Email:");
        String email = in.nextLine();
        System.out.println("Password:");
        String password = in.nextLine();

        // Create an execute an SQL statement to select credit card record if available
        String selectString = "select count(*) from creditcards where id = ?";
        PreparedStatement selectCC = connection.prepareStatement(selectString);
        selectCC.setString(1, creditCard);
        ResultSet result = selectCC.executeQuery();

        boolean validCC = false;
        while(result.next()) {
            if (result.getInt(1) > 0)
                validCC = true;
        }

        if (validCC) {
            String insertString = "insert into customers(first_name, last_name, cc_id, address, email, password)" +
                    "values (?,?,?,?,?,?)";
            PreparedStatement insertCustomer = connection.prepareStatement(insertString);
            insertCustomer.setString(1,firstName);
            insertCustomer.setString(2,lastName);
            insertCustomer.setString(3,creditCard);
            insertCustomer.setString(4,address);
            insertCustomer.setString(5,email);
            insertCustomer.setString(6,password);
            insertCustomer.executeUpdate();
        }
        else {
            System.out.println("Invalid credit card. Please try again.");
            addNewCustomer();
        }
    }

    public static void deleteCustomer() throws SQLException {
        System.out.println("Email:");
        String email = in.nextLine();
        System.out.println("Password:");
        String password = in.nextLine();
        String query = (
            "DELETE FROM customers " +
            "WHERE email = ? AND password = ?"
        );
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, email);
        statement.setString(2, password);

        int returnId = statement.executeUpdate();
        if (returnId == 0) {
            System.out.println("Incorrect email or password.");
        } else {
            System.out.format("User with email `%s` was deleted successfully.\n", email);
        }
    }

    private static void _getTableMetadata(String tableName) throws SQLException {
        System.out.println("\n------------------------");
        System.out.format("Table name: `%s`\n", tableName);
        System.out.println("------------------------\n");

        String query = "SELECT * FROM " + tableName;
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        ResultSetMetaData metadata = result.getMetaData();

        System.out.println("There are " + metadata.getColumnCount() + " columns");

        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            String colName = metadata.getColumnName(i);
            String colType = metadata.getColumnTypeName(i);
            System.out.format("`%s` = %s\n", colName, colType);
        }
    }

    public static void getDatabaseMetadata() throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        ResultSet result = md.getTables(null, null, "%", null);
        while (result.next()) {
            _getTableMetadata(result.getString(3));
        }
    }

    public static void main(String[] arg) throws Exception
    {
        try {
            // Incorporate mySQL driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql:///moviedb", "root", "P@ssword1");
            in = new Scanner(System.in);

//            getStar();
//            addNewStar();
//            addNewCustomer();
//            deleteCustomer();
            getDatabaseMetadata();
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Connection Error: could not connect to the given database.");
        } catch (SQLException e) {
            System.out.println("Access Error: incorrect username or password.");
        }
    }
}
