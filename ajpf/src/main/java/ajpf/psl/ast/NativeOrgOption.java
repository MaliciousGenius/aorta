// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis, and  Michael Fisher 
//
// This file is part of Agent JPF (AJPF)
// 
// AJPF is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// AJPF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with AJPF; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package ajpf.psl.ast;

import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.JVM;


/**
 * The formula Opt(a, phi) - a believes that phi is an organizational option
 * 
 * @author louiseadennis
 * 
 */
public class NativeOrgOption extends Native_Proposition {
	
	/**
	 * The agent which is required to believe the formula.
	 */
	private String agent;
	/**
	 * The formula the agent believes.
	 */
	private Abstract_Formula fmla;
		
	/**
	 * Constructor.
	 * 
	 * @param a  The agent which believes the formula.
	 * @param f  The formula that is believed.
	 */
	public NativeOrgOption(String a, Abstract_Formula f) {
		fmla = f;
		agent = a;
	}
	
		
	/*
	 * (non-Javadoc)
	 * @see mcapl.psl.Proposition#equals(mcapl.psl.MCAPLProperty)
	 */
	public boolean equals(Object p) {
		if (p instanceof NativeOrgOption) {
			return (((NativeOrgOption) p).getOption().equals(fmla) && ((NativeOrgOption) p).getAgent().equals(agent));
		}
		
		return false;
	}
	public int hashCode() {
		return objref;
	}
	
	/**
	 * Getter method for the MCAPL Formula to be believed.
	 * 
	 * @return the formula that should be believed.
	 */
	public Abstract_Formula getOption() {
		return fmla;
	}
	
	/**
	 * Getter method for the Agent.
	 * 
	 * @return the agent who should believe the belief.
	 */
	public String getAgent() {
		return agent;
	}
	
	/**
	 * We don't need to clone the agent - its the same one we want to query.
	 */
	public NativeOrgOption clone() {
		return(new NativeOrgOption(agent, fmla));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "Opt(" + agent + "," + fmla + ")";
		return s;
	}
	    
	public String getEquivalentJPFClass() {
		return "ajpf.psl.ast.Abstract_OrgOption";
	}
	
	
	public int createInJPF(JVM vm) {
		int objref = super.createInJPF(vm);
		ElementInfo ei = vm.getElementInfo(objref);
		ei.setReferenceField("fmla", fmla.createInJPF(vm));
		ei.setReferenceField("agent", vm.getHeap().newString(agent, vm.getLastThreadInfo()));
		return objref;
	}
	
	public int quickCompareVal() {
		return 20;
	}

}
