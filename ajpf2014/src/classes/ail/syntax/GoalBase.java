// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis, and  Michael Fisher 
//
// This file is part of the Agent Infrastructure Layer (AIL)
// 
// The AIL is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// The AIL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with the AIL; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package ail.syntax;

import gov.nasa.jpf.annotation.FilterField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ajpf.util.VerifyMap;
import ajpf.util.VerifySet;

/**
 * A Goal Base is a set of goals.  We need to implement this properly.
 * @author lad
 *
 */
public class GoalBase {
	/**
     * goalMap is a table ussed for efficient
     * look-up of goals.
     */
    private Map<PredicateIndicator, Set<Goal>> goalMap = new VerifyMap<PredicateIndicator, Set<Goal>>();
    
    /**
     * Number of goals.
     */
    @FilterField
    private int size = 0;
    
    /**
     * Getter for the number of goals.
     * @return number of goals.
     */
    public int size() {
        return size;
    }

    /**
     * Returns all goals in the goal base.
     * @return
     */
    public ArrayList<Goal> getAll() {
    	ArrayList<Goal> list = new ArrayList<Goal>();
    	for (Set<Goal> gs: goalMap.values()) {
    		list.addAll(gs);
    	}
    	
    	return list;
    }

 	/**
     * Add a goal to the goal base.
     * 
     */
    public void add(Goal gin) {
       	Goal g = (Goal) gin.strip_varterm();
         
       	Set<Goal> entry = goalMap.get(g.getPredicateIndicator());
       	if (entry == null) {
       		entry = new VerifySet<Goal>();
       		goalMap.put(g.getPredicateIndicator(), entry);
       	}
       	entry.add(g); 
       	size++;
     }

    /**
     * Removing a goal from the Goal Base.
     */
    public boolean remove(Goal g) {
		PredicateIndicator key = g.getPredicateIndicator();
    	if (goalMap.containsKey(key) && goalMap.get(key).contains(g)) {
    		Set<Goal> entry = goalMap.get(key);
    		entry.remove(g);
    		if (entry.isEmpty()) {
    			goalMap.remove(key);
    		}
    		size--;
    		return true;
    	} 
    	return false; 
    }

    /**
     * Returns an iterators over all goals.
     */
    public Iterator<Goal> iterator() {
        List<Goal> all = new ArrayList<Goal>(size());
        for (Set<Goal> gs :goalMap.values()) {
            all.addAll(gs);
       }
        return all.iterator();
	}
    

    /**
     * Getter for the predicate indicators in use.
     * @return
     */
    public Set<PredicateIndicator> getPIs() {
    	return goalMap.keySet();
    }

    /**
     * Get a goal relevant to some literl - i.e., a goal with the
     * same predicate name and arity.  Presumably you can then check for
     * unifiability.
     * 
     * @param l  The literal to search against.
     * @return	An iterators of goals in the goal base with the same
     *          predicate name and arity.
     */
    public Iterator<Goal> getRelevant(Literal l) {
    	if (l.isVar()) {
            // all goals are relevant
            return iterator();
        } else {
            Set<Goal> entry = goalMap.get(l.getPredicateIndicator());
            if (entry != null) {
                List<Goal> entrylist = new ArrayList<Goal>();
                entrylist.addAll(entry);
                return entrylist.iterator();
           } else {
            	ArrayList<Goal> empty = new ArrayList<Goal>();
                return empty.iterator();
            }
        }
        
     }

    /**
     * Convert the goal base into a string for printing.
     *
     */
    public String toString() {
    	return (goalMap.toString());
     }

}
