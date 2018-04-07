package kr.KENNYSOFT.Serendipity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerendipityValidation
{
	static Set<String> classSet=new HashSet<>();
	static Map<String,Boolean> checkMap=new HashMap<>();
	static Map<String,String> fieldMap=new HashMap<>();
	
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
			if(line.startsWith("@prefix"))continue;
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
					if(lv3Started)
					{
						if(classSet.contains(triple.get(0))&&!classSet.contains(triple.get(2)))checkMap.put(triple.get(2),true);
						if(classSet.contains(triple.get(2))&&!classSet.contains(triple.get(0)))checkMap.put(triple.get(0),true);
						if(!classSet.contains(triple.get(0))&&!checkMap.containsKey(triple.get(0)))
						{
							checkMap.put(triple.get(0),false);
							fieldMap.put(triple.get(0),lv3Field);
						}
						if(!classSet.contains(triple.get(2))&&!checkMap.containsKey(triple.get(2)))
						{
							checkMap.put(triple.get(2),false);
							fieldMap.put(triple.get(2),lv3Field);
						}
					}
					else
					{
						classSet.add(triple.get(0));
						classSet.add(triple.get(2));
					}
				}
			}
		}
		br.close();
		boolean flag=false;
		for(Entry<String,Boolean> entry : checkMap.entrySet())
		{
			if(classSet.contains(entry.getKey()))continue;
			if(!entry.getValue())
			{
				System.out.println(entry.getKey()+" :Related "+fieldMap.get(entry.getKey())+" .");
				flag=true;
			}
		}
		if(!flag)System.out.println("Validation passed");
	}
}