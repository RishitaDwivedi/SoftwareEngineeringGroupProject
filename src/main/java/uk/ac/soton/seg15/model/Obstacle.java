package uk.ac.soton.seg15.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "obstacle")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Obstacle {

  private String name;
  private double height;
  private double width;
  private double distanceToCentreLine;
  private double distanceFromThreshold;


  public Obstacle(){

  }

  public Obstacle (String name, double height, double width, double distanceToCentreLine, double distanceFromThresh) {
      super();
      this.name = name;
      this.height = height;
      this.width = width;
      this.distanceToCentreLine = distanceToCentreLine;
      this.distanceFromThreshold = distanceFromThresh;

    }



  @XmlElement(name = "distancetoCentreLine")
  public double getDistanceToCentreLine() {
    return distanceToCentreLine;
  }

  public void setDistanceToCentreLine(double distanceToCentreLine) {
    this.distanceToCentreLine = distanceToCentreLine;
  }
  @XmlElement(name = "distancefromLeftThreshold")
  public double getDistanceFromThreshold() {
    return distanceFromThreshold;
  }

  public void setDistanceFromThreshold(double threshDist) {
    this.distanceFromThreshold = threshDist;
  }

  @XmlElement(name = "height")
  public double getHeight() {return height;}
  public void setHeight(double height) {this.height = height;}

  @XmlElement(name = "width")
  public double getWidth() {return width;}
  public void setWidth(double width) {this.width = width;}


  @XmlElement(name = "name")
  public String getName() {return name;}
  public void setName(String name){this.name = name;}


  //Array of predefined obstacles
  public static Obstacle[] obstacleArray() {
   Obstacle[] obstacles = new Obstacle[4];
    //this obstacle is on the centreline
   obstacles[0] = new Obstacle("Pothole", 0,5, 50, 3646);
   obstacles[1] = new Obstacle("Aeroplane", 25,40,20, 2853);
   obstacles[2] = new Obstacle("Broken part", 20,15, 20,50);
   obstacles[3] = new Obstacle("Personal Aircraft", 15,10, 60,150);

    return obstacles;
  }


  
}
