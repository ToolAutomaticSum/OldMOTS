package main.java.liasd.asadera.optimize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import main.java.liasd.asadera.model.task.AbstractMethod;
import main.java.liasd.asadera.optimize.parameter.ADN;
import main.java.liasd.asadera.optimize.parameter.Parameter;

public abstract class Individu extends AbstractMethod {
	protected HashMap<String, Class<?>> supportADN;
	protected ADN adn;

	public Individu(int id) throws SupportADNException {
		super(id);
		
		supportADN = new HashMap<String, Class<?>>();
	}

	public ADN getADN() {
		return adn;
	}

	public boolean setADN(ADN adn) {
		if (adn.isAdnCorrect(supportADN)) {
			this.adn = adn;
			return true;
		} else
			return false;
	}

	public HashMap<String, Class<?>> getSupportADN() {
		return supportADN;
	}

	public void setSupportADN(HashMap<String, Class<?>> supportADN) {
		this.supportADN = supportADN;
	}

	public final ADN generateAleaADN(Random random) {
		ADN aleaADN = new ADN(supportADN);
		Iterator<String> parameterIt = adn.keySet().iterator();
		while (parameterIt.hasNext()) {
			String parameterName = parameterIt.next();
			if (aleaADN.getParameterClass(parameterName) == Integer.class)
				aleaADN.putParameter(
						generateGenericAleaParameter(random, adn.getParameter(Integer.class, parameterName)));
			else if (aleaADN.getParameterClass(parameterName) == Boolean.class)
				aleaADN.putParameter(
						generateGenericAleaParameter(random, adn.getParameter(Boolean.class, parameterName)));
			else if (aleaADN.getParameterClass(parameterName) == Float.class)
				aleaADN.putParameter(
						generateGenericAleaParameter(random, adn.getParameter(Float.class, parameterName)));
			else if (aleaADN.getParameterClass(parameterName) == Double.class)
				aleaADN.putParameter(
						generateGenericAleaParameter(random, adn.getParameter(Double.class, parameterName)));
		}
		return aleaADN;
	}

	@SuppressWarnings("unchecked")
	public static <T> Parameter<T> generateGenericAleaParameter(Random random, Parameter<T> param) {
		if (param.getParameterClass() == Integer.class) {
			Parameter<Integer> temp = (Parameter<Integer>) param;
			final int minValue;
			final int maxValue;
			if (temp.getMinValue() != null && temp.getMaxValue() != null) {
				minValue = temp.getMinValue();
				maxValue = temp.getMaxValue();
			} else {
				minValue = 0;
				maxValue = 2 * temp.getValue() + 1;
			}
			return (Parameter<T>) temp.aleaParameter(random, rand -> rand.nextInt(maxValue - minValue) + minValue);
		} else if (param.getParameterClass() == Boolean.class) {
			Parameter<Boolean> temp = (Parameter<Boolean>) param;
			return (Parameter<T>) temp.aleaParameter(random, rand -> rand.nextBoolean());
		} else if (param.getParameterClass() == Float.class) {
			Parameter<Float> temp = (Parameter<Float>) param;
			final float minValue;
			final float maxValue;
			if (temp.getMinValue() != null && temp.getMaxValue() != null) {
				minValue = temp.getMinValue();
				maxValue = temp.getMaxValue();
			} else {
				minValue = 0;
				maxValue = 2 * temp.getValue() + 1;
			}
			return (Parameter<T>) temp.aleaParameter(random,
					(rand -> rand.nextFloat() * (maxValue - minValue) + minValue));
		} else if (param.getParameterClass() == Double.class) {
			Parameter<Double> temp = (Parameter<Double>) param;
			final double minValue;
			final double maxValue;
			if (temp.getMinValue() != null && temp.getMaxValue() != null) {
				minValue = temp.getMinValue();
				maxValue = temp.getMaxValue();
			} else {
				minValue = 0;
				maxValue = 2 * temp.getValue() + 1;
			}
			return (Parameter<T>) temp.aleaParameter(random,
					(rand -> rand.nextDouble() * (maxValue - minValue) + minValue));
		} else
			return null;
	}

	public static <T extends Enum<?>> T randomEnum(Random rand, Class<T> clazz) {
		int x = rand.nextInt(clazz.getEnumConstants().length);
		return clazz.getEnumConstants()[x];
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
