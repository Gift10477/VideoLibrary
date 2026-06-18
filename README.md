# VideoLibrary
Video Library System (VLS) 🎬
A distributed, client-server desktop application built to manage a video rental store. The system utilizes Java RMI (Remote Method Invocation) for network communication, JavaFX for a modern graphical user interface, and a persistent MySQL database.

Author: Gift  Gicheru Githaka

Institution: Strathmore University (Informatics and Computer Science)

📌 Project Overview

The Video Library System is designed with a strict Client-Server architecture operating over a Virtual Private Network (ZeroTier). It features two distinct user interfaces:

Admin Management Panel: For managing library inventory and user records.

Customer Portal: For users to browse inventory, rent movies, and manage their return history.

✨ Key Features
Distributed Architecture: Clients connect remotely to a central Java RMI Server, allowing real-time data synchronization across different physical machines.

Database Persistence: Full CRUD (Create, Read, Update, Delete) capabilities integrated with a centralized MySQL database.

Modern UI: Responsive, dark-themed JavaFX interface styled with custom CSS.

Role-Based Portals:

Admin: Register/Remove Genres, Register/Remove Movies, Register Customers.

Customer: Dynamic filtering of movies by genre, Renting capabilities, and Return History tracking.

🛠️ Technology Stack
Language: Java (JDK 21+)

GUI Framework: JavaFX

Networking: Java RMI (Remote Method Invocation), ZeroTier (Virtual LAN)

Database: MySQL (via XAMPP)

Database Connectivity: JDBC (MySQL Connector)

🚀 Setup & Installation Guide
1. Prerequisites
XAMPP installed and running (Apache & MySQL).

ZeroTier installed on all host and client machines.

Java Development Kit (JDK) and a Java IDE (IntelliJ IDEA recommended).

2. Database Configuration (Host Machine Only)
Open XAMPP and start the MySQL module.

Navigate to http://localhost/phpmyadmin in your web browser.

Create a new database named exactly vls_db.

Execute your SQL script to generate the required tables: Genres, Movies, Customers, and Rentals.

3. Network Configuration
Ensure all computers (Server and Clients) are connected to the same ZeroTier Network.

On the Host machine, open the command prompt and run zerotier-cli info to find your Node ID, or check your ZeroTier dashboard to find your specific Managed IP (e.g., 10.255.x.x).

Open HelloApplication.java on all machines and update the server connection string:

Java
private final String SERVER_IP = "YOUR_ZEROTIER_IP_HERE"; 
4. Running the Application
Step 1: Boot the Server (Host Machine)

Open IntelliJ on the machine hosting the XAMPP database.

Navigate to src/main/java/com/videolibrary/server/RmiServer.java.

Run the main method. Wait for the [Database] Successfully connected message in the console.

Step 2: Launch the Clients (Any Machine)

Open IntelliJ on any connected client machine.

Ensure the project is rebuilt (Build -> Rebuild Project) to register the IP address.

Navigate to src/main/java/com/videolibrary/Launcher.java and run the main method.

Select either the Admin or Customer interface from the welcome screen.

🛑 Troubleshooting
1. "Unknown database 'vls_db'" Error

Ensure XAMPP MySQL is running and that you have explicitly created the database named vls_db in phpMyAdmin on the host machine.

2. "java.rmi.UnknownHostException" or Connection Refused

Check the IP: Ensure the SERVER_IP variable in HelloApplication.java exactly matches the Host's ZeroTier IP (no missing numbers or letters).

Firewall: Windows Defender may block Java RMI. On the Host machine, open Windows Defender Firewall -> Advanced Settings -> Inbound Rules, and create a new rule allowing TCP Port 1099.

RMI Hostname: Ensure the RmiServer.java file contains the property override before starting the registry:

Java
System.setProperty("java.rmi.server.hostname", "YOUR_ZEROTIER_IP_HERE");
