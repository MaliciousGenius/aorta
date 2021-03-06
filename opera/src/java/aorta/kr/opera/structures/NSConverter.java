/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aorta.kr.opera.structures;

import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Var;
import aorta.kr.language.MetaLanguage;
import aorta.kr.language.model.Metamodel;
import aorta.kr.opera.ConversionUtils;
import aorta.kr.opera.OperAImportException;
import net.sf.ictalive.operetta.OM.Atom;
import net.sf.ictalive.operetta.OM.Conjunction;
import net.sf.ictalive.operetta.OM.DeonticModality;
import net.sf.ictalive.operetta.OM.DeonticStatement;
import net.sf.ictalive.operetta.OM.Disjunction;
import net.sf.ictalive.operetta.OM.Implication;
import net.sf.ictalive.operetta.OM.NS;
import net.sf.ictalive.operetta.OM.Negation;
import net.sf.ictalive.operetta.OM.Norm;
import net.sf.ictalive.operetta.OM.Objective;
import net.sf.ictalive.operetta.OM.PartialStateDescription;
import net.sf.ictalive.operetta.OM.RoleDeonticStatement;
import net.sf.ictalive.operetta.OM.SS;
import net.sf.ictalive.operetta.OM.TransitionNorm;

/**
 *
 * @author asj
 */
public class NSConverter {

	private static MetaLanguage ml = new MetaLanguage();
	
	public static void convert(NS ns, SS ss, Metamodel mm) throws OperAImportException {
		for (Norm norm : ns.getNorms()) {
			DeonticStatement dStmt = norm.getDeontics();
			if (dStmt instanceof RoleDeonticStatement) {
				RoleDeonticStatement rdStmt = (RoleDeonticStatement) dStmt;
				DeonticModality modality = rdStmt.getModality();
				if (modality == DeonticModality.OBLIGATION) {
					Struct objective = ConversionUtils.stateDescriptionToStruct(rdStmt.getWhat());
					Struct deadline = ConversionUtils.stateDescriptionToStruct(norm.getDeadline());
					
					Struct activation = getViolation(norm.getActivationCondition(), modality, ss);
					
					String name = rdStmt.getRole().getName();
					name = name.substring(0, 1).toLowerCase() + name.substring(1);
					mm.getNorms().add(new aorta.kr.language.model.Norm(name, aorta.kr.language.model.Norm.OBLIGATION, objective, deadline, activation));
				} else if (modality == DeonticModality.PROHIBITION) {
					Struct objective = ConversionUtils.stateDescriptionToStruct(rdStmt.getWhat());
					Struct expiration = ConversionUtils.stateDescriptionToStruct(norm.getExpirationCondition());
					
					Struct activation = getViolation(norm.getActivationCondition(), modality, ss);
					
					String name = rdStmt.getRole().getName();
					name = name.substring(0, 1).toLowerCase() + name.substring(1);
					mm.getNorms().add(new aorta.kr.language.model.Norm(name, aorta.kr.language.model.Norm.PROHIBITION, objective, expiration, activation));
				} else {
					throw new OperAImportException("Modality " + modality + " not supported!");
				}
			} else {
				throw new OperAImportException("Deontic Statement " + dStmt + " not supported!");
			}
		}
		
		for (TransitionNorm tNorm : ns.getTransitionNorms()) {
			// TODO: Why player and not role?
		}
	}
	
	private static Struct getViolation(PartialStateDescription psd, DeonticModality modality, SS ss) throws OperAImportException {
		Struct result;
		if (psd instanceof Atom) {
			Atom a = (Atom)psd;
			Term[] terms = new Term[a.getArguments().size()];
			for (int i = 0; i < a.getArguments().size(); i++) {
				net.sf.ictalive.operetta.OM.Term operaTerm = a.getArguments().get(i);
				terms[i] = ConversionUtils.operaTermToTerm(operaTerm);
			}
			result = new Struct(a.getPredicate(), terms);
		} else if (psd instanceof Negation) {
			Struct negatedFormula = getViolation(((Negation)psd).getStateFormula(), modality, ss);
			if (isObjective(negatedFormula, ss)) {
				result = ml.violation(
						new Var("Agent"), 
						new Var("Role"),  
						new Struct(modality == DeonticModality.OBLIGATION ? 
										aorta.kr.language.model.Norm.OBLIGATION : 
										aorta.kr.language.model.Norm.PROHIBITION),
						negatedFormula);
			} else {
				result = new Struct("\\+", getViolation(((Negation)psd).getStateFormula(), modality, ss));
			}
		} else if (psd instanceof Conjunction) {
			Conjunction c = (Conjunction) psd;
			result = new Struct(",", getViolation(c.getLeftStateFormula(), modality, ss), getViolation(c.getRightStateFormula(), modality, ss));
		} else if (psd instanceof Disjunction) {
			Disjunction d = (Disjunction) psd;
			result = new Struct(";", getViolation(d.getLeftStateFormula(), modality, ss), getViolation(d.getRightStateFormula(), modality, ss));			
		} else if (psd instanceof Implication) {
			Implication i = (Implication) psd;
			result = new Struct(";", new Struct("\\+", getViolation(i.getAntecedentStateFormula(), modality, ss)), getViolation(i.getConsequentStateFormula(), modality, ss));			
		} else {
			throw new OperAImportException("PartialStateDescription " + psd + " not supported!");
		}
		return result;
	}

	private static boolean isObjective(Struct formula, SS ss) throws OperAImportException {
		for (Objective o : ss.getObjectives()) {
			Struct objective = ConversionUtils.stateDescriptionToStruct(o.getStateDescription());
			if (objective.match(formula)) {
				return true;
			}
		}
		return false;
	}
	
}
