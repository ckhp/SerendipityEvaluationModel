package SerendipityEvaluationModel;

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
	static Map<String,String> domainMap=new HashMap<>();

	public static void main(String[] args) throws Exception
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		String line=null;
		int lv=0;
		String lv3Domain=null;
		List<String> triple=new ArrayList<>();
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
			if(lv==3)
			{
				match=Pattern.compile("#+ *([ a-zA-Z:_]*) *#+").matcher(line);
				if(match.find())
				{
					lv3Domain=match.group(1);
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
					switch(lv)
					{
					case 1:
						classSet.add(triple.get(0));
						classSet.add(triple.get(2));
						break;
					case 2:
						classSet.add(triple.get(0));
						checkMap.put(triple.get(2),true);
						break;
					case 3:
						if(!checkMap.containsKey(triple.get(0)))
						{
							checkMap.put(triple.get(0),false);
							domainMap.put(triple.get(0),lv3Domain);
						}
						if(!checkMap.containsKey(triple.get(2)))
						{
							checkMap.put(triple.get(2),false);
							domainMap.put(triple.get(2),lv3Domain);
						}
						break;
					}
				}
			}
		}
		//System.err.println(classSet.toString());
		br.close();
		boolean flag=false;
		for(Entry<String,Boolean> entry : checkMap.entrySet())
		{
			if(classSet.contains(entry.getKey()))continue;
			if(!entry.getValue())
			{
				System.out.println(domainMap.get(entry.getKey())+" :SubTopic "+entry.getKey()+" .");
				flag=true;
			}
		}
		if(!flag)System.out.println("Validation passed");
	}
}