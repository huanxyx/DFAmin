package minDFA;


//转换函数，包括转换的开始状态，驱动字符，结束状态
public class TransformFunction {
	private int startState;
	private char driverChar;
	private int endState;
	public TransformFunction(int startState, char driverChar, int endState) {
		super();
		this.startState = startState;
		this.driverChar = driverChar;
		this.endState = endState;
	}
	public int getStartState() {
		return startState;
	}
	public void setStartState(int startState) {
		this.startState = startState;
	}
	public char getDriverChar() {
		return driverChar;
	}
	public void setDriverChar(char driverChar) {
		this.driverChar = driverChar;
	}
	public int getEndState() {
		return endState;
	}
	public void setEndState(int endState) {
		this.endState = endState;
	}
}
