#!/usr/bin/env bash

#https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
#https://stackoverflow.com/questions/31879288/overpass-api-way-coordinates

#http://www.overpass-api.de/api/
#http://overpass.osm.rambler.ru/cgi/
#http://api.openstreetmap.fr/api/

#http://www.overpass-api.de/api/status



cd ~/Bureau/gisco_rail/orm

for usage in "main" "branch" "industrial" "military" "tourism"
do
	echo $usage
	wget -O orm_$usage.osm "http://overpass-api.de/api/map?data=[out:xml];(node[railway][usage=$usage](42,2,47,9);way[railway][usage=$usage](42,2,47,9);relation[railway][usage=$usage](42,2,47,9););(._;>;);out;"
	ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp_$usage orm_$usage.osm  -overwrite
	rm orm_$usage.osm
done
