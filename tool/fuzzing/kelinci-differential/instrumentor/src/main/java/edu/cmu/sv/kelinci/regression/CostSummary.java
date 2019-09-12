package edu.cmu.sv.kelinci.regression;

public class CostSummary {

    private long cost1;
    private long cost2;

    public CostSummary(long cost1, long cost2) {
        this.cost1 = cost1;
        this.cost2 = cost2;
    }

    /**
     * @return cost difference
     */
    public long getCostDifference() {
        return Math.abs(cost2 - cost1);
    }

}
