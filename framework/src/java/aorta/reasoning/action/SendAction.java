/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aorta.reasoning.action;

import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import aorta.AORTAException;
import aorta.AgentState;
import aorta.kr.KBType;
import aorta.kr.MentalState;
import aorta.kr.language.MetaLanguage;
import aorta.kr.util.FormulaQualifier;
import aorta.msg.OutgoingOrganizationalMessage;
import aorta.tracer.Tracer;
import aorta.ts.TransitionNotPossibleException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import aorta.logging.Logger;
import aorta.reasoning.fml.Formula;
import aorta.ts.rules.ActionExecution;

/**
 *
 * @author ascje
 */
public class SendAction extends Action {

	private static final Logger logger = Logger.getLogger(SendAction.class.getName());

	protected Term recipients;
	protected Formula message;

	public SendAction(Term recipient, Formula message) {
		this.recipients = recipient;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "send(" + recipients + ", " + message + ")";
	}

	public Formula getMessage() {
		return message;
	}

	public Term getRecipient() {
		return recipients;
	}

	@Override
	protected AgentState executeAction(Term option, AgentState state) throws AORTAException {
		AgentState newState = state;
		
		MentalState ms = state.getMentalState();
		
		Term clonedMsgTerm = Term.createTerm(message.toString());		
		clonedMsgTerm = ms.unify(clonedMsgTerm, state.getBindings());
		
		Term recipientsTerm = Term.createTerm(recipients.toString());
		recipientsTerm = ms.unify(recipientsTerm, state.getBindings());
				
		List<Term> rcpList = new ArrayList<>();
		if (recipientsTerm.getTerm().isList()) {
			Struct lt = (Struct) recipientsTerm.getTerm();
			Iterator<? extends Term> it = lt.listIterator();
			while (it.hasNext()) {
				rcpList.add(it.next());
			}		
		} else {
			rcpList.add(recipientsTerm.getTerm());
		}
		
		if (!clonedMsgTerm.isGround()) {
			throw new AORTAException("Cannot execute action: term '" + clonedMsgTerm + "' is not ground.");
		} else {
			try {
				sendMessage(rcpList, clonedMsgTerm, newState);
			} catch (TransitionNotPossibleException ex) {
				return null;
			}
		}
		
		return newState;
	}

	protected void sendMessage(List<Term> recipientList, Term message, AgentState state) throws TransitionNotPossibleException {
		MetaLanguage ml = new MetaLanguage();
		
		for (Term recipient : recipientList) {
			// add mental note that the message was sent to a recipient
			Struct sent = ml.sent(recipient, message);
			state.insertInMentalState(FormulaQualifier.qualifyStruct(sent, KBType.BELIEF));
			state.notifyTermAdded(new ActionExecution().getName(), sent);
		}
		OutgoingOrganizationalMessage msg = new OutgoingOrganizationalMessage(recipientList, message);
		state.sendMessage(msg);
		
		logger.finer("[" + state.getAgent().getName() + "] Executing action: send(" + recipientList + "," + message + ")");
		Tracer.queue(state.getAgent().getName(), "send(" + recipientList + "," + message + ")");
	}
	
}
