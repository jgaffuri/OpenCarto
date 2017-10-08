#!/usr/bin/env bash

#https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
#https://stackoverflow.com/questions/31879288/overpass-api-way-coordinates

#http://www.overpass-api.de/api/
#http://overpass.osm.rambler.ru/cgi/
#http://api.openstreetmap.fr/api/

#http://www.overpass-api.de/api/status
#[timeout:25];


#  ["key"]            /* filter objects tagged with this key and any value */
#  [!"key"]           /* filter objects not tagged with this key and any value */
#  ["key"="value"]    /* filter objects tagged with this key and this value */
#  ["key"!="value"]   /* filter objects tagged with this key but not this value */
#  ["key"~"value"]    /* filter objects tagged with this key and a value matching a regular expression */
#  ["key"!~"value"    /* filter objects tagged with this key but a value not matching a regular expression */
#  [~"key"~"value"]   /* filter objects tagged with a key and a value matching regular expressions */
#  [~"key"~"value",i] /* filter objects tagged with a key and a case-insensitive value matching regular expressions */

#http://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging
#[railway!=razed][railway!=light_rail][railway!=subway][railway!=tram][railway!=miniature][railway!=switch][railway!=railway_crossing][railway!=derail][railway!=buffer_stop][!subway]]

#vacancy_detection  level_crossing  crossing  isolated_track_section  owner_change   milestone   signal_box  interlocking 	crossing_box blockpost  tram_stop service_station stop


cd ~/Bureau/gisco_rail/orm


echo "Load data from with overpass API"
mkdir -p osmxml
for cnt in "FR"
do
	echo "****** $cnt ******"
	echo Get raw ORM data for $cnt
	#wget -O osmxml/orm_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway](area.a);way[railway](area.a);relation[railway](area.a););(._;>;);out;"
	wget -O osmxml/orm_node_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway](area.a););(._;>;);out;"
	wget -O osmxml/orm_way_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(way[railway](area.a););(._;>;);out;"
	wget -O osmxml/orm_relation_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(relation[railway](area.a););(._;>;);out;"
done
