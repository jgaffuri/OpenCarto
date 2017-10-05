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


#Load data from with overpass API
#mkdir -p ormxml
#for cnt in "DE" "FR"
#do
#	echo "****** $cnt ******"
#	echo Get raw ORM data for $cnt$
#	wget -O ormxml/orm_$cnt.osm "http://overpass-api.de/api/map?data=[out:xml];(area['ISO3166-1:alpha2'=$cnt][admin_level=2];)->.a;(node[railway](area.a);way[railway](area.a);relation[railway](area.a););(._;>;);out;"
#done



echo Convert OSM-XML country files into a single set of shapefiles
mkdir -p shp
ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -oo CONFIG_FILE=/home/juju/workspace/OpenCarto/GDALormconf.ini -skipfailures -f "ESRI Shapefile" shp osmxml/orm_AT.osm  -overwrite -lco ENCODING=UTF-8
for cnt in "BE" "BG" "CH" "CY" "CZ" "DK" "EE" "ES" "FI" "GB" "GR" "HU" "IE" "IS" "IT" "LT" "LU" "LV" "MT" "NL" "NO" "PL" "PT" "RO" "SE" "SI" "SK"
do
	echo "****** $cnt ******"
	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -oo CONFIG_FILE=/home/juju/workspace/OpenCarto/GDALormconf.ini -skipfailures -f "ESRI Shapefile" shp osmxml/orm_$cnt.osm -append -lco ENCODING=UTF-8
done

echo "Drop fields and rename"
ogr2ogr shp/points.shp shp/points.shp -sql "SELECT osm_id, name, descriptio AS descrip, railway, usage, railway_tr AS traff_mode, historic, ele AS elevat, start_date, end_date, operator FROM points" 
ogr2ogr shp/lines.shp shp/lines.shp -sql "SELECT osm_id, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, start_date, end_date, operator FROM lines"
ogr2ogr shp/multilinestrings.shp shp/multilinestrings.shp -sql "SELECT osm_id, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, start_date, end_date, operator FROM multilinestrings"
ogr2ogr shp/multipolygons.shp shp/multipolygons.shp -sql "SELECT osm_id, name, descriptio AS descrip, railway, gauge, usage, railway_tr AS traff_mode, service, railway__1 AS track_cl, maxspeed, direction, highspeed, historic, bridge, bridge_nam, tunnel, tunnel_nam, electrifie AS electrif, electrif_1 AS elec_rai, voltage, incline, start_date, end_date, operator FROM multipolygons"

echo Filter by attribute value
ogr2ogr shp/points.shp shp/points.shp -sql "SELECT * FROM points WHERE railway IN ('station','halt','stop','station-site','station site','historic_station')"
ogr2ogr shp/lines.shp shp/lines.shp -sql "SELECT * FROM lines WHERE railway IN ('rail','narrow_gauge','construction','proposed','preserved','abandoned','disused','railway','yes','')"
ogr2ogr shp/multilinestrings.shp shp/multilinestrings.shp -sql "SELECT * FROM multilinestrings WHERE railway IN ('rail','narrow_gauge','construction','proposed','preserved','abandoned','disused','railway','yes','')"

echo Projection and spatial indexing
for type in "points" "lines" "multilinestrings" "multipolygons"
do
	ogr2ogr -t_srs EPSG:3035 -s_srs EPSG:4326 shp/$type.shp shp/$type.shp
	ogrinfo -sql "CREATE SPATIAL INDEX ON $type" shp/$type.shp
done

#TODO: Fix all warnings
#get FR and DE. DO filtering at query level? Decompose by zone/region?
