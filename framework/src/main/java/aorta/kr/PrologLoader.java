package aorta.kr;

import aorta.kr.util.Qualifier;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import alice.tuprolog.InvalidLibraryException;
import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Prolog;
import alice.tuprolog.Theory;

public final class PrologLoader {

	private Prolog prolog;

	public PrologLoader() throws InvalidLibraryException, InvalidTheoryException, IOException {
		prolog = new Prolog();

		prolog.addTheory(new Theory(getClass().getResourceAsStream("rules.pl")));
	}

	public void addTheory(Theory theory, KBType type) throws InvalidTheoryException, FileNotFoundException, IOException {
		try {
			Theory qTheory = Qualifier.qualifyTheory(prolog, type, theory);
			prolog.addTheory(qTheory);
		} catch (Error ex) {
			ex.printStackTrace();
		}
	}

	public void addTheory(String file, KBType type) throws InvalidTheoryException, FileNotFoundException, IOException {
		Theory theory = new Theory(new FileInputStream(file));
		addTheory(theory, type);
	}

	public Prolog load() {
		return prolog;
	}
}
