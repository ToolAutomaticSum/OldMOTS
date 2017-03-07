package optimize.parameter;

import java.util.Random;
import java.util.function.Function;

public class Parameter<T> {
	private String parameterName;
	private T parameterValue;
	private T minValue;
	private T maxValue;

	public Parameter(String parameterName) {
		super();
		this.parameterName = parameterName;
	}

	public Parameter(String parameterName, T parameterValue) {
		super();
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
	}
	
	public Parameter<T> aleaParameter (Random rand, Function<Random, T> creator) {
		Parameter<T> param = new Parameter<T>(parameterName, creator.apply(rand));
		param.setMinValue(minValue);
		param.setMaxValue(maxValue);
		return param;
	}
	
	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public T getValue() {
		return parameterValue;
	}

	public void setValue(T value) {
        this.parameterValue = value;
    }
	
	public Class<?> getParameterClass() {
		return parameterValue.getClass();
	}

	public T getMinValue() {
		return minValue;
	}

	public void setMinValue(T minValue) {
		this.minValue = minValue;
	}

	public T getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(T maxValue) {
		this.maxValue = maxValue;
	}
	
	@Override
	public String toString() {
		return parameterName + " : " + parameterValue.toString();
	}
}
