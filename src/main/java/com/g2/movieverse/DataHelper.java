package com.g2.movieverse;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DataHelper {
    public static void getGenres(String outputFile){
        String query = "select genre from p320_02.movie_data7;";
        Set set = new TreeSet<String>();

        try{
            ResultSet rs = ConnectionManager.sendQuery(query);
            while (rs.next())
            {
                String[] genres = rs.getString(1).split(",");

                for (String genre : genres)
                    set.add(genre.trim());
            }

            //validate
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            Iterator<String> it = set.iterator();
            while(it.hasNext()) {
                String current = it.next();
                System.out.println(current);
                writer.write(current + "\n");
            }

            rs.close();
            writer.close();
         } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void convertLengthToMin(String outputFile){
        String query = "select title, length from p320_02.movie_data7;";
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            ResultSet rs = ConnectionManager.sendQuery(query);
            while (rs.next())
            {
                String title = rs.getString(1);
                String[] len = rs.getString(2).split(" ");

                if (len.length == 1)
                {
                    int index = len[0].indexOf('h');
                    int runtime = 0;
                    if (index != -1)
                        runtime = Integer.parseInt(len[0].substring(0, index)) * 60;
                    else if ((index = len[0].indexOf('m')) != -1)
                        runtime = Integer.parseInt(len[0].substring(0, index));
                    System.out.println(title + ": " + runtime);
                    writer.write(title+","+runtime+"\n");
                }
                else
                {
                    // Must be both
                    int index = len[0].indexOf('h');
                    int runtime = Integer.parseInt(len[0].substring(0, index)) * 60;
                    runtime += Integer.parseInt(len[1].substring(0, len[1].indexOf('m')));

                    System.out.println(title + ": " + runtime);
                    writer.write(title+","+runtime +"\n");
                }
            }
            writer.close();
            rs.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void getCastMember(String outputFile) {
        String query = "select director from p320_02.movie_data7;";
        BufferedWriter writer = null;
        TreeSet<String> set = new TreeSet();

        try {
            writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            ResultSet rs = ConnectionManager.sendQuery(query);
            while (rs.next())
            {
                String[] names = rs.getString(1).split(",");

                for (String name : names)
                    set.add(name.trim());
            }

            Iterator<String> it = set.iterator();
            while(it.hasNext()) {
                String current = it.next();

                String[] temp = current.trim().split(" ");

                if (temp.length == 1)
                    writer.write(temp[0].trim() + "\n");
                else if (temp.length == 2)
                    writer.write(temp[0].trim() + "," + temp[1].trim() + "\n");
                else if (temp.length == 3)
                    writer.write(temp[0].trim() + "," + temp[2].trim() + "\n");
            }

            writer.close();
            rs.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void getStudios(String outputFile){
        String query = "select studio from p320_02.movie_data7;";
        Set set = new TreeSet<String>();

        try{
            ResultSet rs = ConnectionManager.sendQuery(query);
            while (rs.next())
            {
                String[] studios = rs.getString(1).split(",");

                for (String studio : studios)
                    set.add(studio.trim());
            }

            //validate
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            Iterator<String> it = set.iterator();
            while(it.hasNext()) {
                String current = it.next();
                System.out.println(current);
                writer.write(current + "\n");
            }

            rs.close();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void combinePeople(String directorFile, String castMemberFile, String outputFile)
    {
        Set set = new TreeSet<String>();

        try{
            BufferedReader directorReader = new BufferedReader(new FileReader(new File(directorFile)));
            BufferedReader castReader = new BufferedReader(new FileReader(new File(castMemberFile)));

            String line;
            while ((line = directorReader.readLine()) != null)
                set.add(line);

            while ((line = castReader.readLine()) != null)
                set.add(line);

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            Iterator<String> it = set.iterator();
            while(it.hasNext()) {
                String current = it.next();
                writer.write(current + "\n");
            }

            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void getGenrePairings(String outputFile)
    {
        String query = "select title, genre from p320_02.movie_data7;";
        String query2 = "select name, genre_id from p320_02.genre;";

        HashMap<String, Integer> genreMap = new HashMap<>();

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            ResultSet rs = ConnectionManager.sendQuery(query2);

            // Build hashmap
            while (rs.next()) {
                genreMap.put(rs.getString(1), rs.getInt(2));
            }
            rs.close();

            rs = ConnectionManager.sendQuery(query);
            while (rs.next())
            {
                String title = rs.getString(1);
                String[] genres = rs.getString(2).split(",");

                for (String genre : genres)
                    writer.write(title + "," + genreMap.get(genre.trim()) +"\n");
            }

            rs.close();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void getStudioPairings(String outputFile)
    {
        String query = "select title, studio from p320_02.movie_data7;";
        String query2 = "select name, studio_id from p320_02.studio;";

        HashMap<String, Integer> studioMap = new HashMap<>();

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            ResultSet rs = ConnectionManager.sendQuery(query2);

            // Build hashmap
            while (rs.next()) {
                studioMap.put(rs.getString(1), rs.getInt(2));
            }
            rs.close();

            rs = ConnectionManager.sendQuery(query);
            while (rs.next())
            {
                String title = rs.getString(1);
                String[] studios = rs.getString(2).split(",");

                for (String studio : studios)
                    writer.write(title + "," + studioMap.get(studio.trim()) +"\n");
            }

            rs.close();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void getMovieCastMemberParings(String outputFile)
    {
        String query = "select title, director from p320_02.movie_data7;";
        String query2 = "select person_id, first_name, last_name from p320_02.person;";

        HashMap<String, Integer> peopleMap = new HashMap<>();

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
            ResultSet rs = ConnectionManager.sendQuery(query2);

            // Build hashmap
            while (rs.next()) {
                peopleMap.put(rs.getString(2) + " " + rs.getString(3), rs.getInt(1));
            }
            rs.close();

            rs = ConnectionManager.sendQuery(query);
            while(rs.next()) {
                String title = rs.getString(1);
                String[] names = rs.getString(2).trim().split(",");

                for (String name : names)
                {
                    String[] temp = name.trim().split(" ");
                    if (temp.length == 1) {
                        writer.write(title + "," + peopleMap.get(temp[0].trim()) + "\n");
                    }
                    else if (temp.length == 2) {
                        writer.write(title + "," + peopleMap.get(temp[0].trim() + " " + temp[1].trim()) + "\n");
                    }
                    else if (temp.length == 3) {
                        writer.write(title + "," + peopleMap.get(temp[0].trim() + " " + temp[2].trim()) + "\n");
                    }
                }
            }

            rs.close();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
