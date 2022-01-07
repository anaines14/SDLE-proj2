package main;

import main.controller.CommandExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestApp {
    private final CommandExecutor cmdExecutor;

    public TestApp() {
        this.cmdExecutor = new CommandExecutor();
    }

    public static void main(String[] args) {
        TestApp app = new TestApp();
        app.cmdExecutor.displayGraph();

        // check number of arguments
        if (args.length == 1) { // run test from file
            String filename = args[0];
            app.run_test(filename);
        }
        else { // run loop using user input
            app.run_loop();
        }
    }

    private void run_loop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a command (Type EXIT to end)...");
        String cmd = scanner.nextLine();
        while (!cmd.equalsIgnoreCase("EXIT")) {
            try {
                if (cmdExecutor.execCmd(cmd) != 0) {
                    usage();
                    System.exit(-1);
                }
                // get command
                cmd = scanner.nextLine();

            } catch (UnknownHostException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void run_test(String filename) {
        // fetch file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filename);

        try {
            assert resource != null;
            File testFile = new File(resource.toURI());
            Scanner scanner = new Scanner(testFile);
            do {
                // get command
                String cmd = scanner.nextLine();
                cmdExecutor.execCmd(cmd); // exec command
            } while(scanner.hasNextLine());

        } catch (FileNotFoundException | UnknownHostException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("""
                usage: TestApp.java <test_file>

                Avalable commands:


                \t START <username> <capacity>
                \t START_MULT <n>
                \t POST <username> "<content>"
                \t UPDATE <username> <post_id> "<content>"
                \t DELETE <username> <post_id>
                \t TIMELINE <username> <req_timeline>
                \t PRINT <username>
                \t PRINT_PEERS
                \t STOP <username>
                \t STOP_ALL
                \t BREAK
                \t SLEEP <seconds>""");
    }
}
