package edu.virginia.jinsup;

import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * Agent whose behavior depends on a global buy probability (acting as a news
 * feed).
 */
public class OpporStratPoisson extends PoissonAgent {

  /**
   * Limits the number of shares owned by the agent.
   */
  private static final int INVENTORY_LIMIT = 30;

  /**
   * Whether or not agent owns more shares than INVENTORY_LIMIT or has a deficit
   * of more than -INVENTORY_LIMIT.
   */
  private boolean overLimit;

  /**
   * Global buy probability for all poisson opportunistic traders. Initially set
   * at 50 %.
   */
  private static double currBuyProbability = 0.50;

  /**
   * Creates an opportunistic trader.
   * 
   * @param matchEng
   *          Matching engine of the simulation.
   * @param lambdaOrder
   *          The mean order creation frequency.
   * @param lambdaCancel
   *          The mean order cancellation frequency.
   * @param initialBuyProbability
   *          Initial probability of making a buy order.
   * @param initialActTime
   *          Startup time of the simulation.
   */
  public OpporStratPoisson(MatchingEngine matchEng, int lambdaOrder,
    int lambdaCancel, long initialActTime) {
    super(matchEng, lambdaOrder, lambdaCancel, initialActTime);
    overLimit = false;
  }

  @Override
  public void makeOrder() {
    boolean[] inventoryResults =
      checkInventory(getInventory(), INVENTORY_LIMIT, overLimit);
    overLimit = inventoryResults[OVER_LIMIT];
    boolean willBuy = inventoryResults[WILL_BUY];
    // Whether or not to skip factor checking

    if (!inventoryResults[OVERRIDE]) {
      willBuy = (JinSup.rand.nextFloat() < currBuyProbability);
    }

    createPoissonOrder(willBuy,
      getOrderSize(0.66, 0.16, 0.05, 0.04, 0.03, 0.03, 0.03), 0.35, 0.20, 0.05,
      0.05, 0.05, 0.05, 0.07, 0.05, 0.05, 0.05, 0.03);
  }

  /**
   * Calculates the new global buy probability.
   */
  public static void calcNewBuyProbability() {
    currBuyProbability =
      currBuyProbability
        + (new UniformRealDistribution(JinSup.randGen, -0.2, 0.2)).sample();

    // prevent the probability from going over the limit
    if (currBuyProbability < 0.30) {
      currBuyProbability = 0.30;
    }
    if (currBuyProbability > 0.70) {
      currBuyProbability = 0.70;
    }
  }

  /**
   * Manually set the global buy probability.
   * 
   * @param buyProbability
   *          The probability of making a buy order.
   */
  public static void setBuyProbability(double buyProbability) {
    currBuyProbability = buyProbability;
  }

  public static double getCurrBuyProbability() {
    return currBuyProbability;
  }

}
