import java.sql.*;                              // Enable SQL processing
import java.io.*;
import java.util.Scanner;

public class JDBC1
{
    private static Connection connection = null;

    public static void getStar() throws Exception {
        // Connect to the test database
        connection = DriverManager.getConnection("jdbc:mysql:///moviedb","root", "cs122b");

        // Create an execute an SQL statement to select all of table"Stars" records
//        Statement select = connection.createStatement();
        int choice = 0;
//        String statement
//        switch (choice) {
//            case 0:
//        }
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM stars WHERE first_name = ? AND last_name = ?");
        statement.setString(1, "Kristin");
        statement.setString(2, "Kreuk");
        ResultSet result = statement.executeQuery();

        // Get metadata from stars; print # of attributes in table
        System.out.println("The results of the query");
        ResultSetMetaData metadata = result.getMetaData();
        System.out.println("There are " + metadata.getColumnCount() + " columns");

        // Print type of each attribute
        for (int i = 1; i <= metadata.getColumnCount(); i++)
            System.out.println("Type of column "+ i + " is " + metadata.getColumnTypeName(i));

        // print table's contents, field by field
        while (result.next())
        {
            System.out.println("Id = " + result.getInt(1));
            System.out.println("Name = " + result.getString(2) + result.getString(3));
            System.out.println("DOB = " + result.getString(4));
            System.out.println("photoURL = " + result.getString(5));
            System.out.println();
        }
    }

    public static void addNewStar() throws Exception {
        connection = DriverManager.getConnection("jdbc:mysql:///moviedb","root", "cs122b");
        Scanner in = new Scanner(System.in);
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

        // Create an execute an SQL statement to select all of table"Stars" records
        String insertString = "insert into stars (first_name, last_name) values (?, ?)";
        PreparedStatement insertStar = connection.prepareStatement(insertString);
        insertStar.setString(1, firstName);
        insertStar.setString(2, lastName);
        insertStar.executeUpdate();
    }

    public static void main(String[] arg) throws Exception
    {

        // Incorporate mySQL driver
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        //getStar();
        //addNewStar();
    }
}
