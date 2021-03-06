parser grammar AORTAParser;

@header {
import alice.tuprolog.Number;
import alice.tuprolog.Int;
import alice.tuprolog.Struct;
import alice.tuprolog.Prolog;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.Var;

import aorta.kr.*;
import aorta.kr.language.*;
import aorta.parser.helper.*;
import aorta.reasoning.*;
import aorta.reasoning.action.*;
import aorta.reasoning.fml.*;

import java.io.IOException;
}

options {   tokenVocab = AORTALexer; }

aortaAgent[String name] returns [AgentBuilder agent] 
	: {
	   Initialization init = new Initialization();
	   List<ReasoningRule> rules = new ArrayList<>();
	   } 
	(r=rules {rules = $r.r;})
	EOF
	{
	 $agent = new AgentBuilder(name, init, rules);  
	 }
;
rules returns [List<ReasoningRule> r]
    :  { $r = new ArrayList<>(); }
    ( ifRule { $r.add($ifRule.rule); }
    | actRule { $r.add($actRule.rule); } )+;

ifRule returns [IfRule rule]
    : IF formulas START_BLOCK rules END_BLOCK
      { $rule = new IfRule($formulas.fml, $rules.r); }
;
actRule returns [ActionRule rule]
    : option COLON formulas EXECUTE action FULLSTOP
	  { $rule = new ActionRule($option.fml, $formulas.fml, $action.aa); }
;

option returns [Term fml] 
	: {boolean pos = true;}
	(
	  (NOT {pos=false;})? ROLE START term END { $fml = new Struct("role", $term.fml); if (!pos) { $fml = new Struct("~", $fml); } }
	| (NOT {pos=false;})? OBJ START term END { $fml = new Struct("obj", $term.fml); if (!pos) { $fml = new Struct("~", $fml); } }
	| SEND START t1=term COMMA illForce COMMA t2=term END  { $fml = new Struct("send", $t1.fml, $illForce.fml, $t2.fml); }
	| NORM START r=term COMMA deon=DEON COMMA o=term COMMA d=term END  { $fml = new Struct("norm", $r.fml, new Struct($deon.text), $o.fml, $d.fml); }
	| VIOL START a=term COMMA r=term COMMA deon=DEON COMMA o=term END  { $fml = new Struct("viol", $a.fml, $r.fml, new Struct($deon.text), $o.fml); }
	| TRUE { $fml = Term.TRUE; }
	);

illForce returns [Term fml]
	: TELL { $fml = new Struct("tell"); }
	| ACHIEVE { $fml = new Struct("achieve"); };

formulas returns [Formula fml]
	: (formula { $fml = $formula.fml; }
	| TRUE { $fml = new TrueFormula(); }
	| formula COMMA fmls=formulas { $fml = new ConjunctFormula($formula.fml, $fmls.fml); }
	| NOT formula { $fml = new NegatedFormula($formula.fml); }
	| NOT formula COMMA fmls=formulas { $fml = new ConjunctFormula(new NegatedFormula($formula.fml), $fmls.fml); }	  
	| NOT START fmls=formulas END { $fml = new NegatedFormula($fmls.fml); }
	);
formula returns [Formula fml]
	: (OPT START prolog END { $fml = new OptionFormula($prolog.fml); }
	  | BEL START prolog END { $fml = new BeliefFormula($prolog.fml); }
	  | GOAL START prolog END { $fml = new GoalFormula($prolog.fml); }
	  | ORG START prolog END  { $fml = new OrganizationalFormula($prolog.fml); }
	  | CAP START term END { $fml = new CapabilityFormula($term.fml); } );
action returns [Action aa]
	: ( ENACT START pl=term END { $aa = new EnactAction($pl.fml); }
      | DEACT START pl=term END { $aa = new DeactAction($pl.fml); }
      | COMMIT START pl=term END { $aa = new CommitAction($pl.fml); }
      | DROP START pl=term END  { $aa = new DropAction($pl.fml); }
	  | SEND START ag=term COMMA fml=formula END { $aa = new SendAction($ag.fml, $fml.fml); }
	);

prolog returns [Term fml]: prolog2 { $fml = Term.createTerm($prolog2.text); };
prolog2 returns [Term fml]
	: (COMMA pl=prolog2 { $fml = new Struct(",", $pl.fml); }
	  | SEMICOLON pl=prolog2 { $fml = new Struct(";", $pl.fml); } // TODO: MAKE SEMICOLON WORK
	  | START pl=prolog2 END prolog2 { $fml = $pl.fml; }
	  | UNARY_OP pl=prolog2 { $fml = new Struct($UNARY_OP.text, $pl.fml); } 
	  | termBuilder prolog2  { $fml = $termBuilder.fml; }
	  | );
termBuilder returns [Term fml]
	: ( t3=term IS t4=term MATH_OP t5=term { $fml = new Struct("is", $t3.fml, new Struct($MATH_OP.text, $t4.fml, $t5.fml)); } 
	| term { $fml = $term.fml; }
	| t1=term BINARY_OP t2=term { $fml = new Struct($BINARY_OP.text, $t1.fml, $t2.fml); } );
term returns [Term fml]
	: (formula {$fml = new Struct(((ReasoningFormula)$formula.fml).getType(), ((ReasoningFormula)$formula.fml).getFormula());}
	  | UNARY_OP t=term {$fml = new Struct($UNARY_OP.text, $t.fml);}
	  | string {$fml = $string.fml; }
	  | struct {$fml = $struct.fml;}
	  | atom {$fml = $atom.fml;}
	  | var {$fml = $var.fml;}
	  | number {$fml = $number.fml;})
;
string returns [Struct fml]: FILEPATH { $fml = new Struct($FILEPATH.text); };
atom returns [Struct fml]: ATOM { $fml = new Struct($ATOM.text); };
number returns [Number fml] : NUMBER { String numStr = $NUMBER.text; $fml = aorta.kr.language.NumberConverter.parseNumber(numStr); };
var returns [Var fml]: VAR { $fml = new Var($VAR.text); };
struct returns [Struct fml]
	: (ATOM START args END { $fml = new Struct($ATOM.text, $args.fml.toArray(new Term[0])); }
	| list  { $fml = $list.fml; });
args returns [List<Term> fml]
	: { $fml = new ArrayList<>(); } 
	(term { $fml.add($term.fml); }
	| term COMMA a=args { $fml.add($term.fml); $fml.addAll($a.fml); } );
list returns [Struct fml]
	: START_BRACKET listContents END_BRACKET { $fml = $listContents.fml; }
	| START_BRACKET t1=term PIPE t2=term END_BRACKET { $fml = new Struct($t1.fml, $t2.fml); };
listContents returns [Struct fml]
	: { $fml = new Struct(); }
	(listItem { $fml.append($listItem.fml); } 
	| listItem COMMA lc=listContents { $fml.append($listItem.fml); $fml.append($lc.fml); });
listItem returns [Term fml]: prolog2 { $fml = $prolog2.fml; };