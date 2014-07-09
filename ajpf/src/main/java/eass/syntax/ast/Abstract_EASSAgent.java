// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis, Michael Fisher, Nicholas K. Lincoln, Alexei
// Lisitsa and Sandor M. Veres
//
// This file is part of the Engineering Autonomous Space Software (EASS) Library.
// 
// The EASS Library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// The EASS Library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with the EASS Library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package eass.syntax.ast;


import ail.util.AILexception;
import ail.mas.MAS;
import ail.semantics.AILAgent;
import ail.syntax.ast.Abstract_Agent;
import ail.syntax.ast.Abstract_Literal;
import ail.syntax.ast.Abstract_Plan;
import ail.syntax.ast.Abstract_Rule;
import ail.mas.DefaultEnvironment;
import gov.nasa.jpf.jvm.MJIEnv;
import eass.semantics.EASSAgent;


/**
 * A Gwendolen Agent - a demonstration of how to subclass an AIL Agent and
 * create a reasoning cycle.
 * 
 * @author louiseadennis
 *
 */
public class Abstract_EASSAgent extends Abstract_Agent { 
	private boolean isAbstraction = false;
	private String abstraction_for;

	/**
	 * Construct a Gwendolen agent from an architecture and a name.
	 * 
	 * @param arch the Agent Architecture.
	 * @param name te name of the agent.
	 */
	public Abstract_EASSAgent(String name) {
		super(name);
		// first we create an AIL Agent.		
 
		
	}
	
	public void setAbstraction(String agname) {
		isAbstraction = true;
		abstraction_for = agname;
	}
	
	public EASSAgent toMCAPL(MAS mas) {
		try{
		    	EASSAgent ag = new EASSAgent(mas, fAgName);
		    	for (Abstract_Literal l: beliefs) {
		    		ag.addInitialBel(l.toMCAPL());
		    	}
		    	for (Abstract_Rule r: rules) {
		    		ag.addRule(r.toMCAPL());
		    	}
		    	for (Abstract_Plan p: plans) {
		    		try {
		    			ag.addPlan(p.toMCAPL());
		    		} catch (Exception e) {
		    			e.printStackTrace();
		    		}
		    	}
		    	if (initialgoal != null) {
		    		ag.addInitialGoal(initialgoal.toMCAPL());
		    	}
		    	if (isAbstraction) {
		    		ag.setAbstraction(abstraction_for);
		    	}
		    	try {
		    		ag.initAg();
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    	return ag;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		   

	}

    public int newJPFObject(MJIEnv env) {
    	int objref = env.newObject("eass.syntax.ast.Abstract_EASSAgent");
    	env.setReferenceField(objref, "fAgName", env.newString(fAgName));
    	if (initialgoal != null) {
    		env.setReferenceField(objref, "initialgoal", initialgoal.newJPFObject(env));
    	}
    	int bRef = env.newObjectArray("ail.syntax.ast.Abstract_Literal", beliefs.length);
       	int rRef = env.newObjectArray("ail.syntax.ast.Abstract_Rule", rules.length);
       	int pRef = env.newObjectArray("ail.syntax.ast.Abstract_Plan", plans.length);
       	for (int i = 0; i < beliefs.length; i++) {
       		env.setReferenceArrayElement(bRef, i, beliefs[i].newJPFObject(env));
       	}
      	for (int i = 0; i < rules.length; i++) {
       		env.setReferenceArrayElement(rRef, i, rules[i].newJPFObject(env));
       	}
      	for (int i = 0; i < plans.length; i++) {
       		env.setReferenceArrayElement(pRef, i, plans[i].newJPFObject(env));
       	}
      	env.setReferenceField(objref, "beliefs", bRef);
      	env.setReferenceField(objref, "rules", rRef);
      	env.setReferenceField(objref, "plans", pRef);
  		env.setBooleanField(objref, "isAbstraction", isAbstraction);
      	if (isAbstraction) {
      		env.setReferenceField(objref, "abstraction_for", env.newString(abstraction_for));
      	}
      	return objref;
   	
    }

}
