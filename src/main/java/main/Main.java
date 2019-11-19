package main;

import commands.*;
import controller.Server;
import model.Map;
import connection.ServerConnection;

import java.io.IOException;

import org.apache.commons.cli.*;

public final class Main {

    /**
     * Main method which is given the command line arguments and serves as an entry point for the whole application.
     *
     * @param args array of command line arguments
     */
    public static void main(String[] args) throws IOException, org.apache.commons.cli.ParseException {
        //prepare commandline options
        Options options = new Options()
                .addOption(Option.builder("port")
                        .desc("port the server runs on")
                        .hasArg(true)
                        .numberOfArgs(1)
                        .required(true)
                        .type(Integer.TYPE)
                        .build())
                .addOption(Option.builder("seed")
                        .desc("seed for random generators")
                        .hasArg(true)
                        .numberOfArgs(1)
                        .required(true)
                        .type(Long.TYPE)
                        .build())
                .addOption(Option.builder("timeout")
                        .desc("command arrival deadline (in ms)")
                        .hasArg(true)
                        .numberOfArgs(1)
                        .required(true)
                        .type(Integer.TYPE)
                        .build())
                .addOption(Option.builder("map")
                        .desc("path to map (JSON file)")
                        .hasArg(true)
                        .numberOfArgs(1)
                        .required(true)
                        .type(String.class)
                        .build());

        //parse the commandline
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        //extract options
        int port = Integer.parseInt(cmd.getOptionValue("port"));
        long seed = Long.parseLong(cmd.getOptionValue("seed"));
        int timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
        String path = cmd.getOptionValue("map");

        //create needed server components
        OwnCommandFactory cmdFac = new OwnCommandFactory();
        ServerConnection<Command> srvcon = new ServerConnection<>(port, timeout, cmdFac);

        Map map = Map.fromJson(path);

        Server server = new Server(map, seed, srvcon);

        server.startGame();
    }

}
