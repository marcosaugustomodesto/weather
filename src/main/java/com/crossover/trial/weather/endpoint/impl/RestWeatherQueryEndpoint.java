package com.crossover.trial.weather.endpoint.impl;

import static com.crossover.trial.weather.endpoint.impl.RestWeatherCollectorEndpoint.addAirport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.endpoint.api.WeatherQueryEndpoint;
import com.crossover.trial.weather.entity.AirportData;
import com.crossover.trial.weather.entity.AtmosphericInformation;
import com.crossover.trial.weather.entity.DataPoint;
import com.google.gson.Gson;

/**
 * The Weather App REST endpoint allows clients to query, update and check health stats. Currently, all data is
 * held in memory. The end point deploys to a single container
 *
 * @author code test administrator
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {


    /** earth radius in KM */
    public static final double R = 6372.8;

    /** shared gson json to object factory */
    private static Gson gson = new Gson();

    /** all known airports */
    protected static List<AirportData> airportData = new ArrayList<>();

    /** atmospheric information for each airport, idx corresponds with airportData */
    protected static List<AtmosphericInformation> atmosphericInformation = new LinkedList<>();

    /**
     * Internal performance counter to better understand most requested information, this map can be improved but
     * for now provides the basis for future performance optimizations. Due to the stateless deployment architecture
     * we don't want to write this to disk, but will pull it off using a REST request and aggregate with other
     * performance metrics {@link #ping()}
     */
    private static Map<AirportData, Integer> requestFrequency = new HashMap<AirportData, Integer>();

    private static Map<Double, Integer> radiusFreq = new HashMap<Double, Integer>();

    static {
        init();
    }
    /**
     * Retrieve service health including total size of valid data points and request frequency information.
     *
     * @return health stats for the service as a string
     */
    @Override
    public String ping() {
        Map<String, Object> retval = new HashMap<>();

        int datasize = 0;
        for (AtmosphericInformation ai : atmosphericInformation) {
            // we only count recent readings
            if (containInformation(ai) && ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000) {
                // updated in the last day
                datasize++;
            }
        }
        retval.put("datasize", datasize);

        Map<String, Double> freq = new HashMap<>();
        // fraction of queries
        for (AirportData data : airportData) {
            double frac = (double)requestFrequency.getOrDefault(data, 0) / requestFrequency.size();
            freq.put(data.getIata(), frac);
        }
        retval.put("iata_freq", freq);

        int m = radiusFreq.keySet().stream()
                .max(Double::compare)
                .orElse(1000.0).intValue() + 1;

        int[] hist = new int[m];
        for (Map.Entry<Double, Integer> e : radiusFreq.entrySet()) {
            int i = e.getKey().intValue() % 10;
            hist[i] += e.getValue();
        }
        retval.put("radius_freq", hist);

        return gson.toJson(retval);
    }

     private boolean containInformation(AtmosphericInformation ai) { 
        List<DataPoint> listDataPoint = new ArrayList<DataPoint>();
        if(ai.getCloudCover()!= null) 
              listDataPoint.add(ai.getCloudCover());
        if(ai.getHumidity()!= null) 
              listDataPoint.add(ai.getHumidity());
        if(ai.getPressure()!= null) 
              listDataPoint.add(ai.getPressure());
        if(ai.getPrecipitation()!= null) 
              listDataPoint.add(ai.getPrecipitation());
        if(ai.getTemperature()!= null) 
              listDataPoint.add(ai.getTemperature());
        if(ai.getWind()!= null) 
              listDataPoint.add(ai.getWind());
        return !listDataPoint.isEmpty();                                 
     }

    /**
     * Given a query in json format {'iata': CODE, 'radius': km} extracts the requested airport information and
     * return a list of matching atmosphere information.
     *
     * @param iata the iataCode
     * @param radiusString the radius in km
     *
     * @return a list of atmospheric information
     */
    @Override
    public Response weather(String iata, String radiusString) {
        Double radius = radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);
        updateRequestFrequency(iata, radius);

        List<AtmosphericInformation> retval = new ArrayList<>();
        if (radius.equals(0.0)) {
            int idx = getAirportDataIdx(iata);
            retval.add(atmosphericInformation.get(idx));
        } else {
            AirportData ad = findAirportData(iata);
            for (int i=0;i< airportData.size(); i++){
                if (calculateDistance(ad, airportData.get(i)) <= radius && containInformation(atmosphericInformation.get(i))){
                    retval.add(atmosphericInformation.get(i));
                }
            }
        }
        return Response.status(Response.Status.OK).entity(retval).build();
    }


    /**
     * Records information about how often requests are made
     *
     * @param iata an iata code
     * @param radius query radius
     */
    public void updateRequestFrequency(String iata, Double radius) {
        AirportData airport = findAirportData(iata);
        requestFrequency.put(airport, requestFrequency.getOrDefault(airport, 0) + 1);
        radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public static AirportData findAirportData(String iataCode) {
        return airportData.stream()
            .filter(ap -> ap.getIata().equals(iataCode))
            .findFirst().orElse(null);
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public static int getAirportDataIdx(String iataCode) {
        AirportData ad = findAirportData(iataCode);
        return airportData.indexOf(ad);
    }

    /**
     * Haversine distance between two airports.
     *
     * @param ad1 airport 1
     * @param ad2 airport 2
     * @return the distance in KM
     */
    public double calculateDistance(AirportData ad1, AirportData ad2) {
        double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
        double deltaLon = Math.toRadians(ad2.getLongitude() - ad1.getLongitude());
        double a =  Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
                * Math.cos(ad1.getLatitude()) * Math.cos(ad2.getLatitude());
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    /**
     * A dummy init method that loads hard coded data
     */
    public static void init() {
        airportData.clear();
        atmosphericInformation.clear();
        requestFrequency.clear();
        
        addAirport("BOS", 42.364347, -71.005181);
        addAirport("EWR", 40.6925, -74.168667);
        addAirport("JFK", 40.639751, -73.778925);
        addAirport("LGA", 40.777245, -73.872608);
        addAirport("MMU", 40.79935, -74.4148747);
    }

}
