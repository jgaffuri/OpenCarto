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

#cnt=LU

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

#"LU" "BE" "NL" "PL" "CZ" "SK" "DK" "DE" "CH" "AT" "HU" "IT"
for cnt in "CH" "PL" "SK" "DK" "LU" "BE" "NL" "DE"
do
	echo ${RED}Get raw ORM data for $cnt${NC}
	wget -O orm_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway](area.a);way[railway](area.a);relation[railway](area.a););(._;>;);out;"

	echo Transform to shapefiles
	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -oo CONFIG_FILE=/home/juju/workspace/OpenCarto/GDALormconf.ini -skipfailures -f "ESRI Shapefile" shp_$cnt orm_$cnt.osm  -overwrite
	#rm orm_$cnt.osm

	echo "Rename and drop fields + reproject"
	ogr2ogr -t_srs EPSG:3035 -s_srs EPSG:4326 shp_$cnt/points_$cnt.shp shp_$cnt/points.shp -sql "SELECT osm_id, osm_versio AS version, osm_timest AS timestamp, osm_uid, osm_user, osm_change, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, ele AS elevat, start_date, end_date, operator FROM points"
	ogr2ogr -t_srs EPSG:3035 -s_srs EPSG:4326 shp_$cnt/lines_$cnt.shp shp_$cnt/lines.shp -sql "SELECT osm_id, osm_versio AS version, osm_timest AS timestamp, osm_uid, osm_user, osm_change, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, ele AS elevat, start_date, end_date, operator FROM lines"
	ogr2ogr -t_srs EPSG:3035 -s_srs EPSG:4326 shp_$cnt/multilinestrings_$cnt.shp shp_$cnt/multilinestrings.shp -sql "SELECT osm_id, osm_versio AS version, osm_timest AS timestamp, osm_uid, osm_user, osm_change, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, ele AS elevat, start_date, end_date, operator FROM multilinestrings"
	ogr2ogr -t_srs EPSG:3035 -s_srs EPSG:4326 shp_$cnt/multipolygons_$cnt.shp shp_$cnt/multipolygons.shp -sql "SELECT osm_id, osm_versio AS version, osm_timest AS timestamp, osm_uid, osm_user, osm_change, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, ele AS elevat, start_date, end_date, operator FROM multipolygons"
done

#TODO: remove attributes which are not necessary
