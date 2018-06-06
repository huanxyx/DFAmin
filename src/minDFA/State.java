package minDFA;

public class State {
	private int stateNum;
	private boolean accept;
	
	public State(int stateNum, boolean accept) {
		super();
		this.stateNum = stateNum;
		this.accept = accept;
	}
	
	public int getStateNum() {
		return stateNum;
	}
	public void setStateNum(int stateNum) {
		this.stateNum = stateNum;
	}
	public boolean isAccept() {
		return accept;
	}
	public void setAccept(boolean accept) {
		this.accept = accept;
	}
}
