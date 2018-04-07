package kr.KENNYSOFT.Serendipity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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

public class SerendipityEvaluation
{
	final static double ALPHA=0.7;
	static Set<Integer> classSet=new HashSet<>();
	static List<Vertex> graph=new ArrayList<>();
	static List<Vertex> graphN;
	static List<Vertex> graph2=new ArrayList<>();
	static List<Vertex> graph2N;
	static List<Vertex> graph3=new ArrayList<>();
	static Map<Integer,Vertex> graph3N;
	static List<Integer> Vps;
	static List<Integer> Vpc;
	static List<Integer> Vkd;
	static Map<String,Integer> nameToIndex=new HashMap<>();
	
	static String[] aVps={"dbr:Heredity"};
	static String[] aVpc={"dbr:Programming_language"};
	static String[] aVkd={"dbr:Computer_science"};
	
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
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String line=null;
		List<String> triple=new ArrayList<>();
		int lv=0,root=getIndex("kenny:ROOT");
		graph.get(root).level=0;
		while((line=br.readLine())!=null)
		{
			line=line.trim();
			if(line.startsWith("@prefix"))continue;
			Matcher match=Pattern.compile("#+(\\d)-Level#+").matcher(line);
			if(match.find())
			{
				lv=Integer.parseInt(match.group(1));
				continue;
			}
			if(line.startsWith("#")||line.length()==0)continue;
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
					graph.get(s).link.add(e);
					graph.get(e).link.add(s);
					switch(lv)
					{
					case 1:
						graph.get(root).link.add(s);
						graph.get(s).link.add(root);
						classSet.add(s);
						graph.get(s).level=1;
						classSet.add(e);
						graph.get(e).level=2;
						break;
					case 2:
						classSet.add(s);
						graph.get(s).level=2;
						//DOMAIN(S,v)
						graph.get(e).domain=s;
						graph.get(e).level=3;
						break;
					}
				}
			}
		}
		br.close();
		Vps=Arrays.stream(aVps).map(v->getIndex(v)).collect(Collectors.toList());
		Vpc=Arrays.stream(aVpc).map(v->getIndex(v)).collect(Collectors.toList());
		Vkd=Arrays.stream(aVkd).map(v->getIndex(v)).collect(Collectors.toList());
		graphN=graph.stream().filter(v->v.level==3).collect(Collectors.toList());
		//S'=(S.V,S.E-S.E_n), convert S' to undirected graph
		for(Vertex v : graph)
		{
			Vertex v2=new Vertex(v.index,v.name);
			v2.domain=v.domain;
			v2.level=v.level;
			graph2.add(v2);
		}
		for(Vertex v : graph)
		{
			for(int d : v.link)
			{
				if(v.level==3&&graph.get(d).level==3)continue;
				graph2.get(v.index).link.add(d);
				//graph2.get(d).link.add(v.index);
			}
		}
		graph2N=graph2.stream().filter(v->v.level==3).collect(Collectors.toList());
		//S''=(S.V,S.E_n)
		for(Vertex v : graph)
		{
			Vertex v2=new Vertex(v.index,v.name);
			v2.domain=v.domain;
			v2.level=v.level;
			graph3.add(v2);
		}
		for(Vertex v : graph)
		{
			for(int d : v.link)
			{
				if(v.level!=3||graph.get(d).level!=3)continue;
				graph3.get(v.index).link.add(d);
				//graph2.get(d).link.add(v.index);
			}
		}
		graph3N=graph3.stream().filter(v->v.level==3).collect(Collectors.toMap(Vertex::getIndex,Function.identity()));
		showGraph(graph);
		showGraph(graph2);
		showGraph(graph3);
		System.out.println(Evaluate(getIndex("dbr:Ponzo_illusion")));
	}
	
	static int getIndex(String name)
	{
		if(nameToIndex.containsKey(name))return nameToIndex.get(name);
		int index=graph.size();
		Vertex vertex=new Vertex(index,name);
		graph.add(vertex);
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
		int n=0;
		double index=0;
		for(Vertex v : graphN)
		{
			if(v.index==v_d)continue;
			if(v.domain==graph.get(v_d).domain)
			{
				for(List<Integer> p : getPaths(graph3N,v.index,v_d,5))
				{
					for(int v_i : p)
					{
						n++;
						index=index+Serendipity(p,v_i);
					}
				}
			}
		}
		return index/n;
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
		if(path.get(path.size()-1)!=s||path.size()==1)throw new NoPathException("No path between "+s+"("+graph.get(s).name+") <-> "+e+"("+graph.get(e).name+")");
		path.remove(0);
		Collections.reverse(path);
		return path;
	}
	
	static List<Integer> getShortestPath(List<Vertex> graph,int s,int e) throws NoPathException
	{
		return getShortestPath(graph,s,e,null);
	}
	
	static List<List<Integer>> getPaths(Map<Integer,Vertex> graph,int s,int e,int mx)
	{
		List<List<Integer>> paths=new ArrayList<>();
		Map<Integer,Boolean> chk=new HashMap<>();
		Stack<Context> stack=new Stack<>();
		stack.push(new Context(graph,s));
		chk.put(s,true);
		while(!stack.isEmpty())
		{
			if(stack.peek().v==e)
			{
				paths.add(stack.stream().map(c->c.v).collect(Collectors.toList()));
				chk.put(stack.peek().v,false);
				stack.pop();
			}
			if(stack.size()>mx||!stack.peek().it.hasNext())
			{
				chk.put(stack.peek().v,false);
				stack.pop();
				continue;
			}
			int d=stack.peek().it.next();
			if(!chk.containsKey(d)||!chk.get(d))
			{
				stack.push(new Context(graph,d));
				chk.put(d,true);
			}
		}
		return paths;
	}
	
	static int distance(List<Vertex> graph,int s,int e) throws NoPathException
	{
		return getShortestPath(graph,s,e).size();
	}
	
	static double Serendipity(List<Integer> p,int v_i) throws NoPathException
	{
		double score=0;
		for(int v_s : Vps)
		{
			List<Integer> p2;
			try
			{
				p2=getShortestPath(graph3,v_i,v_s,p);
			}
			catch(NoPathException e)
			{
				continue;
			}
			score=score+Interest(p2)*NewConnection(v_s)*Discovery(p);
		}
		return score;
	}
	
	static double Discovery(List<Integer> p)
	{
		double val;
		val=1/graph.get(p.get(0)).link.size();
		return val;
	}
	
	static double Interest(List<Integer> p2) throws NoPathException
	{
		double Val=0;
		for(int v_i2 : p2)Val=Val+InterestVal(v_i2);
		return Val/(p2.size()-1);
	}
	
	static double NewConnection(int v_s) throws NoPathException
	{
		double val=0;
		for(Vertex v : graph2N)
		{
			if(v.index==v_s)continue;
			if(graph2.get(v_s).domain!=v.domain)val=val+distance(graph2,v_s,v.index);
		}
		return val;
	}
	
	static double InterestVal(int v_i) throws NoPathException
	{
		double Comprehensibility=0;
		double Novelty=0;
		for(int v : Vpc)Novelty=Novelty+distance(graph2,v_i,v);
		for(int v : Vkd)Comprehensibility=Comprehensibility+1/distance(graph2,v_i,v);
		return ALPHA*Comprehensibility*Novelty;
	}
}