package com.g2.movieverse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class PasswordLoader {

    /**
     *
     * @return String array of size 2 - index 0: username, index 1: password
     */
    public static String[] getCSAccount()
    {
        String[] output = null;

        try{
            File file = new File("src/account.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String temp = reader.readLine();
            output = temp.trim().split(",");

        }catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return output;
    }
}
