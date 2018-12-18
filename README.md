# SerendipityEvaluationModel

Changu Kang, Hyeonmin Park, Seongbin Park (2018), Serendipity Evaluation Model

## Execution

The main procedure is in `SerendipityEvaluation.java`. The argument should fit to the following form:

* args[0]: The name of ttl file (ex. `HistoricalData.ttl`)
* args[1]: _N_ parameter of input; the maximum level of hierarchy structure (ex. `3` for following example data)
* args[2]: The name of target node (ex. `:Kyujanggak`)

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
