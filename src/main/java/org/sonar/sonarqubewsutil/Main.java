package org.sonar.sonarqubewsutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class Main {

    // General options
    private static final String URI_OPTION = "uri";
    private static final String USERNAME_OPTION = "username";
    private static final String PASSWORD_OPTION = "password";    
    private static final String HELP_OPTION = "help";
    
    // TimeMachine options
    private static final String FROM_DATETIME = "fromDateTime";
    private static final String TO_DATETIME = "toDateTime";

    // SonarQube JSON keys
    private static final String PROJECT_ID_KEY = "id";
    private static final String PROJECT_KEY_KEY = "k";
    private static final String TIME_MACHINE_CELLS_KEY = "cells";
    private static final String TIME_MACHINE_DATE_KEY = "d";
    private static final String TIME_MACHINE_VALUE_KEY = "v";

    public static void main(String args[]) throws ParseException, IOException {

        // SonarQube login info
        String uri = "http://localhost:9000";
        String username = null;
        String password = null;
        String fromDateTime = null;
        String toDateTime = null;

        // Get a list of arguments from command line.
        Options options = new Options();
        options.addOption(URI_OPTION, true, "server url");
        options.addOption(USERNAME_OPTION, true, "username");
        options.addOption(PASSWORD_OPTION, true, "password");
        options.addOption(HELP_OPTION, false, "usage information");
        options.addOption(FROM_DATETIME, true, "from datetime");
        options.addOption(TO_DATETIME, true, "to datetime");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(HELP_OPTION)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SonarQubeWSUtil", options);
            return;
        }
        if (cmd.hasOption(URI_OPTION)) {
            uri = cmd.getOptionValue(URI_OPTION);
        }
        if (cmd.hasOption(USERNAME_OPTION)) {
            username = cmd.getOptionValue(USERNAME_OPTION);
        }
        if (cmd.hasOption(PASSWORD_OPTION)) {
            password = cmd.getOptionValue(PASSWORD_OPTION);
        }
        if (cmd.hasOption(FROM_DATETIME)) {
            fromDateTime = cmd.getOptionValue(FROM_DATETIME);
        }
        if (cmd.hasOption(TO_DATETIME)) {
            toDateTime = cmd.getOptionValue(TO_DATETIME);
        }
            

        // Comma separated values:
        // - project: id, key,
        // - oldest measure: date, value
        // - newest measure: date, value
        String output = String.format("%s,%s,%s,%s,%s,%s",
                "project id", "project key",
                "metric date1", "metric value1",
                "metric date2", "metric value2");
        System.out.println(output);
        
        // Get a list of all projects.
        // See: http://nemo.sonarqube.org/api_documentation#api/projects
        String projectsJson = getSQJson(uri, username, password, "/projects");
        JSONArray projects = new JSONArray(projectsJson);
        int length = projects.length();        
        for (int i = 0; i < length; ++i) {
            JSONObject project = projects.getJSONObject(i);
            String id = project.getString(PROJECT_ID_KEY);
            String key = project.getString(PROJECT_KEY_KEY);

            // String output = String.format("%s,%s,", id, key);
            // System.out.println(output);
            
            // Get metric for a specific project using time machine
            // See: http://nemo.sonarqube.org/api_documentation#api/timemachine
            StringBuilder resource = new StringBuilder();
            resource.append("/timemachine?resource=").append(key).append("&metrics=sqale_index");
            
            // Add fromDateTime and toDateTime parameters
            if (fromDateTime != null && toDateTime != null)
            {
                resource.append("&").append(FROM_DATETIME).append("=").append(fromDateTime)
                        .append("&").append(TO_DATETIME).append("=").append(toDateTime);                 
            }            
            String metricsJson = getSQJson(uri, username, password, resource.toString());
                        
            JSONArray metrics = new JSONArray(metricsJson).getJSONObject(0).getJSONArray(TIME_MACHINE_CELLS_KEY);
            
            String oldestDate = "";
            int oldestValue = 0;
            String newestDate = "";
            int newestValue = 0;
            
            if (metrics != null && metrics.length() > 0) {
                JSONObject oldestMeasure = metrics.getJSONObject(0);
                oldestDate = oldestMeasure.getString(TIME_MACHINE_DATE_KEY);
                oldestValue = oldestMeasure.getJSONArray(TIME_MACHINE_VALUE_KEY).getInt(0);

                int lastIndex = metrics.length() - 1;
                if (lastIndex < 0) {
                    lastIndex = 0;
                }
                JSONObject newestMeasure = metrics.getJSONObject(lastIndex);
                newestDate = newestMeasure.getString(TIME_MACHINE_DATE_KEY);
                newestValue = newestMeasure.getJSONArray(TIME_MACHINE_VALUE_KEY).getInt(0);
            }

            output = String.format("%s,%s,%s,%s,%s,%s",
                    id, key,
                    oldestDate, oldestValue,
                    newestDate, newestValue);
            System.out.println(output);
        }
    }

    private static String getSQJson(
            final String uri,
            final String username,
            final String password,
            final String service) throws IOException {
        URL url = new URL(getApiPath(uri, service));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (username != null && !username.isEmpty()
                && password != null && !password.isEmpty()) {
            String authString = username + ":" + password;
            String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
            conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
        }

        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            // System.out.println(sb.toString());            
        }

        return sb.toString();
    }

    private static String getApiPath(
            final String uri,
            final String service) {
        return uri + "/api" + service;
    }
}
