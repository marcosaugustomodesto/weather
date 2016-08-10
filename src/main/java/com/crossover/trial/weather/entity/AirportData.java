package com.crossover.trial.weather.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Basic airport information.
 *
 * @author code test administrator
 */
public class AirportData {

    /** the three letter IATA code */
    String iata;

    /** latitude value in degrees */
    double latitude;

    /** longitude value in degrees */
    double longitude;

    public AirportData() { 
                   
    }
 
    public AirportData(String iata, double latitude, double longitude) {
          super();
          this.iata = iata;
          this.latitude = latitude;
          this.longitude = longitude;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((iata == null) ? 0 : iata.hashCode());
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AirportData){
            final AirportData other = (AirportData) obj;
            return new EqualsBuilder()
                .append(iata, other.iata)
                .append(latitude, other.latitude)
                .append(longitude, other.longitude)
                .isEquals();
        } else{
            return false;
        }
   }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

}
