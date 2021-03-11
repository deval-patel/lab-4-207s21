/*
    Author: daffy
    Purpose: 207 Lab 4 Simple File IO Practice
 */

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Events {
    // Our file that we want to use for this
    private static File events;
    private static String fileName = "events.txt";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    private static void fileOpener() {
        // Step 1: Read file if exists, otherwise create a new file to be used to store
        // And remember file creation could fail, so surround with try catch statements
        // NOTE: While it is not necessary to have this, its good to just have the file even if its empty
        // Or an alternative is when reading, you have another try catch to see if the file exists or not
        events = new File(fileName);
        if (!events.exists()){
            try{
                if(events.createNewFile()){
                    System.out.println("Created a new file " + fileName);
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }


    }

    private static boolean lineWriter(String date, String event) {
        // When writing, it is generally a good idea to not conflict with another person potentially writing
        // Usually wont happen with assignment but better to be safe than sorry :)

        // Step 2: Check if event is empty, as an empty event is useless and should not be added to the file
        if (event.equals("")) return false;
        // if (event.isEmpty()) return false;
        // Step 3: Using RandomAccessFile & FileLocker, write to it, if can't be locked, return false
        try (RandomAccessFile reader = new RandomAccessFile(fileName, "rw");
            FileLock ignored = reader.getChannel().lock()) {

            // Step 3.1: Format our string to write to the file with yyyy-mm-dd text
            String toWrite = String.format("%s %s\n", date, event);
            // Step 3.2: Seek to the end of the file BEFORE writing else you will be overwriting it
            reader.seek(reader.length());
            // Step 3.3 Write your data to the file
            // reader.write(toWrite.getBytes(StandardCharsets.UTF_8));
            reader.writeChars(toWrite);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Return true if the date inputted is valid */
    private static boolean dateVerifier(String date) {
        // Create a format for our date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    // Line parsing to match our format
    private static ArrayList<String> lineParser(String data) {
        // Pattern Matching
        String pattern = "(\\d{4}-\\d{2}-\\d{2}) (.*)";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(data);

        ArrayList<String> returnString = new ArrayList<String>();
        if (m.find()) {
            returnString.add(m.group(1));
            returnString.add(m.group(2));
        }
        return returnString;
    }

    private static ArrayList<String> searchForEvent(String date) {
        // Step 4: Verify Date, return null if invalid
        if (!dateVerifier(date)) return null;

        // To search through a file, you will need to either re-read from the file again
        // Or during load, parse into a variable and at the end of your program save
        // But in our case, we will be re-reading and updating
        try {
            // Step 5: Create a BufferedReader to read our file
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            // Step 6: Create an event list to store and return
            ArrayList<String> events = new ArrayList<String>();
            // Step 7 Create a String variable to store to read.
            String line;
            // Step 8: Loop through the file and read each line then pass it to lineParser function
            // Step 8.1: If date matches then append to your events list
            System.out.println("Here");
            while((line = br.readLine()) != null){
                ArrayList<String> event = lineParser(line.trim());
                // This event matches the given date
                if (event.get(0).equals(date)){
                    events.add(event.get(1));
                }
            }

            // Step 9: return your final list!
            return events;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }


    private static boolean addEvent(String date, String event) {
        // Step 10: Verify Date, return null if invalid
        if (!dateVerifier(date)) return false;
        // Step 11: Write to file
        return lineWriter(date, event);
    }

    // Your main execution, code completed
    public static void main(String args[]) {

        fileOpener();

        System.out.println(ANSI_YELLOW + "Commands [commands]:");
        System.out.println("\tTo Search using Dates [s]");
        System.out.println("\tTo Add an event/reminder [a]");
        System.out.println("\tTo quit [q]" + ANSI_RESET);
        while (true) {
            System.out.printf("Enter your command: ");
            Scanner obj = new Scanner(System.in);
            String input = obj.nextLine().trim();
            if (input.equals("q")) {
                System.out.println("Exiting...");
                return;
            } else if (input.equals("s")) {
                System.out.printf("Which day of the event would you like to grab? [yyyy-mm-dd]: ");
                Scanner dateInputter = new Scanner(System.in);
                String date = dateInputter.nextLine().trim();
                ArrayList<String> events;
                if ((events = searchForEvent(date)) != null) {
                    if (events.size() == 0) {
                        System.out.println(ANSI_RED + "No events found \uD83D\uDE10" + ANSI_RESET);
                    } else {
                        System.out.println(String.format("Here are your events on %s:", date));
                        for (String s: events) {
                            System.out.println(s);
                        }
                    }

                } else {
                    System.out.println(ANSI_RED + "Invalid Date \uD83D\uDE10" + ANSI_RESET);
                }
            } else if (input.equals("a")) {
                System.out.printf("Please input the date you would like to add an event for [yyyy-mm-dd]: ");
                Scanner dateInputter = new Scanner(System.in);
                String date = dateInputter.nextLine().trim();
                System.out.printf("What is the event/reminder? ");
                Scanner eventInputter = new Scanner(System.in);
                String event = eventInputter.nextLine().trim();
                if (addEvent(date, event)) {
                    System.out.println("Event added!");
                } else {
                    System.out.println("Invalid input \uD83D\uDE10");
                }
            }
        }
    }

}
