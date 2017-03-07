package optimize;

public abstract class Optimize extends Individu {
	
	protected Double score;
	
	public Optimize(int id) throws SupportADNException {
		super(id);
	}

	public abstract void init() throws Exception;
	
	public abstract void optimize() throws Exception;
	
	public abstract double getScore();

	public void setScore(Double score) {
		this.score = score;
	}
}
