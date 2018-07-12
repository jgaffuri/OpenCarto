mkdir test_out
java -jar RegionSimplify.jar -i test_data/chile.shp -o test_out/chile-5M.shp -s 5000000
java -jar RegionSimplify.jar -i test_data/chile.shp -o test_out/chile-10M.shp -s 10000000
