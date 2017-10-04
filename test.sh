#!/usr/bin/env bash

#https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
#https://stackoverflow.com/questions/31879288/overpass-api-way-coordinates

#http://www.overpass-api.de/api/
#http://overpass.osm.rambler.ru/cgi/
#http://api.openstreetmap.fr/api/

#http://www.gdal.org/drv_osm.html

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

cnt=LU

#for usage in "main" "branch" "industrial" "military" "tourism" "test"
#do
#	echo Get data for usage $usage
#	wget -O orm_$usage.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway][usage=$usage](area.a);way[railway][usage=$usage](area.a);relation[railway][usage=$usage](area.a););(._;>;);out;"
#	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_$usage orm_$usage.osm  -overwrite
#	rm orm_$usage.osm
#done

#echo Get data for other usage
#wget -O orm_other.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway][!usage](area.a);way[railway][!usage](area.a);relation[railway][!usage](area.a););(._;>;);out;"
#ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_other orm_other.osm  -overwrite
#rm orm_other.osm

#"LU" "BE" "NL"
for cnt in "LU"
do
	echo ${RED}Get raw ORM data for $cnt${NC}
	wget -O orm_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway](area.a);way[railway](area.a);relation[railway](area.a););(._;>;);out;"

	echo Transform to shapefiles
	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -oo CONFIG_FILE=/home/juju/workspace/OpenCarto/GDALormconf.ini -skipfailures -f "ESRI Shapefile" shp_$cnt orm_$cnt.osm  -overwrite
	rm orm_$cnt.osm

	echo "Rename and drop fields + reproject"
	ogr2ogr -t_srs EPSG:3035 shp_$cnt/points.shp shp_$cnt/lines.shp -sql "SELECT name,descriptio AS desc,railway,gauge,usage,railway_tr AS traf_mode,service,railway__1 AS track_cl,maxspeed,direction,highspeed,historic,bridge,bridge_name AS bridge_na,tunnel,tunnel_nam,electrifie AS elect,elect_if_1 AS elect_rail,voltage,incline,ele,start_date,end_date,operator FROM points"
	ogr2ogr -t_srs EPSG:3035 shp_$cnt/lines.shp shp_$cnt/lines.shp -sql "SELECT name,descriptio AS desc,railway,gauge,usage,railway_tr AS traf_mode,service,railway__1 AS track_cl,maxspeed,direction,highspeed,historic,bridge,bridge_name AS bridge_na,tunnel,tunnel_nam,electrifie AS elect,elect_if_1 AS elect_rail,voltage,incline,ele,start_date,end_date,operator FROM lines"
	ogr2ogr -t_srs EPSG:3035 shp_$cnt/multilines.shp shp_$cnt/lines.shp -sql "SELECT name,descriptio AS desc,railway,gauge,usage,railway_tr AS traf_mode,service,railway__1 AS track_cl,maxspeed,direction,highspeed,historic,bridge,bridge_name AS bridge_na,tunnel,tunnel_name AS tunnel_na,electrifie AS elect,elect_if_1 AS elect_rail,voltage,incline,ele,start_date,end_date,operator FROM multilines"
	ogr2ogr -t_srs EPSG:3035 shp_$cnt/multipolygons.shp shp_$cnt/lines.shp -sql "SELECT name,descriptio AS desc,railway,gauge,usage,railway_tr AS traf_mode,service,railway__1 AS track_cl,maxspeed,direction,highspeed,historic,bridge,bridge_name AS bridge_na,tunnel,tunnel_name AS tunnel_na,electrifie AS elect,elect_if_1 AS elect_rail,voltage,incline,ele,start_date,end_date,operator FROM multipolygons"
done



#'osm_version' to 'osm_versio'
#'osm_timestamp' to 'osm_timest'
#'osm_changeset' to 'osm_change'
#'description' to 'descriptio'
#'railway_traffic_mode' to 'railway_tr'
#'railway_track_class' to 'railway__1'
#'bridge_name' to 'bridge_nam'
#'tunnel_name' to 'tunnel_nam'
#'electrified' to 'electrifie'
#'electrified_rail' to 'elect_if_1'
