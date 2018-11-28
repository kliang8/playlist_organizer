import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Lab07
{
	public static Connection connection = null;

	public static void connect_to_database()
	{
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:playlist_organizer.db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
	}

	public static void create_warehouse_table()
	{
		table_size=0;
		boolean fail=false;
		//Connection connection = null;
		try
		{
			//connection = DriverManager.getConnection("jdbc:sqlite:TPCH.db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.executeUpdate("drop table if exists warehouse");
			statement.executeUpdate("create table warehouse (w_warehousekey decimal(3,0) not null, w_name char(25) not null, w_supplierkey decimal(2,0) not null, w_capacity decimal(6,2) not null, w_address varchar(40) not null, w_nationkey decimal(2,0) not null)");
		}
		catch(SQLException e)
		{
			fail=true;
			System.err.println(e.getMessage());
		}
		if(fail==false)
		{
			System.out.println("Table created");
		}
	}

	public static void add_to_warehouse_table()
	{
		boolean fail=false;
		String input_name,input_supplier,input_address,input_nation,input_capacity;
		int w_supplierkey=0,w_nationkey=0;
		Scanner input = new Scanner(System.in);
		
		System.out.print("Name: ");
		input_name = input.nextLine();

		System.out.print("Supplier: ");
		input_supplier = input.nextLine();

		System.out.print("Capacity: ");
		input_capacity = input.nextLine();

		System.out.print("Address: ");
		input_address = input.nextLine();

		System.out.print("Nation: ");
		input_nation = input.nextLine();
		int w_warehousekey=0;
		String w_name=input_name;
		String w_capacity=input_capacity;
		String w_address=input_address;
		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery("select s_suppkey,n_nationkey from supplier,nation where s_name like '"+input_supplier+"' and n_name like '"+input_nation+"'");
			while(rs.next())
			{
				w_supplierkey=rs.getInt("s_suppkey");
				w_nationkey=rs.getInt("n_nationkey");
			}
			if(w_supplierkey==0 && w_nationkey==0)
			{
				System.out.println("Invalid supplier and/or nation");
				fail=true;
			}
			else
			{
				table_size++;
				w_warehousekey=table_size;
				statement.executeUpdate("insert into warehouse values ("+w_warehousekey+",'"+w_name+"',"+w_supplierkey+","+w_capacity+",'"+w_address+"',"+w_nationkey+")");
			}
		}
		catch(SQLException e)
		{
			fail=true;
			System.err.println(e.getMessage());
		}
		if(fail==false)
		{
			System.out.println("Warehouse added");
		}
	}

	public static void find_smallest_supplier()
	{
		//find supplier with samllest num of warehouses
		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery("select s_name,count(w_warehousekey) as cnt from supplier,warehouse where s_suppkey=w_supplierkey group by s_name having cnt=(select count(w_warehousekey) as c from warehouse,supplier where s_suppkey=w_supplierkey group by s_name order by c desc limit 1)");
			while(rs.next())
			{
				System.out.println(rs.getString("s_name"));
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
	}

	public static void find_max_warehouse_capacity()
	{
		//find max warehouse capacity across all suppliers
		int max=0;
		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery("select max(w_capacity) as max_c from warehouse");
			while(rs.next())
			{
				max=rs.getInt("max_c");
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		System.out.println("max capacity is " + max);
	}

	public static void find_europe_warehouses_smaller_than_input()
	{
		Scanner input = new Scanner(System.in);
		System.out.print("Enter capacity: ");
		int input_capacity=input.nextInt();
		//find warehouses in europe smaller than input capcity
		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery("select w_name from warehouse,nation,region where w_nationkey=n_nationkey and n_regionkey=r_regionkey and r_name='EUROPE' and w_capacity<"+input_capacity);
			while(rs.next())
			{
				System.out.println(rs.getString("w_name"));
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
	}

	public static void determine_if_warehouse_can_fit_capacity()
	{
		//determine if warehouses from a supplier can fit its capacity
		Scanner input = new Scanner(System.in);
		String input_supplier;
		System.out.print("Enter supplier name: ");
		input_supplier=input.nextLine();
		int capacity=0;
		int quantity=0;
		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery("select (select sum(w_capacity) from warehouse where s.s_suppkey=w_supplierkey) as c,(select sum(ps_availqty) from partsupp where s.s_suppkey=ps_suppkey) as qty from supplier s,warehouse w,partsupp ps where s_suppkey=w_supplierkey and ps_suppkey=s_suppkey and s_name like '"+input_supplier+"'group by s_name");
			while(rs.next())
			{
				capacity=rs.getInt("c");
				quantity=rs.getInt("qty");
			}
			if(capacity==0 && quantity==0)
			{
				System.out.println("Supplier not found");
			}
			else
			{
				if(capacity>=quantity)
				{
					System.out.println("This supplier's warehouses can hold the quantity.");
				}
				else
				{
					System.out.println("This supplier's warehouses cannot hold the quantity.");
				}
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
	}

	public static void disconnect_from_database()
	{
		try
		{
			if(connection!=null)
			{
				connection.close();
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
	}

	
	

	public static void add_user()
	{	//Manage user : add or delete
		boolean fail = false;
		String input_username, input_fullname, input_age, input_country, input_admin, input_password
		int u_userID = 0;
		Scanner input = new Scanner(System.in);

		System.out.print("Username: ");
		input_username = input.nextLine();

		System.out.print("Password: ");
		input_password = input.nextLine();		

		System.out.print("Fullname: ");
		input_fullname = input.nextLine();

		System.out.print("Age: ");
		input_age = input.nextLine();

		System.out.print("Country: ");
		input_country = input.nextLine();

		System.out.print("Admin Power: Y/N ");
		input_admin = input.nextLine();		

		try
		{
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30);
				ResultSet rs = statement.executeQuery("select u_userID from users where u_username like '"+input_username+"' ");
				
				while(rs.next())
				{
					u_userID = rs.getInt("u_userID");
				}
				if(u_userID != 0)
				{
					System.out.println("Username Already Exists");
					fail = true;
				}
				else
				{
					statement.executeUpdate("insert into users(u_username, u_fullname, u_age, u_country, u_admin, u_password) values ("+u_username+",'"+u_fullname+"',"+u_age+","+u_country+",'"+u_admin+"',"+u_password+")");
				}
			}
			catch(SQLException e)
			{
				System.err.println(e.getMessage());
			}
			if(fail == false)
			{
				System.out.println("User added");
			}

	}


	public static void add_song()
	{	
		boolean fail = false;
		String input_songname, input_artistname, input_albumname, input_genrename, input_language;
		int u_userID = 0;
		Scanner input = new Scanner(System.in);

		System.out.print("Song: ");
		input_songname = input.nextLine();

		System.out.print("Artist: ");
		input_artistname = input.nextLine();	

		System.out.print("Album: ");
		input_albumname = input.nextLine();

		System.out.print("Genre: ");
		input_genrename = input.nextLine();
		
		System.out.print("Language ");
		input_lanuage = input.nextLine();

		try
		{
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30);
				ResultSet artistSet = statement.executeQuery("select a")
				ResultSet rs = statement.executeQuery("select u_userID from users where u_username like '"+input_username+"' ");
				
				while(rs.next())
				{
					u_userID = rs.getInt("u_userID");
				}
				if(u_userID != 0)
				{
					System.out.println("Username Already Exists");
					fail = true;
				}
				else
				{
					statement.executeUpdate("insert into users(u_username, u_fullname, u_age, u_country, u_admin, u_password) values ("+u_username+",'"+u_fullname+"',"+u_age+","+u_country+",'"+u_admin+"',"+u_password+")");
				}
			}
			catch(SQLException e)
			{
				System.err.println(e.getMessage());
			}
			if(fail == false)
			{
				System.out.println("User added");
			}

	}



	// public static void delete_user()
	// {	//Manage user : add or delete
	// 	boolean fail = false;
	// 	String input_username, input_password
	// 	int u_userID = 0;
	// 	Scanner input = new Scanner(System.in);

	// 	System.out.print("Username: ");
	// 	input_username = input.nextLine();

	// 	System.out.print("Password: ");
	// 	input_password = input.nextLine();		
	

	// 	try
	// 	{
	// 			Statement statement = connection.createStatement();
	// 			statement.setQueryTimeout(30);
	// 			ResultSet rs = statement.executeQuery("select u_userID from users where u_username like '"+input_username+"' ");
				
	// 			while(rs.next())
	// 			{
	// 				u_userID = rs.getInt("u_userID");
	// 			}
	// 			if(u_userID != 0)
	// 			{
	// 				System.out.println("Username Already Exists");
	// 				fail = true;
	// 			}
	// 			else
	// 			{
	// 				statement.executeUpdate("insert into users(u_username, u_fullname, u_age, u_country, u_admin, u_password) values ("+u_username+",'"+u_fullname+"',"+u_age+","+u_country+",'"+u_admin+"',"+u_password+")");
	// 			}
	// 		}
	// 		catch(SQLException e)
	// 		{
	// 			System.err.println(e.getMessage());
	// 		}
	// 		if(fail == false)
	// 		{
	// 			System.out.println("User added");
	// 		}

	// }
	
	
	
	public static void main(String[] args)
	{
		Scanner input = new Scanner(System.in);
		int exit=0;
		connect_to_database();
		System.out.print("username: ");
		String username=input.nextLine();
		System.out.print("password");
		String password=input.nextLine();
		try
		{
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select u_userID, u_admin from users where u_username='"+username+"' and password='"+password"'");
			int rs_u_userID=0;
			//verify the user
			while(rs.next())
			{
				rs_u_userID=rs.getInt("u_userID");
				rs_u_admin=rs.getInt("u_admin");
			}
			if(rs_userID==0)
			{
				System.out.println("Invalid username or password");
				exit=1;
			}
			else
			{
				
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		while(exit==0)
		{

		}
		//closing the connection
		disconnect_from_database();
	}
}
