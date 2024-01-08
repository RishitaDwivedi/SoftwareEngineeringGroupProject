
package uk.ac.soton.seg15.model;

import uk.ac.soton.seg15.model.Runway;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Calculate {
  private static final Logger logger = LogManager.getLogger(Calculate.class);
  public double stripEnd;
  public final double CentreLine = 75;
  public double blastDistance;
  private double height;
  private double distanceToCentreLine;
  private double distanceFromThreshold;
  private Runway runway;
  private String TORACalc = "";
  private String ASDACalc = "";
  private String TODACalc = "";
  private String LDACalc = "";
  private String action;

  private double ascent;
  private double descent;

  public Calculate(Runway runway, double height, double blastDistance, double stripEnd, double distanceFromThreshold, String action) {
    if (runway == null)
      throw new IllegalArgumentException(
              "Calculate Error. Invalid runway, cannot be null.");
    if (height < 0 || stripEnd < 0 || blastDistance < 0) {
      throw new IllegalArgumentException(
          "Calculate Error. Invalid param, can't be negative.");
    }

    this.height = height;
    this.blastDistance = blastDistance;
    this.stripEnd = stripEnd;
    this.action = action;
    this.runway = runway;
    this.distanceFromThreshold = distanceFromThreshold;
    this.ascent = 50;
    this.descent = 50;
  }

  public Calculate(Runway runway, double height, double blastDistance, double stripEnd, double distanceFromThreshold, String action, double ascent,double descent) {
    if (runway == null)
      throw new IllegalArgumentException(
              "Calculate Error. Invalid runway, cannot be null.");
    if (height < 0 || stripEnd < 0 || blastDistance < 0) {
      throw new IllegalArgumentException(
              "Calculate Error. Invalid param, can't be negative.");
    }

    this.height = height;
    this.blastDistance = blastDistance;
    this.stripEnd = stripEnd;
    this.action = action;
    this.runway = runway;
    this.distanceFromThreshold = distanceFromThreshold;
    this.ascent = ascent;
    this.descent = descent;
  }
  public Calculate() {
  }

  public void recalculate(int direction) {
    if (this.runway == null)
      throw new IllegalArgumentException(
              "Error. Invalid runway, cannot be null.");
    if(this.runway.getNewParameters() == null){
      runway.setNewParameters(new Parameters());
    }
    this.runway.getParameters().setClearway();
    this.runway.getParameters().setStopway();


    if(action == null) return;
    if (this.action.equals("TakeOff Away")){
      //takeOffAway(direction == 1 ? distanceFromLeftThreshold : distanceFromRightThreshold);
      logger.info("Action = " + this.action);
      takeOffAway(this.distanceFromThreshold);
    }
    if (this.action == "TakeOff Toward"){
      //takeOffTowards(direction == 1 ? distanceFromRightThreshold : distanceFromLeftThreshold);
      logger.info("Action = " + this.action);
      takeOffTowards(this.distanceFromThreshold);
    }
    if (this.action == "Landing Toward"){
      //landingTowards(direction == 1 ? distanceFromRightThreshold : distanceFromLeftThreshold);
      logger.info("Action = " + this.action);
      landingTowards(this.distanceFromThreshold);
    }
    if (this.action == "Landing Over"){
      //landingOver(direction == 1 ? distanceFromLeftThreshold : distanceFromRightThreshold);
      logger.info("Action = " + this.action);
      landingOver(this.distanceFromThreshold);
    }

  }

  private void takeOffAway(double thresholdDistance) {
    // Clearway = TODA - TORA
    Parameters params = this.runway.getNewParameters();
      double RTORA = this.runway.getParameters().getTora() - this.blastDistance - thresholdDistance - this.runway.getThreshold();
      double RTODA = RTORA + this.runway.getParameters().getClearway();
      double RASDA = RTORA + this.runway.getParameters().getStopway();
      params.setTora(RTORA);
      params.setToda(RTODA);
      params.setAsda(RASDA);
//    if(params.getToda() <= 0 || params.getAsda() <= 0 || params.getTora() <= 0)
//        throw new IllegalArgumentException("Calculation error. Recalculated parameters are negative");
    logger.info("Takeoff Away Complete and Parameters = " + this.runway.getParameters() + this.runway.getNewParameters());
    this.runway.setNewParameters(params);
  }

  private void takeOffTowards(double thresholdDistance) {
    double slopeCalculation = this.height * ascent;

    Parameters newParameters = this.runway.getNewParameters();

    if (this.runway.getParameters().getResa() > slopeCalculation) { // this case shouldn't happen until obstacle length is known
      double RTORA = this.runway.getThreshold() + thresholdDistance - this.runway.getParameters().getResa() - this.stripEnd;
      this.TORACalc = "TORA = Displaced Threshold + Distance from Threshold - RESA - StripEnd \n" + this.runway.getThreshold() + "+" + thresholdDistance + "-" + this.runway.getParameters().getResa() + "-" + this.stripEnd + "=" + RTORA;
      double RTODA = RTORA;
      this.TODACalc = "TODA = TORA \n" + RTORA;
      double RASDA = RTORA;
      this.ASDACalc = "ASDA = TORA \n" + RTORA;
      newParameters.setTora(RTORA);
      newParameters.setToda(RTODA);
      newParameters.setAsda(RASDA);
    } else {
      double RTORA = this.runway.getThreshold() + thresholdDistance - slopeCalculation - this.stripEnd;
      this.TORACalc = "TORA = Displaced Threshold + Distance from Threshold - SlopeCalculation - StripEnd \n" + this.runway.getThreshold() + "+" + thresholdDistance + "-" + slopeCalculation + "-" + this.stripEnd + "=" + RTORA;
      double RTODA = RTORA;
      this.TODACalc = "TODA = TORA \n" + RTORA;
      double RASDA = RTORA;
      this.ASDACalc = "ASDA = TORA \n" + RTORA;
      newParameters.setTora(RTORA);
      newParameters.setToda(RTODA);
      newParameters.setAsda(RASDA);
    }

//    if(newParameters.getToda() <= 0 || newParameters.getAsda() <= 0 || newParameters.getTora() <= 0)
//      throw new IllegalArgumentException("Calculation error. Recalculated parameters are negative");
    this.runway.setNewParameters(newParameters);
    logger.info("Takeoff Towards Calculated and Parameters = " + this.runway.getParameters() + this.runway.getNewParameters());

  }

  private void landingTowards(double thresholdDistance) {
    Parameters params = this.runway.getNewParameters();
    double RLDA = thresholdDistance - this.runway.getParameters().getResa() - this.stripEnd;
    this.LDACalc = "LDA = Distance from Threshold - RESA - StripEnd \n" + thresholdDistance + "-" + this.runway.getParameters().getResa() + "-" + this.stripEnd + "=" + RLDA;
    params.setLda(RLDA);
//    if(params.getLda() <= 0)
//      throw new IllegalArgumentException("Calculation error. Recalculated parameters are negative");
    this.runway.setNewParameters(params);
    logger.info("Landing Toward Complete and Parameters = " + this.runway.getParameters() + this.runway.getNewParameters());
  }

  private void landingOver(double thresholdDistance) {
    double slopeCalculation = this.height * descent;
    Parameters params = this.runway.getNewParameters();

    if (this.runway.getParameters().getResa() > slopeCalculation) { // this case shouldn't happen until obstacle length is known
        double RLDA = this.runway.getParameters().getLda() - thresholdDistance - this.runway.getParameters().getResa() - this.stripEnd;
        params.setLda(RLDA);
    } else {
      if (slopeCalculation + this.stripEnd < this.blastDistance) {
        double RLDA = this.runway.getParameters().getLda() - thresholdDistance - this.blastDistance;
        params.setLda(RLDA);
      } else {
        double RLDA = this.runway.getParameters().getLda() - thresholdDistance - slopeCalculation - this.stripEnd;
        params.setLda(RLDA);
      }
    }
//    if(params.getLda() <= 0)
//      throw new IllegalArgumentException("Calculation error. Recalculated parameters are negative");
    this.runway.setNewParameters(params);
    logger.info("Landing Over and Parameters = " + this.runway.getParameters() + this.runway.getNewParameters());
  }

  public String getTORACalc() {
    return this.TORACalc;
  }

  public Runway getRunway() {
    return runway;
  }

  public void setRunway(Runway runway) {
    if (runway == null)
      throw new IllegalArgumentException(
              "Error. Invalid runway, cannot be null.");
    this.runway = runway;
  }

  public String getTODACalc() {
    return this.TODACalc;
  }

  public String getASDACalc() {
    return this.ASDACalc;
  }

  public String getLDACalc() {
    return this.LDACalc;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setBlastDistance(double blastDistance) {
    this.blastDistance = blastDistance;
  }

  public void setDistanceFromThreshold(double distanceFromThreshold) {
    this.distanceFromThreshold = distanceFromThreshold;
  }


  public void setHeight(double height) {
    if (height < 0)
      throw new IllegalArgumentException(
              "Error. Invalid height, can't be negative.");
    this.height = height;
  }

  public void setDistanceToCentreLine(double distanceToCentreLine) {
    if (distanceToCentreLine < 0)
      throw new IllegalArgumentException(
              "Error. Invalid distanceToCentreLine, can't be negative.");
    this.distanceToCentreLine = distanceToCentreLine;
  }

  public void setStripEnd(double stripEnd) {
    if (stripEnd < 0)
      throw new IllegalArgumentException(
              "Error. Invalid stripEnd, can't be negative.");
    this.stripEnd = stripEnd;
  }


}

