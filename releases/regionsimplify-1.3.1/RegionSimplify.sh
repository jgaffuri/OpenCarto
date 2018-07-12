#!/usr/bin/env bash

for dataset in "bangladesh" "chile" "china_mainland"
do
    echo "Generalisation for - "$dataset
	java -jar RegionSimplify.jar -i test_data/$dataset.shp -o test_out/$dataset.shp -s 2000000
done
