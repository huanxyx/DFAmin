package minDFA;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class DFA {
	private static final int STATE_FAILURE = -1;		//失败状态
	private static final int CHAR_NUM = 128;			//字符数
	private int STATE_NUM;								//状态数
	
	private int[][] tran_table;							//DFA转换二维数组
	private boolean[] accept_table;						//接受状态集
	private Set<Character> char_table;					//使用字符集
	
	private List<TransformFunction> transList;			//保存转换函数集，主要用作输出
	
	//初始化有穷状态自动机
	public DFA( List<State> stateList, List<TransformFunction> transList){
		this.transList = transList;
		
		//初始化状态数
		STATE_NUM = stateList.size();
		
		tran_table = new int[STATE_NUM][CHAR_NUM];
		accept_table = new boolean[STATE_NUM];
		char_table = new HashSet<Character>();
		
		//初始化转换表
		for(int i = 0; i < STATE_NUM; i++) {
			for(int j = 0; j < CHAR_NUM; j++) {
				tran_table[i][j] = STATE_FAILURE;
			}
		}
		for(TransformFunction tf : transList) {
			int start = tf.getStartState();
			char ch = tf.getDriverChar();
			int end = tf.getEndState();
			
			tran_table[start][ch] = end;
			char_table.add(ch);
		}
		
		//初始化接受集
		for(State s : stateList) {
			accept_table[s.getStateNum()] = s.isAccept();
		}
	}
	
	//判断一个字符串是否能被接受
	public boolean judge(String str) {
		int currentState = 0;
		char[] input_buffer = str.toCharArray();
		
		for(char c : input_buffer) {
			currentState = next( currentState, c);
			if(currentState == STATE_FAILURE) 
				break;
		}
		return isAccept(currentState);
	}
	
	//传入一个字符返回下一个状态,STATE_FAILURE代表不能接受
	private int next(int state, char nextchar) {
		if(state < 0 || state >= STATE_NUM){
			return STATE_FAILURE;
		}
		return tran_table[state][nextchar];
	}
	
	//判断该状态能否被接受
	private boolean isAccept(int state) {
		if(state != STATE_FAILURE && accept_table[state]){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 对本DFA进行HopCraft最小化
	 * @return	HopCraft最小化的DFA
	 */	
	public DFA translateMinDFA() {
		int i;
		//用于容纳状态的等价集合
		List<List<Integer>> container = new LinkedList<List<Integer>>();	
		
		
		List<Integer> acceptL = new LinkedList<Integer>();
		List<Integer> nacceptL = new LinkedList<Integer>();
		container.add(acceptL);
		container.add(nacceptL);
		
		//分割成一个接受集，一个非接受集
		for(i = 0; i < accept_table.length; i++) {
			if(accept_table[i]) {
				acceptL.add(i);
			}else{
				nacceptL.add(i);
			}
		}
		//分割等价类
		boolean hasChange = true;					//表示了等价类修改了
		while( hasChange ) {
			hasChange = splitContainer(container);
		}	
		
		//用于求新的转换函数
		return produceMinDFA(container);			//根据该等价类获取DFA
	}
	
	/**
	 * 对container容器中的等价类进行分割
	 * @param container		等价类的容器
	 * @return				是否进行了分割（false代表否， true代表真）
	 */
	private boolean splitContainer( List<List<Integer>> container ) {
		
		for(List<Integer> list : container) {
			for (char inputCh : char_table) {
				if(splitList(inputCh, list, container)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 功能：使用指定驱动字符，分割等价类
	 * 返回：是否分割
	 * @param input			使用的字符
	 * @param list			分割的等价类
	 * @param container		等价类的容器
	 * @return				是否能进行分割
	 */
	private boolean splitList(char input, List<Integer> list, List<List<Integer>> container) {
		Map<Integer, List<Integer>> map = new HashMap();
		
		//获取了一个map集，
		//键：每个这个等价类中的状态能够转的到的等价类下标
		//值：一个List，里面保存了该转换到该等价类的状态
		for(int num : list) {
			
			int endstate = tran_table[num][input];
			int equalIndex = getListNum(endstate, container);
			if ( !map.containsKey(equalIndex) ) {			//若是不包含了该子类映射的容器
				List<Integer> temp = new LinkedList<Integer>();
				temp.add(num);
				map.put(equalIndex, temp);
			}else {
				List<Integer> temp = map.get(equalIndex);
				temp.add(num);
				map.put(equalIndex, temp);
			}
		}
		if(map.size() == 1) {
			//表示该字符无法分割该等价类
			return false;
		}else{
			//表示该字符能分割该等价类
			//1.删除该等价类
			container.remove(list);
			//2.添加分割后的等价类
			for(Map.Entry< Integer, List<Integer>> entry : map.entrySet()) {
				container.add(entry.getValue());				
			}
			return true;
		}
	}
	
	/**
	 * 获取一个状态所在的等价类的编号
	 * @param state				传入的状态
	 * @param container			等价类的容器
	 * @return					该状态所对应的等价类的编号
	 */
	private int getListNum(int state, List<List<Integer>> container) {
		for(int i = 0; i < container.size(); i++) {
			if( container.get(i).contains(state) ) {
				return i;
			}
		}
		return -1;						//代表着不能该状态没有转换到的等价类
	}
	
	/**
	 * 根据分割开来的等价类集合生成新的DFA
	 * @param container		等价类的容器
	 * @return	DFA
	 */
	private DFA produceMinDFA( List<List<Integer>> container) {
		List<State> stateList = new LinkedList<State>();
		List<TransformFunction> transList = new LinkedList<TransformFunction>();
		
		//将含有初始状态的等价类放在第一个
		for(List<Integer> list : container) {
			if(list.contains(0)) {
				container.remove(list);
				container.add(0, list);
			}
		}

		for(int i=0; i < container.size(); i++ ) {
			//1.将该等价类转换为一个状态
			List<Integer> list = container.get(i);

			boolean ac = false;
			for(int j : list) {
				if(accept_table[j]) {
					ac = true;
					break;
				}
			}
			stateList.add(new State(i, ac));
			
			//2.根据旧的转换函数生成新的转换函数
			int state = list.get(0);							//因为该等价类中所有的状态都是等价的，所有无需一个一个判断
			for (char inputCh : char_table) {
				int targetState = tran_table[state][inputCh];
				if( targetState != STATE_FAILURE ) {
					int target = getListNum( targetState, container);
					transList.add(new TransformFunction(i, inputCh, target));
				}
			}
		}
		
		return new DFA(stateList, transList);
	}
	
	/**
	 * 输出该DFA相关信息
	 */
	public void printDFA() {
		System.out.println("状态集合:");
		for(int i = 0; i < STATE_NUM-1; i++) {
			System.out.print(i + ",");
		}
		System.out.println(STATE_NUM - 1);
		System.out.println();
		
		int acc_num = 0;
		System.out.println("接受状态集:");
		for(int i = 0; i < accept_table.length; i++) {
			if(accept_table[i]) {
				if( 0 == acc_num ) {
					acc_num++;
					System.out.print(i);
				}else{
					System.out.print("," + i);
				}
			}
		}
		System.out.println();
		System.out.println();
		
		System.out.println("转换函数集:");
		for(TransformFunction tf : transList) {
			System.out.println(tf.getStartState() + " - " + tf.getDriverChar() + " -> " + tf.getEndState());
		}
	}
	
	
	/*
	 * 单元测试，info.txt测试用例
	 */
	public static void main(String[] args) throws FileNotFoundException {
		List<State> stateList = new ArrayList<State>();
		List<TransformFunction> transList = new ArrayList<TransformFunction>();
		
		//1. 获取状态集合
		Scanner sc = new Scanner(new File("info.txt"));
//		System.out.println("请输入状态集：状态名称 是否接受（1接受，0不接受）");
		while(sc.hasNext()) {
			String stateNum = sc.next();
			//若是为！，则表示输入完毕
			if( "!".equals(stateNum) ){
				break;
			}
			String stateAccept = sc.next(); 
			
			//获取状态集
			stateList.add( new State(Integer.parseInt(stateNum), 
								"1".equals(stateAccept) ? true : false) );
		}
		
		//2. 获取转换函数集合
//		System.out.println("请输入转换函数集：开始状态 驱动字符 转换后的状态");
		while(sc.hasNext()) {
			String stateStart = sc.next();
			//若是为！，则表示输入完毕
			if( "!".equals(stateStart) ){
				break;
			}
			String input = sc.next(); 
			String stateEnd = sc.next();
			
			//获取转换函数集
			transList.add( new TransformFunction(Integer.parseInt(stateStart), 
								input.charAt(0), Integer.parseInt(stateEnd)) );
		}
		
		//3.根据转换函数集合和状态集合创建DFA
		DFA dfa = new DFA(stateList, transList);
		
		dfa = dfa.translateMinDFA(); 
		dfa.printDFA();
		sc.close();
	}
}
