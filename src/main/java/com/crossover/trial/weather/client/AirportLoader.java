package com.crossover.trial.weather.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.crossover.trial.weather.entity.AirportData;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 *
 * 
 * @author code test administrator
 */
public class AirportLoader {

    /** end point to supply updates */
    private WebTarget collect;
    
    private static final Logger LOGGER = Logger.getLogger(AirportLoader.class.getName());

    public AirportLoader() {
        Client client = ClientBuilder.newClient();
        collect = client.target("http://localhost:9090/collect");
    }

    public void upload(InputStream airportDataStream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream));
        String l = null;

        while ((l = reader.readLine()) != null) {
             String[] infoAirport = l.replaceAll("\"", "").split(",");
             String iata = infoAirport[4];
             double latitude = new Double(infoAirport[6]);
             double longitude= new Double(infoAirport[7]);
             AirportData airport = new AirportData(iata, latitude, longitude);
             populate(airport);
        }
    }

    public void populate(AirportData airport) {            
        WebTarget path = collect.path("/airport/"+ airport.getIata() + "/" + airport.getLatitude() + "/" + airport.getLongitude());
   
        path.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(""));
        
        path.request().post(Entity.entity(airport, "application/json"));
        LOGGER.info(path.toString());    
    }
    
    public static void main(String[] args) throws IOException{
        File airportDataFile = new File(args[0]);
        if (!airportDataFile.exists() || airportDataFile.length() == 0) {           
             LOGGER.info(airportDataFile + " is not a valid input");    
        } else {
            AirportLoader al = new AirportLoader();
            al.upload(new FileInputStream(airportDataFile));
        }
    }
}
