package SerendipityEvaluationModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class SerendipityEvaluation
{
	static List<Vertex> Graph=new ArrayList<>();
	static List<Vertex> GraphN;
	static List<Vertex> Graph2=new ArrayList<>();
	static Map<Integer,Vertex> Graph2M;
	static Map<Integer,Vertex> Graph2N;
	static List<Vertex> Graph3=new ArrayList<>();
	static List<Integer> Vps=new ArrayList<>();
	static Map<String,Integer> nameToIndex=new HashMap<>();
	static Sheet sheetiv;
//	static Sheet sheetpt;
//	static Sheet sheetpt2;
//	static Sheet sheetsc;
	static int pathIdx;
	static int N;

	static class Vertex
	{
		int index,domain,level;
		String name;
		Set<Integer> link;

		public Vertex(int index,String name)
		{
			this.index=index;
			this.name=name;
			this.link=new HashSet<>();
		}

		public int getIndex()
		{
			return index;
		}
	}

	static class Context
	{
		int v;
		Iterator<Integer> it;

		public Context(Map<Integer,Vertex> graph,int v)
		{
			this.v=v;
			this.it=graph.get(v).link.iterator();
		}
	}

	/**
	 * The main function of the realization, which initializes graph model and prepares to evaluate serendipity value.
	 * @param args Command-line parameter
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws NoPathException 
	 */
	public static void main(String[] args) throws NumberFormatException,IOException,NoPathException
	{
		Workbook workbook=new SXSSFWorkbook();
		sheetiv=workbook.createSheet("InterestVal");
	//	sheetpt=workbook.createSheet("Path");
	//	sheetpt2=workbook.createSheet("Path2");
	//	sheetsc=workbook.createSheet("Scoring");
		createHeader(sheetiv,"Node","InterestVal", "Normalized");
	//	createHeader(sheetpt,"p","Path");
	//	createHeader(sheetpt2,"p","v_s","p'","Path");
	//	createHeader(sheetsc,"p","p'","Pre-encountering","Post-encountering","Discovery","Score");
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String line=null;
		List<String> triple=new ArrayList<>();
		int lv=0,root=getIndex(":ROOT");
		Graph.get(root).level=0;
		while((line=br.readLine())!=null)
		{
			Matcher match=Pattern.compile("#+ *(\\d)-Level *#+").matcher(line.trim());
			if(match.find())
			{
				lv=Integer.parseInt(match.group(1));
				if(lv>N)N=lv;
			}
		}
		br.close();
		br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		while((line=br.readLine())!=null)
		{
			line=line.trim();
			if(line.startsWith("@prefix"))continue;
			Matcher match=Pattern.compile("#+ *(\\d)-Level *#+").matcher(line);
			if(match.find())
			{
				lv=Integer.parseInt(match.group(1));
				continue;
			}
			line=line.replaceAll("#.*$","").trim();
			if(line.length()==0)continue;
			String[] tokens=line.split("( |\t)+");
			for(String token : tokens)
			{
				switch(token)
				{
				case ";":
					triple.remove(triple.size()-1);
					triple.remove(triple.size()-1);
					break;
				case ".":
					triple.clear();
					break;
				default:
					triple.add(token);
					break;
				}
				if(triple.size()==3)
				{
					int s=getIndex(triple.get(0)),e=getIndex(triple.get(2));
					Graph.get(s).link.add(e);
					Graph.get(e).link.add(s);
					if(lv==1)
					{
						Graph.get(root).link.add(s);
						Graph.get(s).link.add(root);
					}
					if(lv<N)
					{
						Graph.get(s).level=lv;
						Graph.get(e).level=lv+1;
					}
					if(lv==N-1)Graph.get(e).domain=s;//DOMAIN(S,v)
				}
			}
		}
		br.close();
		


		for(Vertex v : Graph)
		{
			Row row=sheetiv.createRow(sheetiv.getLastRowNum()+1);
			row.createCell(0).setCellValue(v.name);
			row.createCell(1).setCellValue(interestVal(v.index));
			row.createCell(2).setCellValue("here will be inserted normalized values");
			for(int d : v.link)
			{
				if(v.level==N&&Graph.get(d).level==N&&v.domain!=Graph.get(d).domain)
				{
					Vps.add(v.index);
					break;
				}
			}
		}
		
		//create normalized values for interestVal
		for(int i=1; i<sheetiv.getLastRowNum()+1; i++) {
			sheetiv.getRow(i).createCell(2).setCellValue(normalize(i, sheetiv, 1.0, 0.1));
		}
	
		GraphN=Graph.stream().filter(v->v.level==N).collect(Collectors.toList());
		
		
		//S'=(S.V,S.E_n)
		for(Vertex v : Graph)
		{
			Vertex v2=new Vertex(v.index,v.name);
			v2.domain=v.domain;
			v2.level=v.level;
			Graph2.add(v2);
		}
		for(Vertex v : Graph)
		{
			for(int d : v.link)
			{
				if(v.level!=N||Graph.get(d).level!=N)continue;
				Graph2.get(v.index).link.add(d);
			}
		}
		Graph2M=Graph2.stream().collect(Collectors.toMap(Vertex::getIndex,Function.identity()));
		Graph2N=Graph2.stream().filter(v->v.level==N).collect(Collectors.toMap(Vertex::getIndex,Function.identity()));
		//S''=(S.V,S.E-S.E_n), convert S' to undirected graph
		for(Vertex v : Graph)
		{
			Vertex v2=new Vertex(v.index,v.name);
			v2.domain=v.domain;
			v2.level=v.level;
			Graph3.add(v2);
		}
		for(Vertex v : Graph)
		{
			for(int d : v.link)
			{
				if(v.level==N&&Graph.get(d).level==N)continue;
				Graph3.get(v.index).link.add(d);
			}
		}
		
		Graph.get(2);
		
		
		double potentialSeren=0;
		for(int i=0; i<GraphN.size(); i++) {
			//System.out.println(Evaluate(getIndex(GraphN.get(i).name)));
			potentialSeren += Evaluate(getIndex(GraphN.get(i).name));
			System.out.println("Page "+ GraphN.get(i).name + " Evaluation completed");
			//System.out.println("Evaluate(" + GraphN.get(i).name+") = " + Evaluate(getIndex(GraphN.get(i).name)));
		}
		
		//Row row=sheetsc.createRow(sheetsc.getLastRowNum()+1);
		//row.createCell(0).setCellValue("Total");
	    //Cell cell = row.createCell(6);
	    //String sum = "SUM(F1:F"+ Integer.toString(row.getRowNum())+")";
		//cell.setCellFormula(sum);
		System.out.println();
		System.out.println("The number of existing search paths: "+PathNum);
		System.out.println("The number of sidetracked paths that can be derived from the exsiting search paths: "+sidePathCount);		
		System.out.println("The potential serendipity value for the given information space is: "+ potentialSeren);

		
		workbook.write(new FileOutputStream(new File(args[0]).getAbsolutePath().replaceAll(".[^.]*$",".xlsx")));
		workbook.close();
	}

	static void createHeader(Sheet sheet,String... headers)
	{
		Row row=sheet.createRow(0);
		int i=0;
		for(String header : headers)row.createCell(i++).setCellValue(header);
	}

	static int getIndex(String name)
	{
		if(nameToIndex.containsKey(name))return nameToIndex.get(name);
		int index=Graph.size();
		Vertex vertex=new Vertex(index,name);
		Graph.add(vertex);
		nameToIndex.put(name,index);
		return index;
	}

	static void showGraph(List<Vertex> graph)
	{
		for(Vertex v : graph)
		{
			System.out.print("["+v.level+"] "+v.index+"("+v.name+"): ");
			for(int d : v.link)System.out.print(d+"("+graph.get(d).name+") ");
			System.out.println();
		}
		System.out.println("-------------------------");
	}

	static double Evaluate(int v_d) throws NoPathException
	{
		double index=0;
		int numOfPath = 0;
		for(Vertex v : GraphN)
		{
			if(v.index==v_d)continue;
			if(v.domain==Graph.get(v_d).domain)
			{
				for(List<Integer> p : getPaths(Graph2N,v.index,v_d,5,v.domain))
				{
					//Row row=sheetpt.createRow(sheetpt.getLastRowNum()+1);
					//row.createCell(0).setCellValue("p"+row.getRowNum());
					//for(int i : p)row.createCell(row.getLastCellNum()).setCellValue(Graph.get(i).name);
					//pathIdx=0;
					for(int i=0;i<p.size();++i)
						if(v_d != p.get(i))index=index+Serendipity(p,p.get(i), v_d); //remove sidetracked paths starting from target node
					numOfPath++;
					//sheetsc.getRow(sheetsc.getLastRowNum()-pathIdx+1).createCell(6).setCellValue(IntStream.rangeClosed(sheetsc.getLastRowNum()-pathIdx+1,sheetsc.getLastRowNum()).mapToObj(i->String.valueOf(sheetsc.getRow(i).getCell(5).getNumericCellValue())).collect(Collectors.joining("+"))+"="+IntStream.rangeClosed(sheetsc.getLastRowNum()-pathIdx+1,sheetsc.getLastRowNum()).mapToDouble(i->sheetsc.getRow(i).getCell(5).getNumericCellValue()).sum());
						
					//if(pathIdx>1)
					//{
						//sheetpt2.addMergedRegion(new CellRangeAddress(sheetpt2.getLastRowNum()-pathIdx+1,sheetpt2.getLastRowNum(),0,0));
						//sheetsc.addMergedRegion(new CellRangeAddress(sheetsc.getLastRowNum()-pathIdx+1,sheetsc.getLastRowNum(),0,0));
						//sheetsc.addMergedRegion(new CellRangeAddress(sheetsc.getLastRowNum()-pathIdx+1,sheetsc.getLastRowNum(),2,2)); // give separate discovery value to each sidetrakced path
						//sheetsc.addMergedRegion(new CellRangeAddress(sheetsc.getLastRowNum()-pathIdx+1,sheetsc.getLastRowNum(),6,6));
					//}

				}
				
			}
		}
		
		//Row row=sheetsc.createRow(sheetsc.getLastRowNum()+1);
		//System.out.println(numOfPath);
		countPaths(numOfPath);
		//row.createCell(0).setCellValue("Sum");
		//row.createCell(6).setCellValue(index);
		return index;
	}
	
	public static int PathNum = 0;
	public static void countPaths(int x) {
		PathNum += x;
	}

	static List<Integer> getShortestPath(List<Vertex> graph,int s,int e,List<Integer> p) throws NoPathException
	{
		List<Integer> path=new ArrayList<>();
		boolean[] chk=new boolean[graph.size()];
		int[] prv=new int[graph.size()];
		if(p!=null)for(int v : p)chk[v]=true;
		Arrays.fill(prv,-1);
		Queue<Integer> q=new ArrayDeque<>();
		q.add(s);
		chk[s]=true;
		while(!q.isEmpty())
		{
			int now=q.poll();
			if(now==e)break;
			for(int d : graph.get(now).link)
			{
				if(!chk[d])
				{
					q.add(d);
					chk[d]=true;
					prv[d]=now;
				}
			}
		}
		int now=e;
		while(now!=-1)
		{
			path.add(now);
			now=prv[now];
		}
		if(path.get(path.size()-1)!=s)throw new NoPathException("No path between "+s+"("+graph.get(s).name+") <-> "+e+"("+graph.get(e).name+")");
		Collections.reverse(path);
		return path;
	}

	static List<Integer> getShortestPath(List<Vertex> graph,int s,int e) throws NoPathException
	{
		return getShortestPath(graph,s,e,null);
	}

	static List<List<Integer>> getPaths(Map<Integer,Vertex> graph,int s,int e,int mx,int domain,List<Integer> p)
	{
		List<List<Integer>> paths=new ArrayList<>();
		BitSet chk=new BitSet();
		Stack<Context> stack=new Stack<>();
		if(p!=null)for(int v : p)chk.set(v);
		stack.push(new Context(graph,s));
		chk.set(s);
		while(!stack.isEmpty())
		{
			Context ctx=stack.peek();
			if(ctx.v==e)
			{
				paths.add(stack.stream().map(c->c.v).collect(Collectors.toList()));
				chk.clear(ctx.v);
				stack.pop();
				continue;
			}
			if(stack.size()>=mx||!ctx.it.hasNext())
			{
				chk.clear(ctx.v);
				stack.pop();
				continue;
			}
			int d=ctx.it.next();
			if(!chk.get(d)&&(domain==-1||graph.get(d).domain==domain))
			{
				stack.push(new Context(graph,d));
				chk.set(d);
			}
		}
		return paths;
	}

	static List<List<Integer>> getPaths(Map<Integer,Vertex> graph,int s,int e,int mx,int domain)
	{
		return getPaths(graph,s,e,mx,domain,null);
	}

	
	
	static int distance(List<Vertex> graph,int s,int e) throws NoPathException
	{
		return getShortestPath(graph,s,e).size()-1;
	}

	static int distance(List<Vertex> graph,int s,int e,List<Integer> p) throws NoPathException
	{
		return getShortestPath(graph,s,e,p).size()-1;
	}

	static double Serendipity(List<Integer> p,int v_i,int target_index) throws NoPathException
	{
		double score=0;
		for(int v_s : Vps)
		{
			if(Graph.get(v_s).domain!=Graph.get(v_i).domain)continue;
			int dist;
			try
			{
				dist=distance(Graph2,v_i,v_s,p);
			}
			catch(NoPathException e)
			{
				continue;
			}
			int vsIdx=0;

			for(List<Integer> p2 : getPaths(Graph2M,v_i,v_s,dist+1,Graph.get(v_i).domain,p))
			{
				//Row rowpt2=sheetpt2.createRow(sheetpt2.getLastRowNum()+1);
				//Row rowsc=sheetsc.createRow(sheetsc.getLastRowNum()+1);
			
				if(pathIdx==0)
				{
					//rowpt2.createCell(0).setCellValue("p"+sheetpt.getLastRowNum());
					//rowsc.createCell(0).setCellValue("p"+sheetpt.getLastRowNum());
				}
				//if(vsIdx==0)rowpt2.createCell(1).setCellValue(Graph.get(v_s).name);
				
				//rowpt2.createCell(2).setCellValue("p'"+sheetpt.getLastRowNum()+","+(pathIdx+1));
				//rowsc.createCell(1).setCellValue("p'"+sheetpt.getLastRowNum()+","+(pathIdx+1));
			
				//for(int j : p2)rowpt2.createCell(rowpt2.getLastCellNum()).setCellValue(Graph.get(j).name);
				double delta=Discovery(p,p2)*Interest(p2)*NewConnection(v_s);
				score=score+delta;
				//rowsc.createCell(5).setCellValue(delta);
				pathIdx++;
				vsIdx++;
			}
			pathCount(vsIdx);

			//if(vsIdx>1)sheetpt2.addMergedRegion(new CellRangeAddress(sheetpt2.getLastRowNum()-vsIdx+1,sheetpt2.getLastRowNum(),1,1));
		}
		return score;
		
	}
	public static int sidePathCount=0;
	static void pathCount(int x) {
		sidePathCount +=x;
	}
	static double Discovery(List<Integer> p,List<Integer> p2)
	{
		double val = 1.0/(p.indexOf(p2.get(0))+1);
		//sheetsc.getRow(sheetsc.getLastRowNum()).createCell(2).setCellValue(val);
		return val;
	}

	//modified to calculate with normalized values
	static double Interest(List<Integer> p2) throws NoPathException
	{
		double Val=0;
		for(int v_i2 : p2)
		{
			Val = Val+findNormalizedIntForGivenName(sheetiv, v_i2, Graph);
		}
		Val=Val/Double.valueOf(p2.size());
		//sheetsc.getRow(sheetsc.getLastRowNum()).createCell(3).setCellValue("("+IntStream.range(0,p2.size()).mapToObj(i->String.valueOf(findNormalizedIntForGivenName(sheetiv, p2.get(i), Graph))).collect(Collectors.joining("+"))+")/"+p2.size()+"="+Val);
		return Val;
	}

	static double NewConnection(int v_s) throws NoPathException
	{
		double val=0;
		String input = "(";
		for(int v : Graph2N.get(v_s).link) {
			if(Graph3.get(v_s).domain!=Graph3.get(v).domain) {
				double dist = normalizeNC(distance(Graph3,v_s,v),0,N*2,1,0);
				val = val+dist;
				input = input.concat(String.valueOf(dist)+"+");
			}
		}
		input = input.substring(0, input.length()-1);
		input = input.concat(")="+val);
		//sheetsc.getRow(sheetsc.getLastRowNum()).createCell(4).setCellValue(input);
		return val;
	}

	
	static double interestVal(int v_i)
	{
		float interest=0;
		for(int index : Graph.get(v_i).link) {
			interest += (1/(float)(Graph.size()* (float)Graph.get(index).link.size()));
		}
		return interest;
	}

	static double findMaxVal(Sheet sheet) {
		double max=0;
		
		for(int i=1; i<sheet.getLastRowNum()+1; i++) {
			Row row = sheet.getRow(i);
			double v = (double)row.getCell(1).getNumericCellValue();
			if(max<v) max=v;
		}	
		return max;
	}
	
	static double findMinVal(Sheet sheet) {
		double min=(double)sheet.getRow(1).getCell(1).getNumericCellValue();
		for(int i=1; i<sheet.getLastRowNum()+1; i++) {
			Row row = sheet.getRow(i);
			double v = (double)row.getCell(1).getNumericCellValue();
			if(min>v) min=v;
		}	
		return min;
	}
	
	static double normalize(int i, Sheet sheet, double normalizedHigh, double normalizedLow) {
		double x = (double)sheet.getRow(i).getCell(1).getNumericCellValue();
	    return ((x - findMinVal(sheet)) 
	            / (findMaxVal(sheet) - findMinVal(sheet)))
	            * (normalizedHigh - normalizedLow) + normalizedLow;
	}
	
	static double normalizeNC(double x, double min, double max, double normalizedHigh, double normalizedLow) {
	    return ((x - min) 
	            / (max - min))
	            * (normalizedHigh - normalizedLow) + normalizedLow;
	}
	
	static double findNormalizedIntForGivenName(Sheet sheet, int indexInGraph, List<Vertex> graph) {
		double x=0;
			for(int i=1; i<sheet.getLastRowNum()+1; i++) {
				if(sheet.getRow(i).getCell(0).toString() == graph.get(indexInGraph).name) {
					x = sheet.getRow(i).getCell(2).getNumericCellValue();
					
				}
			}
		
		return x;
	}
	
}