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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class SerendipityEvaluation
{
	final static double ALPHA=0.7;
	static Set<Integer> classSet=new HashSet<>();
	static List<Vertex> graph=new ArrayList<>();
	static List<Vertex> graphN;
	static List<Vertex> graph2=new ArrayList<>();
	static List<Vertex> graph2N;
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
		List<Integer> link;
		
		public Vertex(int index,String name)
		{
			this.index=index;
			this.name=name;
			this.link=new ArrayList<>();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String line=null;
		boolean lv3Started=false;
		List<String> triple=new ArrayList<>();
		while((line=br.readLine())!=null)
		{
			line=line.trim();
			if(line.startsWith("@prefix"))continue;
			if(line.startsWith("########3-Level#########"))
			{
				lv3Started=true;
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
					if(lv3Started)
					{
						//DOMAIN(S,v)
						if(!classSet.contains(s)&&classSet.contains(e))
						{
							graph.get(s).domain=e;
							graph.get(s).level=3;
						}
						else if(!classSet.contains(e)&&classSet.contains(s))
						{
							graph.get(e).domain=s;
							graph.get(e).level=3;
						}
						else
						{
							graph.get(s).level=3;
							graph.get(e).level=3;
						}
					}
					else
					{
						classSet.add(s);
						classSet.add(e);
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
			v.domain=v.domain;
			v.level=v.level;
			graph2.add(v2);
		}
		for(Vertex v : graph)
		{
			for(int d : v.link)
			{
				if(v.level==3&&graph.get(d).level==3)continue;
				graph2.get(v.index).link.add(d);
				graph2.get(d).link.add(v.index);
			}
		}
		graph2N=graph2.stream().filter(v->v.level==3).collect(Collectors.toList());
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
	
	static double Evaluate(int v_d)
	{
		double index=0;
		for(Vertex v : graphN)
		{
			if(v.index==v_d)continue;
			List<Integer> p=getShortestPath(graph,v.index,v_d);
			for(int v_i : p)index=index+Serendipity(p,v_i);
		}
		return index;
	}
	
	static List<Integer> getShortestPath(List<Vertex> graph,int s,int e)
	{
		List<Integer> path=new ArrayList<>();
		boolean[] chk=new boolean[graph.size()];
		int[] prv=new int[graph.size()];
		Queue<Integer> q=new ArrayDeque<>();
		q.add(s);
		chk[s]=true;
		prv[s]=-1;
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
		path.remove(0);
		Collections.reverse(path);
		return path;
	}
	
	static int distance(List<Vertex> graph,int s,int e)
	{
		return getShortestPath(graph,s,e).size();
	}
	
	static double Serendipity(List<Integer> p,int v_i)
	{
		double score=0;
		for(int v_s : Vps)score=score+Interest(v_i,v_s)*NewConnection(v_s)*Discovery(p);
		return score;
	}
	
	static double Discovery(List<Integer> p)
	{
		double val;
		val=1/graph.get(p.get(0)).link.size();
		return val;
	}
	
	static double Interest(int v_i,int v_s)
	{
		double Val=0;
		List<Integer> p2=getShortestPath(graph,v_i,v_s);
		for(int v_i2 : p2)Val=Val+InterestVal(v_i2);
		return Val/(p2.size()-1);
	}
	
	static double NewConnection(int v_s)
	{
		double val=0;
		for(Vertex v : graph2N)
		{
			if(v.index==v_s)continue;
			if(graph2.get(v_s).domain!=v.domain)val=val+distance(graph2,v_s,v.index);
		}
		return val;
	}
	
	static double InterestVal(int v_i)
	{
		double Comprehensibility=0;
		double Novelty=0;
		for(int v : Vpc)Novelty=Novelty+distance(graph2,v_i,v);
		for(int v : Vkd)Comprehensibility=Comprehensibility+1/distance(graph2,v_i,v);
		return ALPHA*Comprehensibility*Novelty;
	}
}