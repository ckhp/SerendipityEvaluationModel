# SerendipityEvaluationModel

Changu Kang, Hyeonmin Park, Seongbin Park (2018), Serendipity Evaluation Model

## Execution

### Structure

The main procedure is in `SerendipityEvaluation.java`. The argument should fit to the following form:

* args[0]: The name of ttl file (ex. `HistoricalData.ttl`)
* args[1]: _N_ parameter of input; the maximum level of hierarchy structure (ex. `3` for following example data)
* args[2]: The name of target node (ex. `:Kyujanggak`)

### Environment

Prerequsite: Java 8+ is needed.

### Run in Eclipse

This is an Eclipse project, so you can import the whole project and run `SerendipityEvaluation` with described arguments. Arguments can be set in 'Run Configuration' menu, into 'Program arguments'. (_NOT_ 'VM arguments')

Since this project is set to Java-11, if you want to use other version then you should change the JDK Compliance version. This can be configured in 'Project Properties' > 'Java Compiler'.

### Run in Shell

If you are not familiar with Eclipse or just want to run without any IDE, you can run in the shell. We assume that you have Java and can use `java` command without any problem in the shell. That is, if you are using Windows, you should configure environment variables like 'PATH'.

Following commands are for evaluating all example data:

```shell
javac -classpath "libs/*" -sourcepath "src" src/kr/KENNYSOFT/SerendipityEvaluationModel/SerendipityEvaluation.java
java -classpath "src;libs/*" kr.KENNYSOFT.SerendipityEvaluationModel.SerendipityEvaluation "ExampleData/Data1.ttl" 3 "dbr:Description_Logic"
java -classpath "src;libs/*" kr.KENNYSOFT.SerendipityEvaluationModel.SerendipityEvaluation "ExampleData/Data2.ttl" 3 "dbr:Description_Logic"
java -classpath "src;libs/*" kr.KENNYSOFT.SerendipityEvaluationModel.SerendipityEvaluation "ExampleData/HistoricalHeritage.ttl" 4 ":Kyujanggak"
mv "ExampleData/HistoricalHeritage.xlsx" "ExampleData/HistoricalHeritage-Kyujanggak.xlsx"
java -classpath "src;libs/*" kr.KENNYSOFT.SerendipityEvaluationModel.SerendipityEvaluation "ExampleData/HistoricalHeritage.ttl" 4 ":Gyeongbokgung"
mv "ExampleData/HistoricalHeritage.xlsx" "ExampleData/HistoricalHeritage-Gyeongbokgung.xlsx"
java -classpath "src;libs/*" kr.KENNYSOFT.SerendipityEvaluationModel.SerendipityEvaluation "ExampleData/IllustrativeExample.ttl" 3 ":NodeA4"
```

## Input: Turtle-Syntax Linked Data File Format

One should expliecitly denote the level of each node.

Given format for this is as follows:

```turtle
@prefix : <http://example.com/> .

########################
########1-Level#########
########################

:Eastern_world
   :SubField :Eastern_Asia ;
   :SubField :Western_Asia .

:Western_world
   :SubField :Southern_Europe ;
   :SubField :Western_Europe .

########################
########2-Level#########
########################

:Eastern_Asia :SubTopic :Korea ;
   :SubTopic :Japan .

:Western_Asia :SubTopic :Israel .

:Southern_Europe :SubTopic :Italy .

:Western_Europe :SubTopic :United_Kingdom ;
   :SubTopic :France .

########################
########3-Level#########
########################

:Korea :SubTopic :Gyeongbokgung ; #palalce
   :SubTopic :Kyujanggak ;
   :SubTopic :Tripitaka_Koreana ;
   :SubTopic :Cheomseongdae ;
   :SubTopic :Gyeonghoeru ; #palalce
   :SubTopic :Kyujanggak ;
   :SubTopic :Changdeokgung ;
   :SubTopic :Joseon ;
   :SubTopic :Taejong ;
   :SubTopic :Sejong ;
   :SubTopic :Seoul;
   :SubTopic :Hanyang .

:Japan :SubTopic :Kyoto_Imperial_Palace ; #palalce
   :SubTopic :Itsukushima_Shrine ;
   :SubTopic :Sensō-ji ;
   :SubTopic :Kenreimon .

:Israel :SubTopic :Old_City ;
   :SubTopic :Western_Wall ;

...(skip)...
```
