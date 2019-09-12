package edu.cmu.sv.badger.listener;

import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * This listener is used to get the path condition and cost of symcrete execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */

public class SymCreteCostListener extends ListenerAdapter {

    static boolean DEBUG = false;

    private Double observedOldCost = null;
    private Double observedNewCost = null;
    private PathCondition observedPC = null;
    private Map<String, Object> observedSolution = null;

    private boolean firstBacktrack = true;

    public SymCreteCostListener(Config config, JPF jpf) {
        PathCondition.setReplay(true);
    }

    public Double getObservedFinalOldCost() {
        return this.observedOldCost;
    }

    public Double getObservedFinalNewCost() {
        return this.observedNewCost;
    }

    public PathCondition getObservedPathCondition() {
        return this.observedPC;
    }

    public Map<String, Object> getObservedPCSolution() {
        return this.observedSolution;
    }

    @Override
    public void searchConstraintHit(Search search) {
        if (DEBUG) {
            System.out.print("search limit");
        }
        if (DEBUG) {
            System.out.print(" " + search.getStateId());
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (DEBUG) {
            System.out.println(">>> stateAdvanced");
        }
        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
        if (DEBUG) {
            System.out.println("cg: " + cg);
        }
    }

    @Override
    public void stateBacktracked(Search search) {
        if (DEBUG) {
            System.out.println(">>> stateBacktracked");
        }

        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
        if (DEBUG) {
            System.out.println("cg: " + cg);
        }

        if (cg != null && cg instanceof PCChoiceGenerator) {
            int offset = ((PCChoiceGenerator) cg).getOffset();
            if (offset == 0) {
                return;
            }

            if (firstBacktrack) {
                firstBacktrack = false;
                this.observedOldCost = Observations.lastObservedUserdefinedCostOldVersion;
                this.observedNewCost = Observations.lastObservedUserdefinedCostNewVersion;
                this.observedPC = ((PCChoiceGenerator) cg).getCurrentPC();
                if (this.observedPC != null) {
                    // Solve PC. Reset isReplay to false during satisfiability check, otherwise the
                    // PathCondition will always return true.
                    boolean isReplay = PathCondition.isReplay;
                    PathCondition.setReplay(false);
                    this.observedSolution = observedPC.solveWithValuation();
                    PathCondition.setReplay(isReplay);
                }
            }
        }
    }
}
