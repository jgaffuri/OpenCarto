cd ~/Bureau/gisco_rail/orm
wget -O orm.osm "http://overpass-api.de/api/map?data=[out:xml];(node[railway](45,5,46,6);way[railway](45,5,46,6);relation[railway](45,5,46,6););(._;>;);out;"
ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp orm.osm  -overwrite
