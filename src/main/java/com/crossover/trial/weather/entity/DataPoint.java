package com.crossover.trial.weather.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A collected point, including some information about the range of collected values
 *
 * @author code test administrator
 */
public class DataPoint {

    private double mean = 0.0;

    private int first = 0;

    private int second = 0;

    private int third = 0;

    private int count = 0;


    public DataPoint(int first, int second, int mean, int third, int count) {
        this.setFirst(first);
        this.setMean(mean);
        this.setSecond(second);
        this.setThird(third);
        this.setCount(count);
    }

    /** the mean of the observations */
    public double getMean() {
        return mean;
    }

    public void setMean(double mean) { 
                   this.mean = mean; 
    }

    /** 1st quartile -- useful as a lower bound */
    public int getFirst() {
        return first;
    }

    protected void setFirst(int first) {
        this.first = first;
    }

    /** 2nd quartile -- median value */
    public int getSecond() {
        return second;
    }

    protected void setSecond(int second) {
        this.second = second;
    }

    /** 3rd quartile value -- less noisy upper value */
    public int getThird() {
        return third;
    }

    protected void setThird(int third) {
        this.third = third;
    }

    /** the total number of measurements */
    public int getCount() {
        return count;
    }

    protected void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }



    @Override
     public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + count;
          result = prime * result + first;
          long temp;
          temp = Double.doubleToLongBits(mean);
          result = prime * result + (int) (temp ^ (temp >>> 32));
          result = prime * result + second;
          result = prime * result + third;
          return result;
     }

     @Override
     public boolean equals(Object obj) {
         if(obj instanceof DataPoint){
             final DataPoint other = (DataPoint) obj;
             return new EqualsBuilder()
                 .append(count, other.count)
                 .append(first, other.first)                 
                 .append(second, other.second)
                 .append(third, other.third)
                 .append(mean, other.mean)
                 .isEquals();
         } else{
             return false;
         }        
     }

     public static class Builder {
        int first;
        int mean;
        int median;
        int last;
        int count;

        public Builder() { 
                       
        }

        public Builder withFirst(int first) {
            this.first= first;
            return this;
        }

        public Builder withMean(int mean) {
            this.mean = mean;
            return this;
        }

        public Builder withMedian(int median) {
            this.median = median;
            return this;
        }

        public Builder withCount(int count) {
            this.count = count;
            return this;
        }

        public Builder withLast(int last) {
            this.last = last;
            return this;
        }

        public DataPoint build() {
            return new DataPoint(this.first, this.mean, this.median, this.last, this.count);
        }
    }
}
