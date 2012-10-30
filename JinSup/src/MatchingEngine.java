import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;


public class MatchingEngine {
  // hashmap should take agenID instead...makes it easier to delete
  // objects (dont need the agent object).

  // but nope. It is easier to just have it as agents so we can notify them
  // when a trade is made.

  private HashMap<Long, ArrayList<Order>> orderMap;
  private int lastVolumeTraded;

  //these priority queues will only have the 10 most recent orders
  private ArrayList<Order> allOrders;
  //	private ArrayList<Order> priorityQueueBuy; //must keep sorted after each insert/removal
  //	private ArrayList<Order> priorityQueueSell; 

  public MatchingEngine () {
    orderMap = new HashMap<Long, ArrayList<Order>>();
    //		priorityQueueBuy = new ArrayList<Order>();
    //		priorityQueueSell = new ArrayList<Order>();
    allOrders = new ArrayList<Order>();
  }

  public void cancelOrder(Order o, long agentID){
    allOrders.remove(o);
    orderMap.get(agentID).remove(o);
    //log the action.
  }

  public void createOrder(Order o, long agentID) {
    allOrders.add(o);
    if(orderMap.containsKey(agentID)) {
      orderMap.get(agentID).add(o);
    }
    else {
      ArrayList<Order> orderList = new ArrayList<Order>();
      orderList.add(o);
      orderMap.put(agentID, orderList);
    }
    //log the action.
    // must then check if a trade can occur
    checkMakeTrade(o);
  }

  public void modifyOrder(Order o, double newPrice, int newQuant) {
    o.setPrice(newPrice);
    o.setQuant(newQuant);
    //log the action
    // must then check if a trade can occur
    checkMakeTrade(o);
  }

  public double getLastTradePrice() {
    // TODO
    return 0.0;
  }


  // should verify again that this method should get the top ten, and
  // not the most recent orders in the orderbook.
  public ArrayList<Order> topBuyOrders() {
    ArrayList<Order> topBuyOrders = new ArrayList<Order>();
    int i = 0;
    while(i < allOrders.size() && !allOrders.get(i).isBuyOrder()) {
      i++;
    }

    // in case there are no buy orders in the array
    if(!allOrders.get(i).isBuyOrder()) {
      return topBuyOrders;
    } else {
      topBuyOrders.add(allOrders.get(i));
    }

    for(int j = i; j < allOrders.size(); j++) {
      if(allOrders.get(j).isBuyOrder()) {
        if(topBuyOrders.size() < 10)
          topBuyOrders.add(allOrders.get(j));
        Collections.sort(topBuyOrders);
      } else {
        if(allOrders.get(j).compareTo(topBuyOrders.get(9)) > 0){
          topBuyOrders.set(9, allOrders.get(j));
          Collections.sort(topBuyOrders);
        }
      }
    }
    return topBuyOrders;
  }

  public ArrayList<Order> topSellOrders() {
    ArrayList<Order> topSellOrders = new ArrayList<Order>();
    int i = 0;
    while(i < allOrders.size() && allOrders.get(i).isBuyOrder()) {
      i++;
    }

    // in case there are no buy orders in the array
    if(allOrders.get(i).isBuyOrder()) {
      return topSellOrders;
    } else {
      topSellOrders.add(allOrders.get(i));
    }

    for(int j = i; j < allOrders.size(); j++) {
      if(!allOrders.get(j).isBuyOrder()) {
        if(topSellOrders.size() < 10)
          topSellOrders.add(allOrders.get(j));
        Collections.sort(topSellOrders);
      } else {
        if(allOrders.get(j).compareTo(topSellOrders.get(9)) > 0){
          topSellOrders.set(9, allOrders.get(j));
          Collections.sort(topSellOrders);
        }
      }
    }
    return topSellOrders;
  }

  // method to check for and make trades
  public void checkMakeTrade(Order o) {
    ArrayList<Order> samePrice = new ArrayList<Order>();
    if(o.isBuyOrder()) {
      // check for sell orders at the same sell price
      // must be sure to pick orders that were placed first.
      // TODO: potential error due to double precision
      for(int i = 0; i < allOrders.size(); i ++) {
        if(!allOrders.get(i).isBuyOrder() && allOrders.get(i).getPrice() == o.getPrice()) {
          samePrice.add(allOrders.get(i));
        }
      }
    } else {
      for(int i = 0; i < allOrders.size(); i ++) {
        if(allOrders.get(i).isBuyOrder() && allOrders.get(i).getPrice() == o.getPrice()) {
          samePrice.add(allOrders.get(i));
        }
      }
    }

    // trade was not made
    int volumeTraded = 0;
    if(samePrice.isEmpty()) {

      return;
    }

    // now select the orders that were made first.
    Collections.sort(samePrice);

    Order orderToTrade = samePrice.get(0);

    if(o.getCurrentQuant() == orderToTrade.getCurrentQuant()) {
      allOrders.remove(o);
      allOrders.remove(orderToTrade);
      orderMap.get(o.getCreatorID()).remove(o);
      orderMap.get(orderToTrade.getCreatorID()).remove(orderToTrade);	

    } else if(o.getCurrentQuant() > orderToTrade.getCurrentQuant()) {
      //delete orderToTrade, decrease quantity of o
      volumeTraded = orderToTrade.getCurrentQuant();
      o.setQuant(orderToTrade.getCurrentQuant());
      allOrders.remove(orderToTrade);
      orderMap.get(orderToTrade.getCreatorID()).remove(orderToTrade);
    } else {
      volumeTraded = o.getCurrentQuant();
      orderToTrade.setQuant(o.getCurrentQuant());
      allOrders.remove(o);
      orderMap.get(o.getCreatorID()).remove(o);
    }
    // log the action and ID of trade, with System.currentTimeMillis()
    // and volume traded.
    // notify both agents that a trade has occurred.

  }
  
}