package optimize.parameter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class ADN extends HashMap<String, Class<?>> implements Comparable<ADN> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8392450232637694377L;
	
	private HashMap<String, Parameter<Class<?>>> mapNameParam = new HashMap<String, Parameter<Class<?>>>();
	private double score = 0;
	
	public ADN(HashMap<String, Class<?>> listParam) {
		super(listParam);
		for(String s : this.keySet())
			mapNameParam.put(s, null);
	}
	
	public Class<?> getParameterClass(String parameterName) {
		return get(parameterName);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Parameter<T> getParameter(Class<T> key, String parameterName) {
		if(key == null) {
			throw new NullPointerException("No null keys are allowed in this code");
		}
		if (get(parameterName) != key)
			throw new ClassFormatError("Class<T> " + key + " for parameter " + parameterName + " isn't good, it should be : " + get(parameterName));
		Parameter<T> p = (Parameter<T>) mapNameParam.get(parameterName);
		return p;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getParameterValue(Class<T> key, String parameterName) {
		if(key == null) {
			throw new NullPointerException("No null keys are allowed in this code");
		}
		if (get(parameterName) != key)
			throw new ClassFormatError("Class<T> " + key + " for parameter " + parameterName + " isn't good, it should be : " + get(parameterName));
		Parameter<T> p = (Parameter<T>) mapNameParam.get(parameterName);
		return p.getValue();
	}
	
	@SuppressWarnings("unchecked")
	public <T> void putParameter(Parameter<T> p) {
		String parameterName = p.getParameterName();
		if (!(get(parameterName) == p.getParameterClass()))
			throw new ClassFormatError("T class of Parameter<T> " + parameterName + " isn't good, it should be : " + get(parameterName));
		mapNameParam.put(parameterName, (Parameter<Class<?>>) p);
	}
	
	public boolean isAdnCorrect(HashMap<String, Class<?>> adn) {
		boolean correct = true;
		ADN temp = this;
		Iterator<String> adnIt = adn.keySet().iterator();
		while(correct && adnIt.hasNext()) {
			String adnParam = adnIt.next();
			if (temp.get(adnParam) != adn.get(adnParam))
				correct = false;
		}
		if (correct && !adn.keySet().equals(this.keySet()))
			correct = false;
		return correct;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(ADN o) {
		return (int) -Math.signum(this.getScore() - o.getScore());
	}

	@Override
	public String toString() {
		String str = "";
		Iterator<Parameter<Class<?>>> paramIt = mapNameParam.values().iterator();
		while (paramIt.hasNext())
			str += paramIt.next().toString() + "\n";
		return str;
	}
	
	public static ADN croisementADN(Random random, ADN pere, ADN mere) {
		ADN enfant = new ADN(pere);
		Iterator<String> paramNameIt = enfant.keySet().iterator();
		while (paramNameIt.hasNext()) {
			String parameterName = paramNameIt.next();
			if (random.nextBoolean()) {
				enfant.putParameter(pere.getParameter(pere.getParameterClass(parameterName), parameterName));
			}
			else {
				enfant.putParameter(mere.getParameter(mere.getParameterClass(parameterName), parameterName));
			}
		}
		return enfant;
	}
}
