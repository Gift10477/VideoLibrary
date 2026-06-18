package com.videolibrary.server;

import com.videolibrary.rmi.VlsServiceImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Dedicated server entry point to spin up the RMI registry.
 */
public class RmiServer {
    public static void main(String[] args) {
        try {
            // Start RMI Registry on default port 1099
            System.setProperty("java.rmi.server.hostname", "10.255.51.142");
            Registry registry = LocateRegistry.createRegistry(1099);
            VlsServiceImpl service = new VlsServiceImpl();

            // Bind remote object service stub
            registry.rebind("VlsService", service);
            System.out.println("========================================");
            System.out.println(" VLS Central Server is running smoothly ");
            System.out.println("========================================");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
