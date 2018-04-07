package kr.KENNYSOFT.SerendipityEvaluationModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerendipityGraphviz
{
	final static String PREFIX_SPLITTER="/////*****PREFIX_SPLITTER*****/////";
	final static String PREFIX_SPLITTER_PATTERN=Pattern.quote(PREFIX_SPLITTER);
	static Map<String,String> prefixMap=new HashMap<>();
	static Map<String,Set<String>> resourceMap=new HashMap<>();
	static List<String[]> linkList=new ArrayList<>();

	public static void main(String[] args) throws Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String line=null;
		boolean lv3Started=false;
		String lv3Field=null;
		List<String> triple=new ArrayList<>();
		while((line=br.readLine())!=null)
		{
			line=line.trim();
			if(line.startsWith("@prefix"))
			{
				Matcher match=Pattern.compile("@prefix ([a-zA-Z]*:) <([^>]*)>").matcher(line);
				if(match.find())prefixMap.put(match.group(1),match.group(2));
				continue;
			}
			if(line.startsWith("########3-Level#########"))
			{
				lv3Started=true;
				continue;
			}
			if(lv3Started)
			{
				Matcher match=Pattern.compile("#+ *([ a-zA-Z:_]*) *#+").matcher(line);
				if(match.find())
				{
					lv3Field=match.group(1);
					continue;
				}
			}
			if(line.startsWith("#")||line.length()==0)continue;
			String[] tokens=line.split("( |\t)+");
			//System.err.println(Arrays.toString(tokens));
			for(String token : tokens)
			{
				token=token.replace("\\'","\'");
				for(Entry<String,String> prefix : prefixMap.entrySet())if(token.startsWith(prefix.getKey()))token=prefix.getValue()+PREFIX_SPLITTER+token.substring(prefix.getKey().length());
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
					//System.err.println("["+lv3Field+"] "+triple.get(0)+" --"+triple.get(1)+"--> "+triple.get(2));
					getResourceSet(null).add(triple.get(0));
					if(triple.get(2).length()==0)System.err.println(Arrays.toString(triple.toArray()));
					getResourceSet(lv3Field).add(triple.get(2));
					linkList.add(triple.toArray(new String[3]));
				}
			}
		}
		br.close();
		int cnt=0;
		System.out.println("digraph {");
		System.out.println("\trankdir = TB;");
		System.out.println("\tcharset = \"utf-8\";");
		for(String[] link : linkList)
		{
			String[] prefixs=Arrays.stream(link).map(s->s.split(PREFIX_SPLITTER_PATTERN)[0]).toArray(String[]::new);
			String[] ress=Arrays.stream(link).map(s->s.split(PREFIX_SPLITTER_PATTERN)[1]).toArray(String[]::new);
			System.out.printf("\t\"R%s\" -> \"R%s\" [ label=\"%s\" ];\n",prefixs[0]+ress[0],prefixs[2]+ress[2],ress[1]);
		}
		for(Entry<String,Set<String>> resources : resourceMap.entrySet())
		{
			if(resources.getKey()!=null)
			{
				System.out.println("\tsubgraph cluster"+(cnt++)+" {");
				System.out.println("\t\tlabel = \""+resources.getKey()+"\";");
				System.out.println("\t\tcolor = red;");
				System.out.println("\t\tfontcolor = red;");
				System.out.println("\t\trank = same;");
			}
			for(String resource : resources.getValue())
			{
				String prefix=resource.split(PREFIX_SPLITTER_PATTERN)[0];
				String res=resource.split(PREFIX_SPLITTER_PATTERN)[1];
				if(resources.getKey()!=null)System.out.print("\t");
				System.out.printf("\t\"R%s\" [ label=\"%s\", shape = ellipse, color = blue ];\n",prefix+res,res);
			}
			if(resources.getKey()!=null)System.out.println("\t};");
		}
		System.out.println("}");
	}

	static Set<String> getResourceSet(String field)
	{
		if(resourceMap.containsKey(field))return resourceMap.get(field);
		Set<String> set=new HashSet<>();
		resourceMap.put(field,set);
		return set;
	}
}