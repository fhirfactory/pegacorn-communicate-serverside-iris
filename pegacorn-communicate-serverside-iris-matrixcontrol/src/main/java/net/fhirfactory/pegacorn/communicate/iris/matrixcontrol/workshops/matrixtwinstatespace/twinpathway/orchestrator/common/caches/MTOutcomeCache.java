/*
 * Copyright (c) 2020 Mark A. Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.communicate.iris.matrixcontrol.workshops.matrixtwinstatespace.twinpathway.orchestrator.common.caches;

import net.fhirfactory.pegacorn.communicate.iris.matrixcontrol.model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MTOutcomeCache {
    private ConcurrentHashMap<MTBehaviourOutcomeIdentifier, MTBehaviourOutcome> outcomePool;
    private ConcurrentHashMap<MTBehaviourIdentifier, Set<MTBehaviourOutcomeIdentifier>> behaviour2OutcomeMap;

    public MTOutcomeCache(){
        this.outcomePool = new ConcurrentHashMap<>();
        this.behaviour2OutcomeMap = new ConcurrentHashMap<>();
    }

    public void addOutcome(MTBehaviourOutcome newOutcome){
        if(newOutcome == null){
            return;
        }
        if(newOutcome.getId() == null){
            return;
        }
        this.outcomePool.put(newOutcome.getId(), newOutcome);
        if(newOutcome.getSourceBehaviour() != null){
            addOutcomeAssociation2Behaviour(newOutcome.getId(), newOutcome.getSourceBehaviour());
        }
    }

    public void removeOutcome(MTBehaviourOutcomeIdentifier outcomeToRemove){
        if(outcomeToRemove == null){
            return;
        }
        if(!outcomePool.containsKey(outcomeToRemove)){
            return;
        }
        MTBehaviourOutcome workingOutcome = outcomePool.get(outcomeToRemove);
        removeOutcomeAssociation2Behaviour(workingOutcome.getId(), workingOutcome.getSourceBehaviour());
        outcomePool.remove(outcomeToRemove);
    }

    public MTBehaviourOutcome getOutcome(MTBehaviourOutcomeIdentifier outcomeId){
        if(outcomeId == null){
            return(null);
        }
        if(!outcomePool.containsKey(outcomeId)){
            return(null);
        }
        return(outcomePool.get(outcomeId));
    }

    public void addOutcomeSet(MTBehaviourOutcomeSet outcomeSet){
        if(outcomeSet == null){
            return;
        }
        for(MTBehaviourOutcome outcome: outcomeSet.getOutcomes()){
            if(outcome.getSourceBehaviour() == null){
                outcome.setSourceBehaviour(outcomeSet.getSourceBehaviour());
            }
            if(outcome.getAffectingTwin() == null){
                outcome.setAffectingTwin(outcomeSet.getSourceTwin());
            }
            addOutcome(outcome);
        }
    }

    public void addOutcomeAssociation2Behaviour(MTBehaviourOutcomeIdentifier outcomeId, MTBehaviourIdentifier behaviourId){
        if(outcomeId == null || behaviourId == null){
            return;
        }
        if(!behaviour2OutcomeMap.containsKey(behaviourId)){
            HashSet<MTBehaviourOutcomeIdentifier> outcomeSet = new HashSet<>();
            behaviour2OutcomeMap.put(behaviourId, outcomeSet);
        }
        behaviour2OutcomeMap.get(behaviourId).add(outcomeId);
    }

    public void removeOutcomeAssociation2Behaviour(MTBehaviourOutcomeIdentifier outcomeId, MTBehaviourIdentifier behaviourId){
        if(outcomeId == null || behaviourId == null){
            return;
        }
        if(!behaviour2OutcomeMap.containsKey(behaviourId)){
            return;
        }
        Set<MTBehaviourOutcomeIdentifier> outcomeSet = behaviour2OutcomeMap.get(behaviourId);
        if(!outcomeSet.contains(outcomeId)){
            outcomeSet.remove(outcomeId);
            if(outcomeSet.isEmpty()){
                behaviour2OutcomeMap.remove(behaviourId);
            }
        }
    }

    public void removeOutcomesDerivedFromStimulus(MTStimulusIdentifier stimulusId){
        if(stimulusId == null){
            return;
        }
        Collection<MTBehaviourOutcome> outcomes = outcomePool.values();
        for(MTBehaviourOutcome outcome: outcomes) {
            if (outcome.getSourceStimulus() == stimulusId) {
                removeOutcome(outcome.getId());
            }
        }
    }

    public Set<MTBehaviourOutcomeIdentifier> getBehaviourBasedOutcomes(MTBehaviourIdentifier behaviourId){
        if(behaviourId == null){
            return(new HashSet<>());
        }
        if(!behaviour2OutcomeMap.containsKey(behaviourId)){
            return(new HashSet<>());
        }
        return(behaviour2OutcomeMap.get(behaviourId));
    }

    public Set<MTBehaviourOutcome> getStimulusDerivedOutcomes(MTStimulusIdentifier stimulusId){
        if(stimulusId == null){
            return(new HashSet<>());
        }
        HashSet<MTBehaviourOutcome> derivedOutcomes = new HashSet<>();
        Collection<MTBehaviourOutcome> outcomes = outcomePool.values();
        for(MTBehaviourOutcome outcome: outcomes){
            if(outcome.getSourceStimulus() == stimulusId){
                derivedOutcomes.add(outcome);
            }
        }
        return(derivedOutcomes);
    }
}
