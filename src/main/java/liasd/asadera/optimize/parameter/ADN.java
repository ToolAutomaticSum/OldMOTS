package main.java.liasd.asadera.optimize.parameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * map Name of param / class of param
 * 
 * @author valnyz
 *
 */
public class ADN implements Comparable<ADN>, Map<String, Class<?>> {

	/**
	 * map Name of param / class of param
	 */
	private HashMap<String, Class<?>> mapNameClass = new HashMap<String, Class<?>>();
	/**
	 * map name of param / param itself
	 */
	private HashMap<String, Parameter<Class<?>>> mapNameParam = new HashMap<String, Parameter<Class<?>>>();

	private double score = 0;

	public ADN(HashMap<String, Class<?>> supportADN) {
		mapNameClass = new HashMap<String, Class<?>>(supportADN);
		for (String s : this.keySet())
			mapNameParam.put(s, null);
	}

	/**
	 * @param adn
	 */
	public ADN(ADN adn) {
		mapNameClass = new HashMap<String, Class<?>>(adn);
		for (String parameterName : this.keySet()) {
			if (adn.getParameterClass(parameterName) == Integer.class) {
				putParameter(
						new Parameter<Integer>(parameterName, adn.getParameterValue(Integer.class, parameterName)));
			} else if (adn.getParameterClass(parameterName) == Boolean.class) {
				putParameter(
						new Parameter<Boolean>(parameterName, adn.getParameterValue(Boolean.class, parameterName)));
			} else if (adn.getParameterClass(parameterName) == Float.class) {
				putParameter(new Parameter<Float>(parameterName, adn.getParameterValue(Float.class, parameterName)));
			} else if (adn.getParameterClass(parameterName) == Double.class) {
				putParameter(new Parameter<Double>(parameterName, adn.getParameterValue(Double.class, parameterName)));
			}
		}
	}

	public Class<?> getParameterClass(String parameterName) {
		return get(parameterName);
	}

	@SuppressWarnings("unchecked")
	public <T> Parameter<T> getParameter(Class<T> key, String parameterName) {
		if (key == null) {
			throw new NullPointerException("No null keys are allowed in this code");
		}
		if (get(parameterName) != key)
			throw new ClassFormatError("Class<T> " + key + " for parameter " + parameterName
					+ " isn't good, it should be : " + get(parameterName));
		Parameter<T> p = (Parameter<T>) mapNameParam.get(parameterName);
		return p;
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameterValue(Class<T> key, String parameterName) {
		if (key == null) {
			throw new NullPointerException("No null keys are allowed in this code");
		}
		if (get(parameterName) != key)
			throw new ClassFormatError("Class<T> " + key + " for parameter " + parameterName
					+ " isn't good, it should be : " + get(parameterName));
		Parameter<T> p = (Parameter<T>) mapNameParam.get(parameterName);
		return p.getValue();
	}

	@SuppressWarnings("unchecked")
	public <T> void putParameter(Parameter<T> p) {
		String parameterName = p.getParameterName();
		if (!(get(parameterName) == p.getParameterClass()))
			throw new ClassFormatError(
					"T class of Parameter<T> " + parameterName + " isn't good, it should be : " + get(parameterName));
		mapNameParam.put(parameterName, (Parameter<Class<?>>) p);
	}

	public boolean isAdnCorrect(HashMap<String, Class<?>> adn) {
		boolean correct = true;
		ADN temp = this;
		Iterator<String> adnIt = adn.keySet().iterator();
		while (correct && adnIt.hasNext()) {
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
			str += paramIt.next().toString() + "\t";
		return str;
	}

	public String toValueString() {
		String str = "";
		Iterator<Parameter<Class<?>>> paramIt = mapNameParam.values().iterator();
		while (paramIt.hasNext())
			str += paramIt.next().toValueString() + "\t";
		return str;
	}

	public static ADN croisementADN(Random random, ADN pere, ADN mere) {
		ADN enfant = new ADN(pere);
		Iterator<String> paramNameIt = enfant.keySet().iterator();
		while (paramNameIt.hasNext()) {
			String parameterName = paramNameIt.next();
			if (random.nextBoolean()) {
				enfant.putParameter(pere.getParameter(pere.getParameterClass(parameterName), parameterName));
			} else {
				enfant.putParameter(mere.getParameter(mere.getParameterClass(parameterName), parameterName));
			}
		}
		return enfant;
	}

	public HashMap<String, Parameter<Class<?>>> getMapNameParam() {
		return mapNameParam;
	}

	@Override
	public void clear() {
		mapNameParam.clear();
		mapNameClass.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return mapNameClass.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return mapNameClass.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<String, Class<?>>> entrySet() {
		return mapNameClass.entrySet();
	}

	@Override
	public Class<?> get(Object arg0) {
		return mapNameClass.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return mapNameClass.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return mapNameClass.keySet();
	}

	@Override
	public Class<?> put(String arg0, Class<?> arg1) {
		mapNameParam.put(arg0, null);
		return mapNameClass.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Class<?>> arg0) {
		mapNameClass.putAll(arg0);
		for (String s : arg0.keySet())
			mapNameParam.put(s, null);
	}

	@Override
	public Class<?> remove(Object arg0) {
		mapNameParam.remove(arg0);
		return mapNameClass.remove(arg0);
	}

	@Override
	public int size() {
		return mapNameClass.size();
	}

	@Override
	public Collection<Class<?>> values() {
		return mapNameClass.values();
	}
}
