package com.crossover.trial.weather.client;

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.entity.DataPoint;

/**
 * A reference implementation for the weather client. Consumers of the REST API can look at WeatherClient
 * to understand API semantics. This existing client populates the REST endpoint with dummy data useful for
 * testing.
 *
 * @author code test administrator
 */
public class WeatherClient {

    private static final String BASE_URI = "http://localhost:9090";
    
    private static final Logger LOGGER = Logger.getLogger(WeatherClient.class.getName());

    /** end point for read queries */
    private WebTarget query;

    /** end point to supply updates */
    private WebTarget collect;

    public WeatherClient() {
        Client client = ClientBuilder.newClient();
        query = client.target(BASE_URI + "/query");
        collect = client.target(BASE_URI + "/collect");
    }

    public void pingCollect() {
        WebTarget path = collect.path("/ping");
        Response response = path.request().get();
        LOGGER.info("collect.ping: " + response.readEntity(String.class));
    }

    public void query(String iata) {
        WebTarget path = query.path("/weather/" + iata + "/0");
        Response response = path.request().get();
        LOGGER.info("query." + iata + ".0: " + response.readEntity(String.class));
    }

    public void pingQuery() {
        WebTarget path = query.path("/ping");
        Response response = path.request().get();
        LOGGER.info("query.ping: " + response.readEntity(String.class));
    }

    public void populate(String pointType, String iata, int first, int last, int mean, int median, int count) {
        WebTarget path = collect.path("/weather/" + iata + "/" + pointType);
        DataPoint dp = new DataPoint.Builder()
                .withFirst(first).withLast(last).withMean(mean).withMedian(median).withCount(count)
                .build();
        Response post = path.request().post(Entity.entity(dp, "application/json"));
        LOGGER.info(post.toString());
    }

    public void exit() {
        collect.path("/exit").request().get();
    }

    public void deleteAirport(String iata) {       
        WebTarget path = collect.path("/airport/" + iata);
        Invocation.Builder invocationBuilder =  path.request();
        Response response = invocationBuilder.delete();
        LOGGER.info("collect.airport."+ iata + " (delete): " + response.getStatus());
    }
    
    public static void main(String[] args) {
        WeatherClient wc = new WeatherClient();
        wc.pingCollect();
        wc.populate("wind", "BOS", 0, 10, 6, 4, 20);
        wc.query("BOS");
        wc.query("JFK");
        wc.query("EWR");
        wc.query("LGA");
        wc.query("MMU");
        wc.pingQuery();
        wc.deleteAirport("LHR");
        wc.pingQuery();
        wc.exit();

        LOGGER.info("complete");
    }
}
