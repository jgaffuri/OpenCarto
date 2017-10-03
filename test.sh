#!/usr/bin/env bash

#https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
#https://stackoverflow.com/questions/31879288/overpass-api-way-coordinates

#http://www.overpass-api.de/api/
#http://overpass.osm.rambler.ru/cgi/
#http://api.openstreetmap.fr/api/

#http://www.overpass-api.de/api/status


#test csv output
#[out:csv("name";false)];
#area[name="Troisdorf"];
#way(area)[highway][name];
#[timeout:25];


cd ~/Bureau/gisco_rail/orm

for usage in "main" "branch" "industrial" "military" "tourism" "test"
do
	echo $usage
	wget -O orm_$usage.osm "http://overpass-api.de/api/map?data=[out:xml];(node[railway][usage=$usage](42,2,47,9);way[railway][usage=$usage](42,2,47,9);relation[railway][usage=$usage](42,2,47,9););(._;>;);out;"
	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_$usage orm_$usage.osm  -overwrite
	rm orm_$usage.osm
done
wget -O orm_other.osm "http://overpass-api.de/api/map?data=[out:xml];(node[railway][!usage](42,2,47,9);way[railway][!usage](42,2,47,9);relation[railway][!usage](42,2,47,9););(._;>;);out;"
ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_other orm_other.osm  -overwrite
rm orm_other.osm


wget -O orm.csv "http://overpass-api.de/api/map?data=[out:csv(::id,name,description,railway,::gauge,usage,"railway:traffic_mode",service,"railway:track_class",maxspeed,direction,highspeed,historic,bridge,"bridge:name",tunnel,"tunnel:name",electrified,electrified:rail,::voltage,incline,start_date,end_date)];(node[railway][usage=main](46,8,47,9);way[railway][usage=main](46,8,47,9);relation[railway][usage=main](46,8,47,9););(._;>;);out;"
#[out:csv(::id, ::lat, ::lon, name)];
#[out:csv(id,railway,usage,service,maxspeed,direction)]
#[!"subway"]
