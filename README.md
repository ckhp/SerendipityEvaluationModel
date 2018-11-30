# SerendipityEvaluationModel
Changu Kang, Hyeonmin Park, Seongbin Park (2018), Serendipity Evaluation Model

# Execution

main procedure is in SerendipityEvaluation.java.
It takes name of ttl fil (ex. HistoricalData.ttl) args[0] and the targent node (ex.Kyujanggak) as args[1]

# Input Turtle-Syntax Linekd Data File Format

one should expliecitly denote the level of each node.
Given format for this is as follows

```
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
   
```
