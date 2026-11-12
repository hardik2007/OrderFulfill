package Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FulfillmentEngine {

    public static List<Warehouse> getCandidateWarehouses(List<Warehouse> allWarehouses, OrderItem item) {
        ArrayList<Warehouse> candidateWarehouses = new ArrayList<>();
        for (Warehouse w : allWarehouses) {
            if (w.hasStock(item.product, 1)) {
                candidateWarehouses.add(w);
            }
        }
        return candidateWarehouses;
    }

    public static List<FulfillmentPlan> generatePlans(List<Warehouse> candidateWarehouses, Order order) {
        ArrayList<FulfillmentPlan> allPlans = new ArrayList<>();
        backtrack(candidateWarehouses, new ArrayList<>(), allPlans, 0, 3);
        return allPlans;
    }

    private static void backtrack(List<Warehouse> candidates, List<Warehouse> currentCombination, List<FulfillmentPlan> allPlans, int start, int maxLimit) {
        if (!currentCombination.isEmpty()) {
            FulfillmentPlan plan = new FulfillmentPlan(currentCombination);
            allPlans.add(plan);
        }

        if (currentCombination.size() == maxLimit) return;

        for (int i = start; i < candidates.size(); i++) {
            currentCombination.add(candidates.get(i));
            backtrack(candidates, currentCombination, allPlans, i + 1, maxLimit);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }
    
    public static FulfillmentPlan selectBestPlan(
    		List<Warehouse> candidateWarehouses,
            Order order,
            List<Constraint> constraints,
            FulfillmentStrategy strategy) {
    	List<FulfillmentPlan> allPlans= generatePlans(candidateWarehouses,order);
    	FulfillmentPlan bestPlan= null;
    	double bestScore =Double.POSITIVE_INFINITY;
    	for(FulfillmentPlan plan: allPlans) {
    		boolean valid=true;
    		
    		for(Constraint constraint: constraints)
    		{
    			if (!constraint.isSatisfied(plan, order)) {
                    valid = false;
                    break;
                }
    		}
    		
    		if(valid)
    		{
    			double score=strategy.calculateScore(plan, order);
    			
    			if(score<bestScore) {
        			bestScore=score;
        			bestPlan=plan;
        		}
    		}
    	}
    	return bestPlan;
    }

    public static FulfillmentPlan greedyFallback(List<Warehouse> candidates, Order order) {
        Map<Product, Integer> unfulfilledItems = new HashMap<>();
        for (OrderItem item : order.items) {
            unfulfilledItems.put(item.product, item.requiredQuantity);
        }
        List<Warehouse> selectedWarehouses = new ArrayList<>();
        List<Warehouse> availableCandidates = new ArrayList<>(candidates);

        while (!unfulfilledItems.isEmpty()) {
            Warehouse bestWarehouse = null;
            int maxItems = 0;

            for (Warehouse w : availableCandidates) {
                int itemsProvided = 0;
                for (Map.Entry<Product, Integer> entry : unfulfilledItems.entrySet()) {
                    Product p = entry.getKey();
                    int needed = entry.getValue();
                    int inStock = w.inventory.getOrDefault(p, 0);
                    itemsProvided += Math.min(needed, inStock);
                }
                if (itemsProvided > maxItems) {
                    maxItems = itemsProvided;
                    bestWarehouse = w;
                }
            }

            if (bestWarehouse == null) {
                break;
            }

            selectedWarehouses.add(bestWarehouse);
            availableCandidates.remove(bestWarehouse);

            Iterator<Map.Entry<Product, Integer>> iterator = unfulfilledItems.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Product, Integer> entry = iterator.next();
                Product p = entry.getKey();
                int needed = entry.getValue();
                int inStock = bestWarehouse.inventory.getOrDefault(p, 0);

                if (inStock > 0) {
                    int remainingNeeded = needed - inStock;
                    if (remainingNeeded <= 0) {
                        iterator.remove();
                    } else {
                        unfulfilledItems.put(p, remainingNeeded);
                    }
                }
            }
        }
        return new FulfillmentPlan(selectedWarehouses);
    }
}