/**
 * Created by Van on 1/10/2016.
 */

import java.sql.*;                              // Enable SQL processing
import java.io.*;

public class JDBC1
{
    private static Connection connection = null;

    public static void getStar() throws Exception {
        // Connect to the test database
        connection = DriverManager.getConnection("jdbc:mysql:///moviedb","root", "P@ssword1");

        // Create an execute an SQL statement to select all of table"Stars" records
//        Statement select = connection.createStatement();
        int choice = 0;
        String statement
        switch (choice) {
            case 0:
        }
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

    public static void main(String[] arg) throws Exception
    {

        // Incorporate mySQL driver
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        getStar();
    }
}
