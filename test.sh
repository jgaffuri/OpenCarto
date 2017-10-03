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


cd ~/Bureau/gisco_rail/orm

for usage in "main" "branch" "industrial" "military" "tourism" "test"
do
	echo Get data for usage $usage
	wget -O orm_$usage.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'='FR'][admin_level=2];)->.a;(node[railway][usage=$usage](area.a);way[railway][usage=$usage](area.a);relation[railway][usage=$usage](area.a););(._;>;);out;"
	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_$usage orm_$usage.osm  -overwrite
	rm orm_$usage.osm
done

echo Get data for other usage
wget -O orm_other.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'='FR'][admin_level=2];)->.a;(node[railway][!usage](area.a);way[railway][!usage](area.a);relation[railway][!usage](area.a););(._;>;);out;"
ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_other orm_other.osm  -overwrite
rm orm_other.osm

echo Get attribute data
wget -O orm.csv "http://overpass-api.de/api/map?data=[out:csv(::id,name,description,railway,gauge,usage,'railway:traffic_mode',service,'railway:track_class',maxspeed,direction,highspeed,historic,bridge,'bridge:name',tunnel,'tunnel:name',electrified,'electrified:rail',voltage,incline,ele,start_date,end_date,operator,::timestamp,::version,::user,::user,::uid)];(area['ISO3166-1:alpha2'='FR'][admin_level=2];)->.a;(node[railway][usage](area.a);way[railway][usage](area.a);relation[railway][usage](area.a););(._;>;);out;"




#wget -O orm.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'='FR'][admin_level=2];)->.a;(node[railway][usage](area.a);way[railway][usage](area.a);relation[railway][usage](area.a););(._;>;);out;"
#ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp orm.osm  -overwrite

#area['ISO3166-1:alpha2'='FR'][admin_level=2];
#area['ISO3166-1:alpha2'='FR'][admin_level=2];
#wget -O orm.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'='FR'][admin_level=2];)->.a;(node[railway][usage=main](area.a);way[railway][usage=main](area.a);relation[railway][usage=main](area.a););(._;>;);out;"

