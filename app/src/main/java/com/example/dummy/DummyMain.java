package com.example.dummy;

import com.example.misc.JsonConverter;
import java.util.Scanner;

public class DummyMain {

    public static void main(String[] args) {
        String acc_typ;
        // get the account type (master or tenant)
        // if the user does not give valid info then ask again
        do {
            System.out.print("Manager/Tenant[M/t]: ");
            Scanner inp = new Scanner(System.in);
            acc_typ = inp.next().toLowerCase();
            if (!acc_typ.equals("m") && !acc_typ.equals("t")) System.out.println("[-] Invalid option. Please choose Manager or Tenant.");
        } while (!acc_typ.equals("m") && !acc_typ.equals("t"));

        // choose what to run based on account type
        if (acc_typ.equals("m")) Manager.runManager();
        else Tenant.runTenant();
    }
}
