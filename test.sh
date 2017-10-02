#http://wiki.openstreetmap.org/wiki/OpenRailwayMap  ---  http://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging
#https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
#https://stackoverflow.com/questions/31879288/overpass-api-way-coordinates

#http://www.overpass-api.de/api/
#http://overpass.osm.rambler.ru/cgi/
#http://api.openstreetmap.fr/api/

#http://www.overpass-api.de/api/status


cd ~/Bureau/gisco_rail/orm
wget -O orm.osm "http://overpass-api.de/api/map?data=[out:xml];(node[railway](43,5,44,6);way[railway](43,5,44,6);relation[railway](43,5,44,6););(._;>;);out;"
ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp orm.osm  -overwrite
