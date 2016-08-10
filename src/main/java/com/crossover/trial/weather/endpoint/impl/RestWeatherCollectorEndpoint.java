package com.crossover.trial.weather.endpoint.impl;

import static com.crossover.trial.weather.endpoint.impl.RestWeatherQueryEndpoint.airportData;
import static com.crossover.trial.weather.endpoint.impl.RestWeatherQueryEndpoint.atmosphericInformation;
import static com.crossover.trial.weather.endpoint.impl.RestWeatherQueryEndpoint.findAirportData;
import static com.crossover.trial.weather.endpoint.impl.RestWeatherQueryEndpoint.getAirportDataIdx;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.endpoint.api.WeatherCollectorEndpoint;
import com.crossover.trial.weather.entity.AirportData;
import com.crossover.trial.weather.entity.AtmosphericInformation;
import com.crossover.trial.weather.entity.DataPoint;
import com.crossover.trial.weather.entity.DataPointType;
import com.crossover.trial.weather.exceptions.WeatherException;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
    private static final Logger LOGGER = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());

    /** shared gson json to object factory */
    private static Gson gson = new Gson();

    @Override
    public Response ping() {
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    @Override
    public Response updateWeather(String iataCode, String pointType, String datapointJson) {
        try {
            addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "context", e);
             
        }
        return Response.status(Response.Status.OK).build();
    }


    @Override
    public Response getAirports() {
        Set<String> retval = new HashSet<>();
        for (AirportData ad : airportData) {
            retval.add(ad.getIata());
        }
        return Response.status(Response.Status.OK).entity(retval).build();
    }


    @Override
    public Response getAirport(@PathParam("iata") String iata) {
        AirportData ad = findAirportData(iata);
        return Response.status(Response.Status.OK).entity(ad).build();
    }


    @Override
    public Response addAirport(String iata, String latString, String longString) {
        addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
        return Response.status(Response.Status.OK).build();
    }


    @Override
    public Response deleteAirport(String iata) {         
         AirportData ad = delAirport(iata);
          return Response.status(Response.Status.OK).entity(ad).build();
    }

    @Override
    public Response exit() {
        return Response.noContent().build();
    }
    //
    // Internal support methods
    //

    /**
     * Update the airports weather data with the collected data.
     *
     * @param iataCode the 3 letter IATA code
     * @param pointType the point type {@link DataPointType}
     * @param dp a datapoint object holding pointType data
     *
     * @throws WeatherException if the update can not be completed
     */
    public void addDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException {
        int airportDataIdx = getAirportDataIdx(iataCode);
        AtmosphericInformation ai = atmosphericInformation.get(airportDataIdx);
        updateAtmosphericInformation(ai, pointType, dp);
    }

    /**
     * update atmospheric information with the given data point for the given point type
     *
     * @param ai the atmospheric information object to update
     * @param pointType the data point type as a string
     * @param dp the actual data point
     */
    public void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp) throws WeatherException {

        updateWind(ai, pointType, dp);

        updateTemperature(ai, pointType, dp);

        updateHumidty(ai, pointType, dp);

        updatePressure(ai, pointType, dp);

        updateCloudcover(ai, pointType, dp);

        updatePrecipitation(ai, pointType, dp);

    }

     private void updatePrecipitation(AtmosphericInformation ai, String pointType, DataPoint dp) {
          if (pointType.equalsIgnoreCase(DataPointType.PRECIPITATION.name()) && dp.getMean() >=0 && dp.getMean() < 100) {
            ai.setPrecipitation(dp);
            ai.setLastUpdateTime(System.currentTimeMillis());
        }
     }

     private void updateCloudcover(AtmosphericInformation ai, String pointType, DataPoint dp) {
          if (pointType.equalsIgnoreCase(DataPointType.CLOUDCOVER.name()) && dp.getMean() >= 0 && dp.getMean() < 100) {
            ai.setCloudCover(dp);
            ai.setLastUpdateTime(System.currentTimeMillis());
        }
     }

     private void updatePressure(AtmosphericInformation ai, String pointType, DataPoint dp) {
          if (pointType.equalsIgnoreCase(DataPointType.PRESSURE.name()) && dp.getMean() >= 650 && dp.getMean() < 800) {
            ai.setPressure(dp);
            ai.setLastUpdateTime(System.currentTimeMillis());
        }
     }

     private void updateHumidty(AtmosphericInformation ai, String pointType, DataPoint dp) {
          if (pointType.equalsIgnoreCase(DataPointType.HUMIDTY.name()) && dp.getMean() >= 0 && dp.getMean() < 100) {
            ai.setHumidity(dp);
            ai.setLastUpdateTime(System.currentTimeMillis());
        }
     }

     private void updateTemperature(AtmosphericInformation ai, String pointType, DataPoint dp) {
          if (pointType.equalsIgnoreCase(DataPointType.TEMPERATURE.name()) && dp.getMean() >= -50 && dp.getMean() < 100) {
            ai.setTemperature(dp);
            ai.setLastUpdateTime(System.currentTimeMillis());
        }
     }

     private void updateWind(AtmosphericInformation ai, String pointType, DataPoint dp) {
          if (pointType.equalsIgnoreCase(DataPointType.WIND.name()) && dp.getMean() >= 0) {
            ai.setWind(dp);
            ai.setLastUpdateTime(System.currentTimeMillis());
        }
     }

    /**
     * Add a new known airport to our list.
     *
     * @param iataCode 3 letter code
     * @param latitude in degrees
     * @param longitude in degrees
     *
     * @return the added airport
     */
    public static AirportData addAirport(String iataCode, double latitude, double longitude) {
        AirportData ad = new AirportData();
        airportData.add(ad);

        AtmosphericInformation ai = new AtmosphericInformation();
        atmosphericInformation.add(ai);
        ad.setIata(iataCode);
        ad.setLatitude(latitude);
        ad.setLatitude(longitude);
        return ad;
    }
    
    /**
     * Delete a known airport from our list.
     *
     * @param iataCode 3 letter code
     *
     * @return the removed airport
     */
    public static AirportData delAirport(String iataCode) {
         for(AirportData a: airportData){
               String iataaux = a.getIata().toString();
               if(iataaux.equals(iataCode)){
                    airportData.remove(a); 
               }
         }
         return null;
    }
    
}
