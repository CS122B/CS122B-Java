import java.sql.*;
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
    private static final boolean __DEV__ = true;

    public static void printHeader(String header) {
        System.out.println("------------------------");
        System.out.println(header);
        System.out.println("------------------------\n");
    }

    public static String getStringInput(String value) {
        System.out.print(value + ": ");
        return in.nextLine();
    }

    public static int getUserChoice(String[] choices) {
        int choice = -1;

        System.out.println("Please enter a choice: ");
        for (int i = 0; i < choices.length; ++i) {
            System.out.format("%d: %s\n", i+1, choices[i]);
        }

        if (in.hasNextInt()) {
            choice = in.nextInt() - 1;
        }

        while (choice < 0 || choice >= choices.length) {
            System.out.println("Invalid choice. Please enter a choice: ");

            in.nextLine();
            if (in.hasNextInt()) {
                choice = in.nextInt() - 1;
            }
        }
        in.nextLine();

        printHeader(choices[choice]);

        return choice;
    }

    public static void printResults(ResultSet result) throws SQLException {
        printHeader("Query Results");
        ResultSetMetaData metadata = result.getMetaData();

        int rowCount = 0;

        while (result.next()) {
            for (int i = 1; i <= metadata.getColumnCount(); ++i) {
                if (!result.getString(i).equals("")) {
                    System.out.format("%s = %s\n", metadata.getColumnName(i), result.getString(i));
                }
            }
            System.out.println();
            ++rowCount;
        }

        printHeader("Total Rows: " + rowCount);
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
        String firstName;
        String lastName;
        String ID;

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
            firstName = getStringInput("First name");
            lastName = getStringInput("Last name");
            statement.setString(1, firstName);
            statement.setString(2, lastName);

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
            firstName = getStringInput("First name");
            lastName = getStringInput("Last name");
            statement.setString(1, firstName);
            statement.setString(2, lastName);

            break;
        case 2:
            query = (
                "SELECT * FROM movies as m " +
                "JOIN stars_in_movies as sim " +
                "ON m.id = sim.movie_id " +
                "WHERE sim.star_id = ?"
            );
            statement = connection.prepareStatement(query);
            ID = getStringInput("ID");
            statement.setString(1, ID);
            break;
        default:
            break;
        }

        ResultSet result = statement.executeQuery();
        printResults(result);
    }

    public static void addNewStar() throws Exception {
        String name = getStringInput("Enter actor's name (first and/or last)");
        String[] names = name.split(" ");
        String firstName = "";
        String lastName;

        if (names.length == 1) {
            lastName = names[0];
        } else {
            firstName = names[0];
            lastName = names[1];
        }

        // Create an execute an SQL statement to insert new record into stars
        String insertString = (
            "INSERT INTO stars (first_name, last_name) " +
            "VALUES (?, ?)"
        );
        PreparedStatement insertStar = connection.prepareStatement(insertString);

        insertStar.setString(1, firstName);
        insertStar.setString(2, lastName);

        insertStar.executeUpdate();
    }

    public static void addNewCustomer() throws Exception{
        String firstName = getStringInput("First name");
        String lastName = getStringInput("Last name");
        String creditCard = getStringInput("Credit Card #");
        String address = getStringInput("Address");
        String email = getStringInput("Email");
        String password = getStringInput("Password");

        String selectString = (
            "SELECT count(*) " +
            "FROM creditcards " +
            "WHERE id = ?"
        );
        PreparedStatement selectCC = connection.prepareStatement(selectString);
        selectCC.setString(1, creditCard);
        ResultSet result = selectCC.executeQuery();

        boolean validCC = false;
        while(result.next()) {
            if (result.getInt(1) > 0)
                validCC = true;
        }

        if (validCC) {
            String query = (
                "INSERT INTO customers( " +
                "   first_name, last_name, cc_id, " +
                "   address, email, password " +
                ") VALUES (?, ?, ?, ?, ?, ?)"
            );
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1,firstName);
            statement.setString(2,lastName);
            statement.setString(3,creditCard);
            statement.setString(4,address);
            statement.setString(5,email);
            statement.setString(6,password);

            statement.executeUpdate();
        } else {
            System.out.println("Invalid credit card. Please try again.");
            addNewCustomer();
        }
    }

    public static void deleteCustomer() throws SQLException {
        String email = getStringInput("Email");
        String password = getStringInput("Password");

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
        printHeader("Table name: `" + tableName + "`");

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
        System.out.println();
    }

    public static void getDatabaseMetadata() throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        ResultSet result = md.getTables(null, null, "%", null);
        while (result.next()) {
            _getTableMetadata(result.getString(3));
        }
    }

    public static void initializeCredentials() throws SQLException {
        String database;
        String username;
        String password;

        if (__DEV__) {
            database = "jdbc:mysql:///moviedb";
            username = "root";
            password = "P@ssword1";
        } else {
            database = "jdbc:mysql:///" + getStringInput("Database");
            username = getStringInput("Username");
            password = getStringInput("Password");
        }
        connection = DriverManager.getConnection(database, username, password);

        printHeader("Connected to database.");
    }

    public static void runMenu() throws Exception {
        String[] menuChoices = {
            "Get star",
            "Add star",
            "Add customer",
            "Delete customer",
            "Get database metadata",
            "Enter SQL command",
            "Re-enter credentials",
            "Exit program"
        };
        int choice;
        boolean whileFlag = true;

        do {
            printHeader("Main Menu");
            choice = getUserChoice(menuChoices);

            switch(choice) {
            case 0:
                getStar();
                break;
            case 1:
                addNewStar();
                break;
            case 2:
                addNewCustomer();
                break;
            case 3:
                deleteCustomer();
                break;
            case 4:
                getDatabaseMetadata();
                break;
            case 5:
                break;
            case 6:
                initializeCredentials();
                break;
            case 7:
            default:
                whileFlag = false;
                break;
            }
        } while (whileFlag);

        in.close();
        connection.close();
    }

    public static void main(String[] arg) throws Exception
    {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            in = new Scanner(System.in);

            initializeCredentials();
            runMenu();
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Connection Error: could not connect to the given database.");
        } catch (SQLException e) {
            System.out.println("Access Error: incorrect username or password.");
        }
    }
}
