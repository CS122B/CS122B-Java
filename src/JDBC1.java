/**
 * Created by Van on 1/10/2016.
 */

import java.sql.*;
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

    public static void main(String[] arg) throws Exception
    {
        // Incorporate mySQL driver
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        connection = DriverManager.getConnection("jdbc:mysql:///moviedb","root", "P@ssword1");
        in = new Scanner(System.in);
        getStar();
    }
}
