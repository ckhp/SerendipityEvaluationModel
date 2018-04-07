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
	static Map<Integer,Vertex> graph2M;
	static Map<Integer,Vertex> graph2N;
	static List<Vertex> graph3=new ArrayList<>();
	static List<Vertex> graph3N;
	static List<Integer> Vps=new ArrayList<>();
	static Map<String,Integer> nameToIndex=new HashMap<>();
	
	static String[] aVps={"dbr:Heredity"};
	
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
		for(Vertex v : graph)
		{
			for(int d : v.link)
			{
				if(v.level==3&&graph.get(d).level==3&&v.domain!=graph.get(d).domain)
				{
					Vps.add(v.index);
					break;
				}
			}
		}
		System.out.println(Vps.toString());
		graphN=graph.stream().filter(v->v.level==3).collect(Collectors.toList());
		//S'=(S.V,S.E_n)
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
				if(v.level!=3||graph.get(d).level!=3)continue;
				graph2.get(v.index).link.add(d);
			}
		}
		graph2M=graph2.stream().collect(Collectors.toMap(Vertex::getIndex,Function.identity()));
		graph2N=graph2.stream().filter(v->v.level==3).collect(Collectors.toMap(Vertex::getIndex,Function.identity()));
		//S''=(S.V,S.E-S.E_n), convert S' to undirected graph
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
				if(v.level==3&&graph.get(d).level==3)continue;
				graph3.get(v.index).link.add(d);
			}
		}
		graph3N=graph3.stream().filter(v->v.level==3).collect(Collectors.toList());
		showGraph(graph);
		showGraph(graph2);
		showGraph(graph3);
		System.out.println(Evaluate(getIndex(":NodeA4")));
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
			System.out.println(v.index);
			if(v.domain==graph.get(v_d).domain)
			{
				for(List<Integer> p : getPaths(graph2N,v.index,v_d,5))
				{
					System.out.println("hi"+p);
					n++;
					for(int i=0;i<p.size();++i)
					{
						index=index+Serendipity(p,p.get(i),i);
					}
					System.out.println("!"+index);
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
		if(path.get(path.size()-1)!=s)throw new NoPathException("No path between "+s+"("+graph.get(s).name+") <-> "+e+"("+graph.get(e).name+")");
		Collections.reverse(path);
		return path;
	}
	
	static List<Integer> getShortestPath(List<Vertex> graph,int s,int e) throws NoPathException
	{
		return getShortestPath(graph,s,e,null);
	}
	
	static List<List<Integer>> getPaths(Map<Integer,Vertex> graph,int s,int e,int mx,List<Integer> p,int domain)
	{
		List<List<Integer>> paths=new ArrayList<>();
		Map<Integer,Boolean> chk=new HashMap<>();
		Stack<Context> stack=new Stack<>();
		if(p!=null)for(int v : p)chk.put(v,true);
		stack.push(new Context(graph,s));
		chk.put(s,true);
		while(!stack.isEmpty())
		{
			if(stack.peek().v==e)
			{
				paths.add(stack.stream().map(c->c.v).collect(Collectors.toList()));
				chk.put(stack.peek().v,false);
				stack.pop();
				continue;
			}
			if(stack.size()>=mx||!stack.peek().it.hasNext())
			{
				chk.put(stack.peek().v,false);
				stack.pop();
				continue;
			}
			int d=stack.peek().it.next();
			if((!chk.containsKey(d)||!chk.get(d))&&(domain==-1||graph.get(d).domain==domain))
			{
				stack.push(new Context(graph,d));
				chk.put(d,true);
			}
		}
		return paths;
	}
	
	static List<List<Integer>> getPaths(Map<Integer,Vertex> graph,int s,int e,int mx)
	{
		return getPaths(graph,s,e,mx,null,-1);
	}
	
	static int distance(List<Vertex> graph,int s,int e) throws NoPathException
	{
		return getShortestPath(graph,s,e).size()-1;
	}
	
	static int distance(List<Vertex> graph,int s,int e,List<Integer> p) throws NoPathException
	{
		return getShortestPath(graph,s,e,p).size()-1;
	}
	
	static double Serendipity(List<Integer> p,int v_i,int i) throws NoPathException
	{
		double score=0;
		for(int v_s : Vps)
		{
			if(graph.get(v_s).domain!=graph.get(v_i).domain)continue;
			int dist;
			if(v_i==v_s)System.out.println("asdfasdasdfasdf");
			try
			{
				dist=distance(graph2,v_i,v_s,p);
			}
			catch(NoPathException e)
			{
				continue;
			}
			for(List<Integer> p2 : getPaths(graph2M,v_i,v_s,dist+1,p,graph.get(v_i).domain))
			{
				System.out.println("hello"+p2+v_s);
				System.out.println("ser"+Discovery(p,i)*Interest(p2)*NewConnection(v_s)+"/"+Discovery(p,i)+"?"+Interest(p2)+"?"+NewConnection(v_s));
				score=score+Discovery(p,i)*Interest(p2)*NewConnection(v_s);
			}
		}
		return score;
	}
	
	static double Discovery(List<Integer> p,int i)
	{
		double val;
		System.out.println("dsc"+graph.get(p.get(0)).domain+"/////////"+i);
		val=1.0/graph.get(graph.get(p.get(0)).domain).link.stream().filter(v->graph.get(v).level==3).count()/(i+1);
		return val;
	}
	
	static double Interest(List<Integer> p2) throws NoPathException
	{
		double Val=0;
		for(int v_i2 : p2)Val=Val+InterestVal(v_i2);
		return Val/p2.size();
	}
	
	static double NewConnection(int v_s) throws NoPathException
	{
		double val=0;
		for(Vertex v : graph3N)
		{
			if(v.index==v_s)continue;
			if(graph3.get(v_s).domain!=v.domain)val=val+distance(graph3,v_s,v.index);
		}
		return val;
	}
	
	static double InterestVal(int v_i)
	{
		switch(graph.get(v_i).name)
		{
		case ":NodeA1":
			return 10;
		case ":NodeA2":
			return 20;
		case ":NodeA2_1":
			return 10;
		case ":NodeA2_2":
			return 40;
		case ":NodeA3":
			return 50;
		case ":NodeA4":
			return 20;
		case ":NodeB1":
			return 30;
		default:
			throw new RuntimeException("Not Hardcoded");
		}
	}
}